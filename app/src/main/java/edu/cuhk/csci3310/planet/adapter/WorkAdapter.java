package edu.cuhk.csci3310.planet.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.util.WorkUtil;

/**
 * RecyclerView adapter for a list of Works.
 */
public class WorkAdapter extends FirestoreAdapter<WorkAdapter.ViewHolder> {

    static final String mDrawableFilePath = "android.resource://edu.cuhk.csci3310.planet/drawable/";

    public interface OnWorkSelectedListener {

        void onWorkSelected(DocumentSnapshot restaurant);

    }

    private final OnWorkSelectedListener mListener;

    public WorkAdapter(Query query, OnWorkSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_work, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageItemView;
        TextView nameTextView;
        TextView deadlineTextView;
        TextView tagsTextView;
        TextView progressTextView;
        TextView importanceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageItemView = itemView.findViewById(R.id.work_item_image);
            nameTextView = itemView.findViewById(R.id.work_item_name);
            deadlineTextView = itemView.findViewById(R.id.work_item_ddl);
            tagsTextView = itemView.findViewById(R.id.work_item_tags);
            progressTextView = itemView.findViewById(R.id.work_item_progress);
            importanceTextView = itemView.findViewById(R.id.work_item_importance);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnWorkSelectedListener listener) {
            Work work = snapshot.toObject(Work.class);
            if (work != null) {
                Uri uri = Uri.parse(mDrawableFilePath + "image_" + work.getIcon());
                imageItemView.setImageURI(uri);
                nameTextView.setText(work.getTitle());
                deadlineTextView.setText(work.getDeadline());
                tagsTextView.setText(WorkUtil.getTagsString(work));
                progressTextView.setText(work.getProgress() + "%");
                importanceTextView.setText(WorkUtil.getImportanceString(work));
            }

            // click listener
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onWorkSelected(snapshot);
                }
            });
        }
    }
}
