package tambapps.com.a2sfile_sharing.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileProgressInputStream extends FileInputStream {

    private long bytesRead = 0;
    private final long fileSize;

    public FileProgressInputStream(File file) throws FileNotFoundException {
        super(file);
        fileSize = file.length();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int b = super.read(bytes);
        if (b > 0) {
            bytesRead += b;
        }
        return b;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b > 0) {
            bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        int b = super.read(bytes, i, i1);
        if (b > 0) {
            bytesRead += b;
        }
        return b;
    }

    public int getProgress() { //between 0 and 100
        return (int) (100 * bytesRead / fileSize);
    }
}

