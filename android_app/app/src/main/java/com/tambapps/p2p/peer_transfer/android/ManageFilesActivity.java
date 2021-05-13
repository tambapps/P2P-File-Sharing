package com.tambapps.p2p.peer_transfer.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tambapps.p2p.fandem.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageFilesActivity extends AppCompatActivity {

    private static final int SAVE_FILE_REQUEST_CODE = 12345;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

    private MyAdapter adapter;
    private List<File> files;
    // will be used on activity result
    private File sourceFile = null;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_files);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        files = new ArrayList<>();
        File[] files = ReceiveActivity.getReceivedFilesDirectory(this).listFiles();
        if (files == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.couldnt_get_files_list), Toast.LENGTH_SHORT).show();
            finish();
        } else if (files.length == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_received_files), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            this.files.addAll(Arrays.asList(files));
        }
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File[] files = ReceiveActivity.getReceivedFilesDirectory(this).listFiles();
        if (files != null) {
            List<File> fileList = Arrays.asList(files);
            if (!this.files.equals(fileList)) {
                this.files.clear();
                this.files.addAll(fileList);
                notifyDataSetChanged();
            }
        }
    }

    public void viewFile(File file) {
        // Get URI and MIME type of file
        Uri uri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".io", file);
        String mime = getContentResolver().getType(uri);

        // Open file with user selected app
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_file_item, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final File file = files.get(position);
            holder.position = position;
            holder.fileNameText.setText(file.getName());
            holder.fileSizeText.setText(getString(R.string.file_size, FileUtils.toFileSize(file.length())));

            holder.receivedDateText.setText(getString(R.string.received_date, DATE_FORMAT.format(new Date(file.lastModified()))));
            holder.itemView.setOnClickListener(v -> viewFile(file));
            holder.manageButton.setOnClickListener(v -> manageFileDialog(file));
            holder.itemView.setOnLongClickListener(v -> {
                manageFileDialog(file);
                return true;
            });
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .alpha(1F)
                    .setDuration(250)
                    .start();
        }

        @Override
        public int getItemCount() {
            return files.size();
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView fileNameText;
        private final TextView fileSizeText;
        private final TextView receivedDateText;
        private final ImageButton manageButton;
        int position;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.filename);
            fileSizeText = itemView.findViewById(R.id.filesize);
            // it's normal, we just recycle peer_element
            receivedDateText = itemView.findViewById(R.id.received_date);
            manageButton = itemView.findViewById(R.id.manage_button);
        }
    }

    public void manageFileDialog(File file) {
        AlertDialog alertDialog = new AlertDialog.Builder(ManageFilesActivity.this)
                .setTitle(getString(R.string.handle_received_file, file.getName()))
                .setMessage(getString(R.string.move_to_downloads_description))
                .setPositiveButton(R.string.move_to_downloads, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
                        sourceFile = file;
                        startActivityForResult(intent, SAVE_FILE_REQUEST_CODE);
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .setNegativeButton(R.string.delete, (dialog, which) -> {
                    new AlertDialog.Builder(ManageFilesActivity.this)
                            .setTitle(getString(R.string.confirm_delete, file.getName()))
                            .setNeutralButton(R.string.no, null)
                            .setPositiveButton(R.string.yes, (dialog1, which1) -> {
                                if (!file.delete()) {
                                    Toast.makeText(ManageFilesActivity.this, getString(R.string.couldnt_delete_file), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                removeFromRecycler(file);
                            })
                            .show();
                })
                .create();
        alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(Color.RED));
        alertDialog
                .show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_FILE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, getString(R.string.couldnt_move_file), Toast.LENGTH_SHORT).show();
            } else if (data.getData() == null) {
                Toast.makeText(this, getString(R.string.error_occured_retry), Toast.LENGTH_SHORT).show();
            } else {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.moving_file));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(100);
                progressDialog.setCancelable(false);
                progressDialog.show();
                getExecutorService().submit(() -> moveFile(data.getData(), progressDialog));
            }
        }
    }

    private void moveFile(Uri data, ProgressDialog progressDialog) {
        byte[] buffer = new byte[8192];
        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = getContentResolver().openOutputStream(data)) {
            int lastProgress = 0;
            long bytesProcessed = 0L;
            long totalBytes = sourceFile.length();

            int count;
            while((count = inputStream.read(buffer)) > 0) {
                bytesProcessed += count;
                outputStream.write(buffer, 0, count);
                int progress = (int)Math.min(99L, 100L * bytesProcessed / totalBytes);
                if (progress != lastProgress) {
                    lastProgress = progress;
                    runOnUiThread(() -> progressDialog.setProgress(progress));
                }
            }
            String message = !sourceFile.delete() ? getString(R.string.warning_couldnt_delete) : null;
            progressDialog.dismiss();
            final File fileToRemove = sourceFile;
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.successfully_moved, fileToRemove.getName()))
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, (dialog, which) -> removeFromRecycler(fileToRemove))
                        .setCancelable(false)
                        .show();
                ManageFilesActivity.this.sourceFile = null;
            });
        } catch (IOException e) {
            progressDialog.dismiss();
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle(R.string.error_occured_retry)
                    .setMessage(e.getMessage())
                    .setNeutralButton(android.R.string.ok, null)
                    .show());
        }
    }

    private void removeFromRecycler(File file) {
        files.remove(file);
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (files.size() == 0) {
            Toast.makeText(getApplicationContext(), "No received files were found", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }
}