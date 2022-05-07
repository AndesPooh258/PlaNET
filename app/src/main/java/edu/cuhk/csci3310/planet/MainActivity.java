package edu.cuhk.csci3310.planet;

import java.util.Collections;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import edu.cuhk.csci3310.planet.databinding.ActivityMainBinding;

/**
 * Main activity of the application.
 */
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize view model
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        // restore user email
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mViewModel.setEmail(mPreferences.getString("email", null));
        // set-up bottom navigation activity
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
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