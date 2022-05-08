package edu.cuhk.csci3310.planet.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.model.Filters;
import edu.cuhk.csci3310.planet.ui.home.HomeFragment;

/**
 * Dialog Fragment containing filter form.
 */
public class FilterDialogFragment extends DialogFragment implements
        View.OnClickListener {

    public FilterDialogFragment() {
        // required empty public constructor
    }

    public interface FilterListener {
        void onFilter(Filters filters);
    }

    private View mRootView;
    private EditText nameEditText;
    private EditText tagEditText;
    private EditText progressEditText;
    private Spinner importanceSpinner;
    private EditText deadlineEditText;
    private Spinner deadlineConstraint;
    private Spinner showPastSpinner;
    private FilterListener mFilterListener;
    private boolean shouldResetFilter = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // initialize view
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);
        nameEditText = mRootView.findViewById(R.id.editText_name);
        tagEditText = mRootView.findViewById(R.id.editText_tag);
        progressEditText = mRootView.findViewById(R.id.editText_progress);
        importanceSpinner = mRootView.findViewById(R.id.spinner_importance);
        deadlineEditText = mRootView.findViewById(R.id.editText_deadline);
        deadlineConstraint = mRootView.findViewById(R.id.constraint_deadline);
        showPastSpinner = mRootView.findViewById(R.id.spinner_showPast);
        // set onClickListener
        View button_cancel = mRootView.findViewById(R.id.button_cancel);
        View button_search = mRootView.findViewById(R.id.button_search);
        button_cancel.setOnClickListener(this);
        button_search.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            HomeFragment fragment = (HomeFragment) getParentFragmentManager().getFragments().get(0);
            if (fragment instanceof FilterListener) {
                mFilterListener = (FilterListener) fragment;
            }
        } catch (Exception ignored) {
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
        if (shouldResetFilter) {
            resetFilters();
            shouldResetFilter = false;
        }
    }

    public void onCancelClicked() {
        resetFilters();
        dismiss();
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }
        dismiss();
    }

    @Nullable
    private String getSelectedName() {
        String selected = nameEditText.getText().toString();
        return selected.equals("") ? null : selected;
    }

    @Nullable
    private String getSelectedTag() {
        String selected = tagEditText.getText().toString();
        return selected.equals("") ? null : selected;
    }

    private int getSelectedProgress() {
        String selected = progressEditText.getText().toString();
        try {
            return Integer.parseInt(selected);
        } catch (NumberFormatException ex) {
            return -1;
        }
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

    private int getSelectedDeadline_constraint() {
        String selected = (String) deadlineConstraint.getSelectedItem();
        if (getString(R.string.constraint_le).equals(selected)) {
            return -1;
        } else if (getString(R.string.constraint_eq).equals(selected)) {
            return 0;
        } else if (getString(R.string.constraint_ge).equals(selected)) {
            return 1;
        } else return -2;
    }

    private boolean getSelectedShowPast() {
        String selected = (String) showPastSpinner.getSelectedItem();
        return getString(R.string.showPast_true).equals(selected);
    }

    public void resetFilters() {
        if (mRootView != null) {
            nameEditText.setText("");
            tagEditText.setText("");
            deadlineEditText.setText("");
            deadlineConstraint.setSelection(0);
            progressEditText.setText("");
            importanceSpinner.setSelection(0);
            showPastSpinner.setSelection(0);
        }
    }

    public void setShouldResetFilter(boolean shouldResetFilter) {
        this.shouldResetFilter = shouldResetFilter;
    }

    public Filters getFilters() {
        Filters filters = new Filters();
        if (mRootView != null) {
            filters.setName(getSelectedName());
            filters.setTag(getSelectedTag());
            filters.setDeadline(getSelectedDeadline());
            filters.setDeadline_constraint(getSelectedDeadline_constraint());
            filters.setProgress(getSelectedProgress());
            filters.setImportance(getSelectedImportance());
            filters.setShowPast(getSelectedShowPast());
        }
        return filters;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_cancel) {
            onCancelClicked();
        } else if (id == R.id.button_search) {
            onSearchClicked();
        }
    }
}
