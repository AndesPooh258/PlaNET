package edu.cuhk.csci3310.planet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.ui.dialog.RequestDialogFragment;
import edu.cuhk.csci3310.planet.ui.home.HomeFragment;
import edu.cuhk.csci3310.planet.util.DBUtil;
import edu.cuhk.csci3310.planet.util.NotificationUtils;
import edu.cuhk.csci3310.planet.util.WorkUtil;

/**
 * Activity displaying work detail.
 */
public class WorkDetailActivity extends AppCompatActivity implements
    RequestDialogFragment.ChangeListener {

    static final String mDrawableFilePath = "android.resource://edu.cuhk.csci3310.planet/drawable/";
    private DetailActivityViewModel mDetailViewModel;
    private FirebaseFirestore mFirestore;
    private SharedPreferences mPreferences;
    private RequestDialogFragment mRequestDialog;
    private ImageView imageDetailView;
    private TextView nameTextView;
    private TextView deadlineTextView;
    private TextView tagsTextView;
    private TextView progressTextView;
    private ProgressBar progressBar;
    private TextView importanceTextView;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_detail);
        // initialize view model
        mDetailViewModel = new ViewModelProvider(this).get(DetailActivityViewModel.class);
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        // get shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mDetailViewModel.setEmail(mPreferences.getString("email", null));
        mDetailViewModel.setReminderTime(mPreferences.getInt("reminder_time", -1));
        // get intent
        Intent intent = getIntent();
        String id = (String) intent.getExtras().get(HomeFragment.Id_MESSAGE);
        Work work = (Work) intent.getExtras().get(HomeFragment.WORK_MESSAGE);
        mDetailViewModel.setWorkId(id);
        mDetailViewModel.setWork(work);
        if (work != null) {
            // initialize view
            imageDetailView = findViewById(R.id.work_image);
            nameTextView = findViewById(R.id.work_name);
            deadlineTextView = findViewById(R.id.work_ddl);
            tagsTextView = findViewById(R.id.work_tags);
            progressTextView = findViewById(R.id.work_progress_text);
            progressBar = findViewById(R.id.work_progress_bar);
            importanceTextView = findViewById(R.id.work_importance);
            descriptionEditText = findViewById(R.id.work_description_text);
            // set view content
            updateUI(work);
            // initialize fragment
            mRequestDialog = RequestDialogFragment.newInstance(
                    work.getEmail(), id, work, mDetailViewModel.getReminderTime());
        } else onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Work work = mDetailViewModel.getWork();
        String description = getSelectedDescription();
        if (work != null) {
            if (!description.equals(work.getDescription())) {
                work.setDescription(description);
                DBUtil.work_update(mFirestore, mDetailViewModel.getWorkId(), work, null);
                // display update message
                Toast updateToast = Toast.makeText(this,
                        R.string.work_description_updated,
                        Toast.LENGTH_LONG);
                updateToast.show();
            }
        }
    }

    @Override
    public void onChange(Work work) {
        mDetailViewModel.setWork(work);
        mRequestDialog = RequestDialogFragment.newInstance(
                work.getEmail(), mDetailViewModel.getWorkId(),
                work, mDetailViewModel.getReminderTime());
        updateUI(work);
    }

    private String getSelectedDescription() {
        return descriptionEditText == null ? "" : descriptionEditText.getText().toString();
    }
    
    public void editWork(View view) {
        // show the dialog containing edit work form
        if (!mRequestDialog.isAdded()) {
            mRequestDialog.show(getSupportFragmentManager(), null);
        }
    }

    public void deleteWork(View view) {
        // delete the work
        DBUtil.work_delete(mFirestore, mDetailViewModel.getWorkId(),
                aVoid -> NotificationUtils.reminderNotification(getBaseContext(), mFirestore,
                        mDetailViewModel.getEmail(),
                        mDetailViewModel.getReminderTime()));
        mDetailViewModel.setWork(null);
        onBackPressed();
    }

    public void backHome(View view) {
        onBackPressed();
    }

    public void updateUI(Work work) {
        if (work != null) {
            Uri uri = Uri.parse(mDrawableFilePath + "image_" + work.getIcon());
            imageDetailView.setImageURI(uri);
            nameTextView.setText(work.getTitle());
            deadlineTextView.setText(getString(R.string.work_ddl, work.getDeadline()));
            progressTextView.setText(getString(R.string.work_progress, work.getProgress()));
            progressBar.setProgress(work.getProgress());
            importanceTextView.setText(
                    getString(R.string.work_importance, WorkUtil.getImportanceString(work)));
            tagsTextView.setText(getString(R.string.work_tags, WorkUtil.getTagsString(work)));
            descriptionEditText.setText(work.getDescription());
        }
    }
}
