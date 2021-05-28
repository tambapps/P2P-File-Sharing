package com.tambapps.p2p.peer_transfer.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.function.Consumer;

import me.relex.circleindicator.CircleIndicator3;

public class OnBoardingActivity extends AppCompatActivity {

    private final int[] imageRes = new int[] {R.drawable.appicon, R.drawable.wifi, R.drawable.appicon, R.drawable.appicon};
    private final int[] titleRes = new int[] {R.string.welcome_to_fandem, R.string.same_wifi, R.string.hotspot, R.string.lets_get_started};
    private final int[] messageRes = new int[] {R.string.welcome_des, R.string.same_wifi_des, R.string.hotspot_des, R.string.lets_get_started_des};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        ViewPager2 viewPager = findViewById(R.id.viewpager);
        OnBoardingAdapter adapter = new OnBoardingAdapter();
        viewPager.setAdapter(adapter);
        CircleIndicator3 indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        Button button = findViewById(R.id.nextButton);
        button.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < viewPager.getAdapter().getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finish();
            }
        });
        Window window = getWindow();
        View root = findViewById(R.id.root);

        viewPager.registerOnPageChangeCallback(new OnPageChangeListener(
                color -> {
                    window.setNavigationBarColor(color);
                    root.setBackgroundColor(color);
                }, adapter.getItemCount(), button));
    }

    private class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingViewHolder> {

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


    private class OnPageChangeListener extends ViewPager2.OnPageChangeCallback {


        private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        private final Consumer<Integer> backgroundColorUpdater;
        private final int transitionCount;
        private final Button button;
        private final int startColor;
        private final int endColor;

        private OnPageChangeListener(Consumer<Integer> backgroundColorUpdater, int itemCount, Button button) {
            this.backgroundColorUpdater = backgroundColorUpdater;
            this.transitionCount = itemCount - 1; // YES, this is normal. THINK!
            this.button = button;
            Context context = button.getContext();
            this.startColor = context.getColor(R.color.colorPrimaryDark);
            this.endColor = context.getColor(R.color.gradientDown);
        }

        @Override
        public void onPageSelected(int position) {
            if (position == imageRes.length - 1) {
                button.setText(R.string.onboarding_end);
            } else {
                button.setText(R.string.next);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            backgroundColorUpdater.accept((Integer) argbEvaluator.evaluate(
                    (positionOffset + toFloat(position)) / toFloat(transitionCount) ,
                    startColor, endColor));
        }

        private float toFloat(int i) {
            return (float) i;
        }
    }
}