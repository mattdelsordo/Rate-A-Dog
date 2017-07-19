package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mdelsordo.rate_a_dog.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoDogFragment extends Fragment {


    public NoDogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_dog, container, false);

        Button back = (Button)view.findViewById(R.id.b_nodog_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.noDogBack();
            }
        });

        return view;
    }

    public interface NoDogListener{
        void noDogBack();
    }
    private NoDogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (NoDogListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
