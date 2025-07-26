package com.example.mostsave.util;

import androidx.fragment.app.FragmentActivity;
import com.example.mostsave.ui.dialogs.ChooseIconDialogFragment;

/**
 * Helper class to easily show icon chooser dialog
 */
public class IconChooserHelper {

    public static void showIconChooserDialog(FragmentActivity activity,
                                           OnIconSelectedCallback callback) {
        ChooseIconDialogFragment dialog = ChooseIconDialogFragment.newInstance();

        dialog.setOnIconSelectedListener(new ChooseIconDialogFragment.OnIconSelectedListener() {
            @Override
            public void onIconSelected(int iconResId, String iconName) {
                if (callback != null) {
                    callback.onIconSelected(iconResId, iconName);
                }
            }

            @Override
            public void onUploadCustomIcon() {
                if (callback != null) {
                    callback.onUploadCustomIcon();
                }
            }
        });

        dialog.show(activity.getSupportFragmentManager(), "ChooseIconDialog");
    }

    public interface OnIconSelectedCallback {
        void onIconSelected(int iconResId, String iconName);
        void onUploadCustomIcon();
    }
}
