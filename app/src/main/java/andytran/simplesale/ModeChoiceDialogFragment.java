package andytran.simplesale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Andy Tran on 6/21/2015.
 */
public class ModeChoiceDialogFragment extends DialogFragment {
    static private final String[] MODES = {"Public", "Friends", "Private"};
    private ModeChoiceListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (ModeChoiceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement ModeChoiceListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Audience")
                .setItems(MODES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onItemChosen(which);
                    }
                });
        return builder.create();
    }

    public interface ModeChoiceListener {
        void onItemChosen(int which);
    }
}
