package edu.cuhk.csci3310.planet.model;

import android.content.Context;
import android.text.TextUtils;

import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.util.WorkUtil;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String name = null;
    private String tag = null;
    private String deadline = null;
    private int progress = -1;
    private int importance = -1;
    private int deadline_constraint = -2; // constraint: -2 => any, -1 => le, 0 => eq, 1 => ge
    private boolean showPast = false;

    public Filters() {
    }

    public static Filters getDefault() {
        return new Filters();
    }

    public boolean hasName() {
        return !(TextUtils.isEmpty(name));
    }

    public boolean hasTag() {
        return !(TextUtils.isEmpty(tag));
    }

    public boolean hasDeadline() {
        return !(TextUtils.isEmpty(deadline));
    }

    public boolean hasProgress() {
        return (progress >= 0 && progress <= 100);
    }

    public boolean hasImportance() {
        return (importance >= 1 && importance <= 3);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getDeadline_constraint() {
        return deadline_constraint;
    }

    public void setDeadline_constraint(int constraint) {
        this.deadline_constraint = constraint;
    }

    public boolean getShowPast() {
        return showPast;
    }

    public void setShowPast(boolean showPast) {
        this.showPast = showPast;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (name == null && tag == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_works));
            desc.append("</b>");
        }

        if (name != null) {
            desc.append("<b>");
            desc.append(name);
            desc.append("</b>");
        }

        if (name != null && tag != null) {
            desc.append(" with tag ");
        }

        if (tag != null) {
            desc.append("<b>");
            desc.append(tag);
            desc.append("</b>");
        }

        if (importance > 0) {
            desc.append(" for importance = ");
            desc.append("<b>");
            desc.append(WorkUtil.getImportanceString(importance));
            desc.append("</b>");
        }
        return desc.toString();
    }

}
