package edu.cuhk.csci3310.planet.util;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.cuhk.csci3310.planet.model.Work;

/**
 * Database-related Utilities.
 */
public class DBUtil {

    public static FirebaseFirestore initFirestore(){
        return FirebaseFirestore.getInstance();
    }

    public static void work_insert(FirebaseFirestore mFirestore, Work work) {
        CollectionReference works = mFirestore.collection("works");
        works.add(work);
    }

    public static void work_update(FirebaseFirestore mFirestore, String id, Work work) {
        CollectionReference works = mFirestore.collection("works");
        works.document(id).update("title", work.getTitle());
        works.document(id).update("icon", work.getIcon());
        works.document(id).update("importance", work.getImportance());
        works.document(id).update("progress", work.getProgress());
        works.document(id).update("deadline", work.getDeadline());
        works.document(id).update("tags", work.getTags());
        works.document(id).update("description", work.getDescription());
    }

    public static void work_delete(FirebaseFirestore mFirestore, String id) {
        CollectionReference works = mFirestore.collection("works");
        works.document(id).delete();
    }

}
