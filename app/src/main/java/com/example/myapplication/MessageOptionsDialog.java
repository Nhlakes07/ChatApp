package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MessageOptionsDialog extends DialogFragment
{
    private MessageOptionListener listener;

    public MessageOptionsDialog(MessageOptionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{"Copy", "Delete", "Forward", "React"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    switch (which) {
                        case 0:
                            listener.onCopyClicked();
                            break;
                        case 1:
                            listener.onDeleteClicked();
                            break;
                        case 2:
                            listener.onForwardClicked();
                            break;
                        case 3:
                            listener.onReactClicked();
                            break;
                    }
                }
            }
        });

        return builder.create();
    }

    public interface MessageOptionListener {
        void onCopyClicked();

        void onDeleteClicked();

        void onForwardClicked();

        void onReactClicked();
    }
}
