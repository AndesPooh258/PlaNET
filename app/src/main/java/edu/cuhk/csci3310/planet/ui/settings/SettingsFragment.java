package edu.cuhk.csci3310.planet.ui.settings;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.databinding.FragmentSettingsBinding;

/**
 * Fragment containing the setting page.
 */
public class SettingsFragment extends Fragment implements
        View.OnClickListener {

    private SettingsViewModel settingsViewModel;
    private SharedPreferences mPreferences;
    private FragmentSettingsBinding binding;
    private SwitchCompat switch_dark_mode;
    private EditText timezoneEditText;
    private Spinner spinner_reminder_time;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialize view model
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        // get shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        if (getActivity() != null) {
            mPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        }
        settingsViewModel.setDarkMode(mPreferences.getBoolean("dark_mode", false));
        settingsViewModel.setTimezone(mPreferences.getInt("timezone", 8));
        settingsViewModel.setReminderTime(mPreferences.getInt("reminder_time", -1));
        // initialize view
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        switch_dark_mode = root.findViewById(R.id.switch_dark_mode);
        timezoneEditText = root.findViewById(R.id.editText_timezone);
        spinner_reminder_time = root.findViewById(R.id.spinner_reminder_time);
        switch_dark_mode.setChecked(settingsViewModel.getDarkMode());
        timezoneEditText.setText("" + settingsViewModel.getTimezone());
        spinner_reminder_time.setSelection(
                getReminderSpinnerIndex(settingsViewModel.getReminderTime()));
        // set onClickListener
        View button_update = root.findViewById(R.id.button_update);
        button_update.setOnClickListener(this);
        return root;
    }

    public void onUpdateClicked() {
        if (isValidForm()) {
            settingsViewModel.setDarkMode(getSelectedDarkMode());
            settingsViewModel.setTimezone(getSelectedTimezone());
            settingsViewModel.setReminderTime(getSelectedReminderTime());
            // save setting
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putBoolean("dark_mode", settingsViewModel.getDarkMode());
            preferencesEditor.putInt("timezone", settingsViewModel.getTimezone());
            preferencesEditor.putInt("reminder_time", settingsViewModel.getReminderTime());
            preferencesEditor.apply();
            // display update message
            Toast updateToast = Toast.makeText(getContext(),
                    R.string.setting_updated,
                    Toast.LENGTH_LONG);
            updateToast.show();
        } else {
            Toast invalidToast = Toast.makeText(getContext(),
                    R.string.invalid_input,
                    Toast.LENGTH_LONG);
            invalidToast.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean getSelectedDarkMode() {
        return null != switch_dark_mode && switch_dark_mode.isChecked();
    }

    private int getSelectedTimezone() {
        String selected = timezoneEditText.getText().toString();
        int factor = selected.startsWith("-") ? -1 : 1;
        int start = selected.startsWith("+") || selected.startsWith("-") ? 1 : 0;
        int value;
        try {
            value = Integer.parseInt(selected.substring(start), 10);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 8;
        }
        return factor * value;
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

    public boolean isValidForm() {
        int timezone = getSelectedTimezone();
        return timezone >= -12 && timezone <= 14;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_update) {
            onUpdateClicked();
        }
    }
}