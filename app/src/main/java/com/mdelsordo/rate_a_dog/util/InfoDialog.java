package com.mdelsordo.rate_a_dog.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mdelsordo.rate_a_dog.R;

/**
 * Created by mdelsord on 7/19/17.
 * Displays info to about the app to the user
 */

public class InfoDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_info, null);
        TextView privacy = (TextView)view.findViewById(R.id.tv_info_privacypolicy);
        privacy.setText(Html.fromHtml("<a href=\"https://mattdelsordo.github.io/Rate-A-Dog/privacy_policy.html\">Privacy Policy</a>"));
        privacy.setMovementMethod(LinkMovementMethod.getInstance());

        return new AlertDialog.Builder(getActivity()).setView(view).setTitle("Info").setPositiveButton(getString(R.string.close), null).create();
    }
}
