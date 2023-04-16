package edu.cuhk.csci3310.planet.util;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.cuhk.csci3310.planet.model.Work;

/**
 * Database-related Utilities.
 */
public class DBUtil {

    /**
     * Initialize Firestore database
     */
    public static FirebaseFirestore initFirestore(){
        return FirebaseFirestore.getInstance();
    }

    /**
     * Insert a new work to database
     */
    public static void work_insert(FirebaseFirestore mFirestore, Work work,
                                   OnSuccessListener<DocumentReference> onSuccessListener) {
        CollectionReference works = mFirestore.collection("works");
        works.add(work).addOnSuccessListener(onSuccessListener);
    }

    /**
     * Update a work with specified ID
     */
    public static void work_update(FirebaseFirestore mFirestore, String id, Work work,
                                   OnSuccessListener<Void> onSuccessListener) {
        CollectionReference works = mFirestore.collection("works");
        Map<String, Object> map = new HashMap<>();
        map.put("title", work.getTitle());
        map.put("icon", work.getIcon());
        map.put("importance", work.getImportance());
        map.put("progress", work.getProgress());
        map.put("completed", work.getCompleted());
        map.put("deadline", work.getDeadline());
        map.put("tags", work.getTags());
        map.put("description", work.getDescription());
        works.document(id).update(map).addOnSuccessListener(onSuccessListener);
    }

    /**
     * Delete a work with specified ID
     */
    public static void work_delete(FirebaseFirestore mFirestore, String id,
                                   OnSuccessListener<Void> onSuccessListener) {
        CollectionReference works = mFirestore.collection("works");
        works.document(id).delete().addOnSuccessListener(onSuccessListener);
    }

}
