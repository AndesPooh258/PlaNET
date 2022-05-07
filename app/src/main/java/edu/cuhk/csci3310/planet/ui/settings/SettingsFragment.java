package edu.cuhk.csci3310.planet.ui.settings;

import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
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

    public final String TAG = "SettingsFragment";
    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;
    private SharedPreferences mPreferences;
    private SwitchCompat switch_dark_mode;
    private EditText timezoneEditText;
    private Spinner spinner_reminder_time;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialize view model
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        // initialize view
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        switch_dark_mode = root.findViewById(R.id.switch_dark_mode);
        timezoneEditText = root.findViewById(R.id.editText_timezone);
        spinner_reminder_time = root.findViewById(R.id.spinner_reminder_time);
        // set onClickListener
        View button_update = root.findViewById(R.id.button_update);
        button_update.setOnClickListener(this);
        // set shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        if (getActivity() != null) {
            mPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        }
        return root;
    }

    public void onUpdateClicked() {
        if (getSelectedDarkMode()) {
            Log.d(TAG, "dark mode is true");
        } else Log.d(TAG, "dark mode is false");
        Log.d(TAG, "timezone: " + getSelectedTimeZone());
        Log.d(TAG, "reminder time: " + getSelectedReminderTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean getSelectedDarkMode() {
        return null != switch_dark_mode && switch_dark_mode.isChecked();
    }

    private int getSelectedTimeZone() {
        String selected = timezoneEditText.getText().toString();
        int factor = selected.startsWith("-") ? -1 : 1;
        int value;
        try {
            value = Integer.parseInt(selected.substring(1), 10);
        } catch (NumberFormatException e) {
            return -1;
        }
        if (factor == 1 && (value < 0 || value > 14))
            return -1;
        else if (factor == -1 && (value < 0 || value > 12))
            return -1;
        else return factor * value;
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_update) {
            onUpdateClicked();
        }
    }
}