package edu.cuhk.csci3310.planet;

import androidx.lifecycle.ViewModel;
import edu.cuhk.csci3310.planet.model.Work;

/**
 * View model of WorkDetailActivity.
 */
public class DetailActivityViewModel extends ViewModel {

    private String workId;
    private Work mWork;

    public DetailActivityViewModel() {
        workId = null;
    }

    public String getWorkId(){
        return workId;
    }

    public void setWorkId(String workId){
        this.workId = workId;
    }

    public Work getWork() {
        return mWork;
    }

    public void setWork(Work mWork) {
        this.mWork = mWork;
    }
}
