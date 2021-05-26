package com.tambapps.p2p.peer_transfer.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class OnBoardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        ViewPager2 viewPager = findViewById(R.id.viewpager);
        OnBoardingAdapter adapter = new OnBoardingAdapter();
        viewPager.setAdapter(adapter);
    }

    private class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingViewHolder> {

        private final int[] imageRes = new int[] {R.drawable.appicon, R.drawable.wifi, R.drawable.appicon, R.drawable.appicon};
        private final int[] titleRes = new int[] {R.string.welcome_to_fandem, R.string.same_wifi, R.string.hotspot, R.string.lets_get_started};
        private final int[] messageRes = new int[] {R.string.welcome_des, R.string.same_wifi_des, R.string.hotspot_des, R.string.lets_get_started_des};
        @NonNull
        @Override
        public OnBoardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onboarding_page, parent, false);
            return new OnBoardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnBoardingViewHolder holder, int position) {
            holder.imageView.setImageResource(imageRes[position]);
            holder.titleView.setText(getString(titleRes[position]));
            holder.descriptionView.setText(getString(messageRes[position]));
        }

        @Override
        public int getItemCount() {
            return titleRes.length;
        }
    }


    private static class OnBoardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView descriptionView;

        public OnBoardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
            titleView = itemView.findViewById(R.id.title);
            descriptionView = itemView.findViewById(R.id.description);
        }
    }
}