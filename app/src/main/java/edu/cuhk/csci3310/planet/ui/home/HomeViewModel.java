package edu.cuhk.csci3310.planet.ui.home;

import edu.cuhk.csci3310.planet.MainActivityViewModel;
import edu.cuhk.csci3310.planet.model.Filters;

/**
 * View model of HomeFragment.
 */
public class HomeViewModel extends MainActivityViewModel {

    private Filters mFilters;

    public HomeViewModel() {
        mFilters = Filters.getDefault();
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }

}