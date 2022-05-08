package edu.cuhk.csci3310.planet.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import edu.cuhk.csci3310.planet.model.Work;

/**
 * Work-related Utilities.
 */
public class WorkUtil {

    /**
     * Get deadlines as formatted string
     */
    public static String getDeadlineString(LocalDateTime deadline) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return deadline.format(dateFormat);
    }

    /**
     * Get deadlines as LocalDateTime object
     */
    public static LocalDateTime getDeadlineObject(String deadline) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        try {
            return LocalDateTime.parse(deadline, dateFormat);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get deadlines as number of milliseconds from the epoch of 1970-01-01T00:00:00Z
     */
    public static long getDeadlineMillis(Work work) {
        return getDeadlineMillis(work.getDeadline());
    }

    public static long getDeadlineMillis(String deadline) {
        LocalDateTime localDateTime = getDeadlineObject(deadline);
        if (localDateTime != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(currentTime);
            return localDateTime.toEpochSecond(zoneOffset) * 1000;
        } else {
            return -1;
        }
    }

    /**
     * Get current time as formatted string
     */
    public static String getCurrentTimeString(int offsetMin) {
        return getDeadlineString(LocalDateTime.now().plusMinutes(offsetMin));
    }

    public static String getCurrentTimeString() {
        return getDeadlineString(LocalDateTime.now());
    }

    /**
     * Get tags as formatted string
     */
    public static String getTagsString(Work work) {
        return getTagsString(work.getTags());
    }

    public static String getTagsString(List<String> tags) {
        // only allow to add 2 tags at most
        switch (tags.size()) {
            case 0:
                return "";
            case 1:
                return tags.get(0);
            default:
                return tags.get(0) + ", " + tags.get(1);
        }
    }

    /**
     * Get importance represented as asterisk signs
     */
    public static String getImportanceString(Work work) {
        return getImportanceString(work.getImportance());
    }

    public static String getImportanceString(int importance) {
        switch (importance) {
            case 1:
                return "*";
            case 2:
                return "**";
            case 3:
            default:
                return "***";
        }
    }
}
