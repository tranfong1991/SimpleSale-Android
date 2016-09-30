package andytran.simplesale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Andy Tran on 8/27/2015.
 */
public class VenmoUserNameDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_venmo_user_name, null);
        final EditText venmoUserName = (EditText) view.findViewById(R.id.venmo_username);

        builder.setView(view)
                .setTitle("REQUIRED")
                .setPositiveButton("Done", null);

        final AlertDialog d = builder.create();
        d.setCanceledOnTouchOutside(false);
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String input = venmoUserName.getText().toString();

                        if (input.length() > 0) {
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("venmoUserName", input);
                            editor.commit();

                            Toast.makeText(getActivity(), "Venmo user name added", Toast.LENGTH_SHORT).show();
                            d.dismiss();
                        }
                    }
                });
            }
        });

        return d;
    }
}
