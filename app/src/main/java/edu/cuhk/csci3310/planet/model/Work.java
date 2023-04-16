package edu.cuhk.csci3310.planet.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Work POJO.
 */
@IgnoreExtraProperties
public class Work implements Serializable {

    public static final String FIELD_DEADLINE = "deadline";
    private String email; // email of creator
    private String title; // title of work
    private String icon; // icon of work
    private int importance; // importance of work, within the range [1, 3]
    private int progress; // progress of work (%)
    private boolean completed; // whether the work has completed, for better filtering
    private String deadline; // deadline of work
    private String description; // description of work
    private List<String> tags; // tags of work

    public Work(){}

    public Work(String email, String title, String icon, int progress,
                int importance, String deadline, List<String> tags, String description) {
        this.email = email;
        this.title = title;
		this.icon = icon;
		this.progress = progress;
        this.completed = progress == 100;
        this.importance = importance;
        this.deadline = deadline;
        this.tags = tags;
        this.description = description;
    }

    public String getEmail(){
        return email;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

	public String getIcon(){
		return icon;
	}

    public int getImportance(){
        return importance;
    }

    public void setImportance(int importance){
        this.importance = importance;
    }

    public int getProgress(){
        return progress;
    }

    public void setProgress(int progress){
        this.progress = progress;
        this.completed = progress == 100;
    }

    public boolean getCompleted() {
        return completed;
    }

	public String getDeadline(){
		return deadline;
	}

	public void setDeadline(String deadline){
		this.deadline = deadline;
	}

	public List<String> getTags(){
		return tags;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
