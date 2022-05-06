package edu.cuhk.csci3310.planet.ui.dashboard;

import edu.cuhk.csci3310.planet.MainActivityViewModel;
import edu.cuhk.csci3310.planet.model.Work;

/**
 * View model of DashboardFragment.
 */
public class DashboardViewModel extends MainActivityViewModel {

    private int work_all = -1;
    private int work_completed = -1;
    private int work_todo = -1;
    private Work recommended_work = null;

    public DashboardViewModel() {}

    public int getWorkAll() {
        return work_all;
    }

    public void setWorkAll(int work_all) {
        this.work_all = work_all;
    }

    public int getWorkCompleted() {
        return work_completed;
    }

    public void setWorkCompleted(int work_completed) {
        this.work_completed = work_completed;
    }

    public int getWorkTodo() {
        return work_todo;
    }

    public void setWorkTodo(int work_todo) {
        this.work_todo = work_todo;
    }

    public Work getRecommendedWork() {
        return recommended_work;
    }

    public void setRecommended_work(Work recommended_work) {
        this.recommended_work = recommended_work;
    }

}