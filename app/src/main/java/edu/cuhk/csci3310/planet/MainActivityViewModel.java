package edu.cuhk.csci3310.planet;

import androidx.lifecycle.ViewModel;

/**
 * View model of MainActivity.
 */
public class MainActivityViewModel extends ViewModel {

    private boolean isSigningIn = false;
    private boolean isSignedIn = false;
    private boolean dark_mode = false;
    private String email = null;
    private int reminder_time = -1;

    public MainActivityViewModel() {}

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public void setIsSigningIn(boolean isSigningIn) {
        this.isSigningIn = isSigningIn;
    }

    public boolean getIsSignedIn() {
        return isSignedIn;
    }

    public boolean getDarkMode() {
        return dark_mode;
    }

    public void setDarkMode(boolean dark_mode) {
        this.dark_mode = dark_mode;
    }

    public void setIsSignedIn(boolean isSignedIn) {
        this.isSignedIn = isSignedIn;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public int getReminderTime() {
        return reminder_time;
    }

    public void setReminderTime(int reminder_time) {
        this.reminder_time = reminder_time >= 0 ? reminder_time : -1;
    }
}
