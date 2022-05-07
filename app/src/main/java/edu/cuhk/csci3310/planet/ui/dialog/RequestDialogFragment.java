package edu.cuhk.csci3310.planet.ui.dialog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.ui.dashboard.DashboardFragment;
import edu.cuhk.csci3310.planet.util.DBUtil;

/**
 * Dialog Fragment containing request form.
 */
public class RequestDialogFragment extends DialogFragment implements
        View.OnClickListener {

    public static final String TAG = "RequestDialog";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_WORKID = "workId";
    private static final String ARG_WORK = "mWork";
    private FirebaseFirestore mFirestore;
    private ChangeListener mChangeListener;
    private String request_type;
    private String email;
    private String workId;
    private Work mWork;
    private EditText nameEditText;
    private Spinner iconSpinner;
    private Spinner tagSpinner;
    private EditText tag1EditText;
    private EditText tag2EditText;
    private EditText progressEditText;
    private Spinner importanceSpinner;
    private EditText deadlineEditText;
    private EditText descriptionEditText;

    public RequestDialogFragment() {
        // required empty public constructor
    }

    public interface ChangeListener {
        void onChange(Work work);
    }

    // factory method to create new instance
    public static RequestDialogFragment newInstance(String email, String workId, Work mWork) {
        RequestDialogFragment fragment = new RequestDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_WORKID, workId);
        args.putSerializable(ARG_WORK, mWork);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
            workId = getArguments().getString(ARG_WORKID);
            mWork = (Work) getArguments().getSerializable(ARG_WORK);
            request_type = workId == null || mWork == null ? "create" : "edit";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        // initialize view
        View mRootView = inflater.inflate(R.layout.dialog_request, container, false);
        nameEditText = mRootView.findViewById(R.id.editText_work_name);
        iconSpinner = mRootView.findViewById(R.id.spinner_ic);
        tagSpinner = mRootView.findViewById(R.id.spinner_has_tags);
        tag1EditText = mRootView.findViewById(R.id.editText_work_tag1);
        tag2EditText = mRootView.findViewById(R.id.editText_work_tag2);
        progressEditText = mRootView.findViewById(R.id.editText_work_progress);
        importanceSpinner = mRootView.findViewById(R.id.spinner_work_importance);
        deadlineEditText = mRootView.findViewById(R.id.editText_work_deadline);
        descriptionEditText = null != getActivity() ?
                getActivity().findViewById(R.id.work_description_text) : null;
        // set request header
        TextView request_header = mRootView.findViewById(R.id.request_header);
        if (request_type.equals("create")){
            request_header.setText(R.string.create_work);
        } else {
            request_header.setText(R.string.edit_work);
        }
        // set onClickListener
        View button_cancel = mRootView.findViewById(R.id.button_request_cancel);
        View button_search = mRootView.findViewById(R.id.button_request_done);
        button_cancel.setOnClickListener(this);
        button_search.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChangeListener) {
            mChangeListener = (ChangeListener) context;
        } else {
            try {
                DashboardFragment fragment = (DashboardFragment) getParentFragmentManager()
                                                                .getFragments().get(0);
                if (fragment instanceof ChangeListener) {
                    mChangeListener = (ChangeListener) fragment;
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public void onCancelClicked() {
        resetForm();
        dismiss();
    }

    public void onDoneClicked() {
        // check if the form is valid
        if (isValidForm()) {
            // initialize new work
            String title = getSelectedName();
            String icon = getSelectedIcon();
            List<String> tags = getSelectedTags();
            int progress = getSelectedProgress();
            int importance = getSelectedImportance();
            String deadline = getSelectedDeadline();
            String description = getSelectedDescription();
            title = title == null ? mWork.getTitle() : title;
            icon = icon == null ? mWork.getIcon() : icon;
            tags = getSelectedHasTags() && tags.size() == 0 ? mWork.getTags() : tags;
            progress = progress == -1 ? mWork.getProgress() : progress;
            importance = importance == -1 ? mWork.getImportance() : importance;
            deadline = deadline == null ? mWork.getDeadline() : deadline;
            Work newWork = new Work(email, title, icon, progress, importance,
                    deadline, tags, description);
            // perform desired operation
            if (request_type.equals("create")){
                DBUtil.work_insert(mFirestore, newWork);
            } else {
                DBUtil.work_update(mFirestore, workId, newWork);
            }
            // inform change
            if (mChangeListener != null) {
                mChangeListener.onChange(newWork);
            }
            resetForm();
            dismiss();
        } else {
            // display invalid input message
            Toast invalidToast = Toast.makeText(getContext(),
                    R.string.invalid_input,
                    Toast.LENGTH_LONG);
            invalidToast.show();
        }
    }

    @Nullable
    private String getSelectedName() {
        String selected = nameEditText.getText().toString();
        return selected.equals("") ? null : selected;
    }

    @Nullable
    private String getSelectedIcon() {
        String selected = (String) iconSpinner.getSelectedItem();
        if (getString(R.string.icon_default).equals(selected)) {
            return "default";
        } else if (getString(R.string.icon_exam).equals(selected)) {
            return "exam";
        } else if (getString(R.string.icon_homework).equals(selected)) {
            return "homework";
        } else if (getString(R.string.icon_laboratory).equals(selected)) {
            return "laboratory";
        } else if (getString(R.string.icon_meeting).equals(selected)) {
            return "meeting";
        } else if (getString(R.string.icon_payment).equals(selected)) {
            return "payment";
        } else if (getString(R.string.icon_presentation).equals(selected)) {
            return "presentation";
        } else if (getString(R.string.icon_project).equals(selected)) {
            return "project";
        } else if (getString(R.string.icon_reading).equals(selected)) {
            return "reading";
        } else return null;
    }

    private boolean getSelectedHasTags() {
        String selected = (String) tagSpinner.getSelectedItem();
        return getString(R.string.hasTags_true).equals(selected);
    }

    private List<String> getSelectedTags() {
        List<String> selected = new ArrayList<>();
        String selected_1 = tag1EditText.getText().toString();
        String selected_2 = tag2EditText.getText().toString();
        if (!selected_1.equals(""))
            selected.add(selected_1);
        if (!selected_2.equals(""))
            selected.add(selected_2);
        return selected;
    }

    private int getSelectedProgress() {
        int progress;
        String selected = progressEditText.getText().toString();
        try {
            progress = Integer.parseInt(selected);
        } catch (NumberFormatException ex) {
            return -1;
        }
        return progress >= 0 && progress <= 100 ? progress : -1;
    }

    private int getSelectedImportance() {
        String selected = (String) importanceSpinner.getSelectedItem();
        if (getString(R.string.importance_1).equals(selected)) {
            return 1;
        } else if (getString(R.string.importance_2).equals(selected)) {
            return 2;
        } else if (getString(R.string.importance_3).equals(selected)) {
            return 3;
        } else return -1;
    }

    @Nullable
    private String getSelectedDeadline() {
        String selected = deadlineEditText.getText().toString();
        return selected.equals("") ? null : selected;
    }

    private String getSelectedDescription() {
        return descriptionEditText == null ? "" : descriptionEditText.getText().toString();
    }

    public void resetForm() {
        nameEditText.setText("");
        iconSpinner.setSelection(0);
        tagSpinner.setSelection(0);
        tag1EditText.setText("");
        tag2EditText.setText("");
        progressEditText.setText("");
        importanceSpinner.setSelection(0);
        deadlineEditText.setText("");
    }

    private boolean isValidForm() {
        // check request type
        if (!request_type.equals("create") && !request_type.equals("edit"))
            return false;
        if (request_type.equals("create")){
            // check name
            if (getSelectedName() == null)
                return false;
            // check icon
            if (getSelectedIcon() == null)
                return false;
            // check tag
            if (getSelectedHasTags() && getSelectedTags().size() == 0)
                return false;
            // check progress
            int progress = getSelectedProgress();
            if (progress < 0 || progress > 100)
                return false;
            // check importance
            if (getSelectedImportance() == -1)
                return false;
        }
        // check tag
        if (!getSelectedHasTags() && getSelectedTags().size() > 0)
            return false;
        // check deadline
        String deadline = getSelectedDeadline();
        if ((request_type.equals("edit") && deadline != null) ||
                request_type.equals("create")) {
            try {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime.parse(deadline, dateFormat);
            } catch (NullPointerException | DateTimeParseException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_request_cancel) {
            onCancelClicked();
        } else if (id == R.id.button_request_done) {
            onDoneClicked();
        }
    }
}
