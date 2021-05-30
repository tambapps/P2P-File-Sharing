package com.tambapps.p2p.peer_transfer.android.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tambapps.p2p.peer_transfer.android.OnBoardingActivity;
import com.tambapps.p2p.peer_transfer.android.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private int index;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        int layoutId;
        switch (index) {
            default:
            case 0:
                layoutId = R.layout.fragment_help;
                break;
            case 1:
                layoutId = R.layout.fragment_send;
                break;
            case 2:
                layoutId = R.layout.fragment_receive;
                break;
        }
        View v = inflater.inflate(layoutId, container, false);
        if (index == 0) {
            Button button = v.findViewById(R.id.introButton);
            button.setOnClickListener((__) -> requireActivity().startActivity(new Intent(getActivity(), OnBoardingActivity.class)));
        }
        return v;
    }
}