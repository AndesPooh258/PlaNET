package edu.cuhk.csci3310.planet.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.cuhk.csci3310.planet.MainActivity;
import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.databinding.FragmentSettingsBinding;
import edu.cuhk.csci3310.planet.util.DBUtil;
import edu.cuhk.csci3310.planet.util.NotificationUtils;

/**
 * Fragment containing the setting page.
 */
public class SettingsFragment extends Fragment implements
        View.OnClickListener {

    private SettingsViewModel mSettingsViewModel;
    private FirebaseFirestore mFirestore;
    private SharedPreferences mPreferences;
    private FragmentSettingsBinding binding;
    private SwitchCompat switch_dark_mode;
    private Spinner spinner_reminder_time;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialize view model
        mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        // get shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        if (getActivity() != null) {
            mPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
            mSettingsViewModel.setEmail(mPreferences.getString("email", null));
            mSettingsViewModel.setDarkMode(mPreferences.getBoolean("dark_mode", false));
            mSettingsViewModel.setReminderTime(mPreferences.getInt("reminder_time", -1));
        }
        // initialize view
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        switch_dark_mode = root.findViewById(R.id.switch_dark_mode);
        spinner_reminder_time = root.findViewById(R.id.spinner_reminder_time);
        switch_dark_mode.setChecked(mSettingsViewModel.getDarkMode());
        spinner_reminder_time.setSelection(
                getReminderSpinnerIndex(mSettingsViewModel.getReminderTime()));
        // set onClickListener
        View button_update = root.findViewById(R.id.button_update);
        View logout_button = root.findViewById(R.id.button_logout);
        button_update.setOnClickListener(this);
        logout_button.setOnClickListener(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_update) {
            onUpdateClicked();
        }  else if (id == R.id.button_logout) {
            mSettingsViewModel.setIsSignedIn(false);
            mSettingsViewModel.setEmail(null);
            MainActivity mainActivity = (MainActivity) this.getActivity();
            if (mainActivity != null){
                mainActivity.startSignOut();
            }
        }
    }

    public void onUpdateClicked() {
        int old_reminder_time = mSettingsViewModel.getReminderTime();
        int new_reminder_time = getSelectedReminderTime();
        mSettingsViewModel.setDarkMode(getSelectedDarkMode());
        mSettingsViewModel.setReminderTime(new_reminder_time);
        // save setting
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putBoolean("dark_mode", mSettingsViewModel.getDarkMode());
        preferencesEditor.putInt("reminder_time", mSettingsViewModel.getReminderTime());
        preferencesEditor.apply();
        // update notification
        if (new_reminder_time != old_reminder_time) {
            NotificationUtils.reminderNotification(getContext(), mFirestore,
                    mSettingsViewModel.getEmail(),
                    mSettingsViewModel.getReminderTime());
        }
        // display update message
        Toast updateToast = Toast.makeText(getContext(),
                R.string.setting_updated,
                Toast.LENGTH_LONG);
        updateToast.show();
    }

    private boolean getSelectedDarkMode() {
        return null != switch_dark_mode && switch_dark_mode.isChecked();
    }

    private int getSelectedReminderTime() {
        String selected = (String) spinner_reminder_time.getSelectedItem();
        if (getString(R.string.time1).equals(selected)) {
            return 15;
        } else if (getString(R.string.time2).equals(selected)) {
            return 30;
        } else if (getString(R.string.time3).equals(selected)) {
            return 60;
        } else if (getString(R.string.time4).equals(selected)) {
            return 360;
        } else if (getString(R.string.time5).equals(selected)) {
            return 720;
        } else if (getString(R.string.time6).equals(selected)) {
            return 1440;
        } else if (getString(R.string.time7).equals(selected)) {
            return 2880;
        } else return -1;
    }

    private int getReminderSpinnerIndex(int reminder_time) {
        switch(reminder_time) {
            case 15:
                return 1;
            case 30:
                return 2;
            case 60:
                return 3;
            case 360:
                return 4;
            case 720:
                return 5;
            case 1440:
                return 6;
            case 2880:
                return 7;
            default:
                return 0;
        }
    }
}