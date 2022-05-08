package edu.cuhk.csci3310.planet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;

import edu.cuhk.csci3310.planet.databinding.ActivityMainBinding;
import edu.cuhk.csci3310.planet.util.DBUtil;
import edu.cuhk.csci3310.planet.util.NotificationUtils;

/**
 * Main activity of the application.
 */
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;
    private SharedPreferences mPreferences;
    private FirebaseFirestore mFirestore;
    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize view model
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        // get shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mViewModel.setEmail(mPreferences.getString("email", null));
        mViewModel.setDarkMode(mPreferences.getBoolean("dark_mode", false));
        mViewModel.setReminderTime(mPreferences.getInt("reminder_time", -1));
        // set-up bottom navigation activity
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        mNavController = Navigation.findNavController(this,
                R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, mNavController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, mNavController);
        // set to dark mode if necessary
        if (mViewModel.getDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (mViewModel.getEmail() != null) {
            NotificationUtils.reminderNotification(this, mFirestore,
                    mViewModel.getEmail(),
                    mViewModel.getReminderTime());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save user email
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("email", mViewModel.getEmail());
        preferencesEditor.apply();
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // sign in with FirebaseUI
        Intent intent = AuthUI.getInstance()
                              .createSignInIntentBuilder()
                              .setAvailableProviders(Collections.singletonList(
                                new AuthUI.IdpConfig.EmailBuilder().build()))
                              .setIsSmartLockEnabled(false)
                              .build();
        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            // update view model
            mViewModel.setIsSignedIn(true);
            mViewModel.setEmail(
                    ((IdpResponse) data.getExtras().get("extra_idp_response")).getEmail());
            // save user email
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putString("email", mViewModel.getEmail());
            preferencesEditor.apply();
            if (requestCode == RC_SIGN_IN) {
                mViewModel.setIsSigningIn(false);
                // result is not ok
                if (resultCode != RESULT_OK && shouldStartSignIn()) {
                    mViewModel.setIsSignedIn(false);
                    startSignIn();
                }
                NotificationUtils.reminderNotification(this, mFirestore,
                        mViewModel.getEmail(),
                        mViewModel.getReminderTime());
                mNavController.navigate(R.id.navigation_home);
            }
        } else startSignIn();
    }

    public void startSignOut() {
        // update view model
        mViewModel.setIsSignedIn(false);
        mViewModel.setEmail(null);
        // sign out with Firebase
        AuthUI.getInstance().signOut(this);
        startSignIn();
    }
}