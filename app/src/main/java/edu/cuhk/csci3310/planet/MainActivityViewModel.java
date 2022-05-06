package edu.cuhk.csci3310.planet;

import androidx.lifecycle.ViewModel;

/**
 * View model of MainActivity.
 */
public class MainActivityViewModel extends ViewModel {

    private boolean isSigningIn = false;
    private boolean isSignedIn = false;
    private String email = null;

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

    public void setIsSignedIn(boolean isSignedIn) {
        this.isSignedIn = isSignedIn;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }
}
