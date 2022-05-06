package edu.cuhk.csci3310.planet.ui.dashboard;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.cuhk.csci3310.planet.MainActivity;
import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.databinding.FragmentDashboardBinding;
import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.ui.dialog.RequestDialogFragment;
import edu.cuhk.csci3310.planet.util.DBUtil;
import edu.cuhk.csci3310.planet.util.WorkUtil;

/**
 * Fragment containing the dashboard.
 */
public class DashboardFragment extends Fragment implements
        View.OnClickListener,
        RequestDialogFragment.ChangeListener{

    static final String mDrawableFilePath = "android.resource://edu.cuhk.csci3310.planet/drawable/";
    private DashboardViewModel mDashboardViewModel;
    private FragmentDashboardBinding binding;
    private FirebaseFirestore mFirestore;
    private RequestDialogFragment mRequestDialog;
    private ImageView imageItemView;
    private TextView nameTextView;
    private TextView tagsTextView;
    private TextView progressTextView;
    private TextView importanceTextView;
    private TextView deadlineTextView;
    private TextView allTextView;
    private TextView completedTextView;
    private TextView todoTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        // initialize view model
        mDashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        // initialize view
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        imageItemView = root.findViewById(R.id.work_item_image);
        nameTextView = root.findViewById(R.id.work_item_name);
        tagsTextView = root.findViewById(R.id.work_item_tags);
        progressTextView = root.findViewById(R.id.work_item_progress);
        importanceTextView = root.findViewById(R.id.work_item_importance);
        deadlineTextView = root.findViewById(R.id.work_item_ddl);
        allTextView = root.findViewById(R.id.textView_all);
        completedTextView = root.findViewById(R.id.textView_completed);
        todoTextView = root.findViewById(R.id.textView_todo);
        // set onClickListener
        View add_button = root.findViewById(R.id.button_add);
        View logout_button = root.findViewById(R.id.button_logout);
        add_button.setOnClickListener(this);
        logout_button.setOnClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if the user has signed in
        if (!mDashboardViewModel.getIsSignedIn()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                // update view model
                mDashboardViewModel.setEmail(user.getEmail());
                mDashboardViewModel.setIsSignedIn(true);
                // initialize fragment
                String email = mDashboardViewModel.getEmail();
                mRequestDialog = RequestDialogFragment.newInstance(email, null, null);
                updateDashboard();
            }
        }
    }

    public int getWeight(Work work, String currentTime) {
        int weight;
        LocalDateTime current = WorkUtil.getDeadlineObject(currentTime);
        LocalDateTime deadline = WorkUtil.getDeadlineObject(work.getDeadline());
        long diff = ChronoUnit.DAYS.between(current, deadline);
        // check difference in deadline
        if (diff < 1){
            weight = 1000;
        } else if (diff < 3) {
            weight = 50;
        } else if (diff < 7) {
            weight = 25;
        } else if (diff < 14) {
            weight = 10;
        } else weight = 1;
        // check importance
        weight *= (work.getImportance() * work.getImportance());
        // check progress
        weight *= (100 - work.getProgress());
        return weight;
    }

    public void updateDashboard() {
        String email = mDashboardViewModel.getEmail();
        String currentTime = WorkUtil.getDeadlineString(LocalDateTime.now());
        mFirestore.collection("works")
                .whereEqualTo("email", email)
                .get().addOnSuccessListener(result -> {
            int i;
            int all = result.size();
            int completed = 0;
            int todo = 0;
            int total_weight = 0;
            ArrayList<Work> todoWorks = new ArrayList<>();
            ArrayList<Integer> weights = new ArrayList<>();
            // count user statistics
            for (i = 0; i < all; i++) {
                Work work = result.getDocuments().get(i).toObject(Work.class);
                if (work != null) {
                    if (work.getProgress() == 100) {
                        completed++;
                    } else if (currentTime.compareTo(work.getDeadline()) < 0) {
                        todo++;
                        todoWorks.add(work);
                        total_weight += getWeight(work, currentTime);
                        weights.add(total_weight);
                    }
                }
            }
            // update view model
            mDashboardViewModel.setWorkAll(all);
            mDashboardViewModel.setWorkCompleted(completed);
            mDashboardViewModel.setWorkTodo(todo);
            if (total_weight > 0) {
                int randomNum = ThreadLocalRandom.current()
                        .nextInt(0, total_weight + 1);
                for (i = 0; i < all; i++) {
                    if (weights.get(i) >= randomNum) {
                        break;
                    }
                }
                mDashboardViewModel.setRecommended_work(todoWorks.get(i));
            } else mDashboardViewModel.setRecommended_work(null);
            updateUI();
        });
    }

    public void updateUI() {
        Work work = mDashboardViewModel.getRecommendedWork();
        // update recommended task
        if (work != null) {
            Uri uri = Uri.parse(mDrawableFilePath + "image_" + work.getIcon());
            imageItemView.setImageURI(uri);
            nameTextView.setText(work.getTitle());
            tagsTextView.setText(WorkUtil.getTagsString(work));
            progressTextView.setText(work.getProgress() + "%");
            importanceTextView.setText(WorkUtil.getImportanceString(work));
            deadlineTextView.setText(work.getDeadline());
        } else {
            Uri uri = Uri.parse(mDrawableFilePath + "image_sleep");
            imageItemView.setImageURI(uri);
            nameTextView.setText(R.string.rest_name);
            tagsTextView.setText(R.string.rest_tags);
            progressTextView.setText(R.string.rest_progress);
            importanceTextView.setText(WorkUtil.getImportanceString(3));
            deadlineTextView.setText(R.string.rest_ddl);
        }
        // update user statistics
        if (getActivity() != null) {
            allTextView.setText(getActivity().getString(
                    R.string.user_stat_all, mDashboardViewModel.getWorkAll()));
            completedTextView.setText(getActivity().getString(
                    R.string.user_stat_completed, mDashboardViewModel.getWorkCompleted()));
            todoTextView.setText(getActivity().getString(
                    R.string.user_stat_todo, mDashboardViewModel.getWorkTodo()));
        }
    }

    public void addWork() {
        // show the dialog containing add work form
        mRequestDialog.show(getParentFragmentManager(), RequestDialogFragment.TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onChange(Work work) {
        updateDashboard();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_add){
            addWork();
        } else if (id == R.id.button_logout) {
            mDashboardViewModel.setIsSignedIn(false);
            mDashboardViewModel.setEmail(null);
            MainActivity mainActivity = (MainActivity) this.getActivity();
            if (mainActivity != null){
                mainActivity.startSignOut();
            }
        }
    }
}