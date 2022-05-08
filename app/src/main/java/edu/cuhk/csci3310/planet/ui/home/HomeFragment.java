package edu.cuhk.csci3310.planet.ui.home;

import static android.content.Context.MODE_PRIVATE;
import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.WorkDetailActivity;
import edu.cuhk.csci3310.planet.adapter.WorkAdapter;
import edu.cuhk.csci3310.planet.databinding.FragmentHomeBinding;
import edu.cuhk.csci3310.planet.model.Filters;
import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.ui.dialog.FilterDialogFragment;
import edu.cuhk.csci3310.planet.util.DBUtil;
import edu.cuhk.csci3310.planet.util.WorkUtil;

/**
 * Fragment containing the home page.
 */
public class HomeFragment extends Fragment implements
        View.OnClickListener,
        FilterDialogFragment.FilterListener,
        WorkAdapter.OnWorkSelectedListener {

    public static final String Id_MESSAGE = "edu.cuhk.csci3310.planet.id.MESSAGE";
    public static final String WORK_MESSAGE = "edu.cuhk.csci3310.planet.work.MESSAGE";
    private HomeViewModel mHomeViewModel;
    private FirebaseFirestore mFirestore;
    private WorkAdapter mAdapter;
    private Query mQuery;
    private FragmentHomeBinding binding;
    private FilterDialogFragment mFilterDialog;
    private RecyclerView mWorksRecycler;
    private ViewGroup mEmptyView;
    private TextView mCurrentSearchView;
    private TextView mCurrentSortByView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        // initialize view model
        mHomeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // initialize Firestore
        mFirestore = DBUtil.initFirestore();
        mQuery = mFirestore.collection("works")
                .orderBy("deadline", Query.Direction.ASCENDING);
        // get shared preference
        String sharedPrefFile = "edu.cuhk.csci3310.planet";
        if (getActivity() != null) {
            SharedPreferences mPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
            mHomeViewModel.setReminderTime(mPreferences.getInt("reminder_time", -1));
        }
        // initialize view
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mCurrentSearchView = root.findViewById(R.id.text_current_search);
        mCurrentSortByView = root.findViewById(R.id.text_current_sort_by);
        mWorksRecycler = root.findViewById(R.id.recycler_works);
        mEmptyView = root.findViewById(R.id.view_empty);
        initRecyclerView();
        // set onClickListener
        View filter_bar = root.findViewById(R.id.filter_bar);
        View filter_clear = root.findViewById(R.id.button_clear_filter);
        filter_bar.setOnClickListener(this);
        filter_clear.setOnClickListener(this);
        // initialize fragment
        mFilterDialog = new FilterDialogFragment();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // apply filters
        onFilter(mHomeViewModel.getFilters());
        // start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mHomeViewModel.getIsSignedIn()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                mHomeViewModel.setEmail(user.getEmail());
                mHomeViewModel.setIsSignedIn(true);
                onFilter(mHomeViewModel.getFilters());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.filter_bar) {
            onFilterClicked();
        } else if (id == R.id.button_clear_filter) {
            onClearFilterClicked();
        }
    }

    @Override
    public void onWorkSelected(DocumentSnapshot workSnapshot) {
        // go to the details page for the selected work
        String id = workSnapshot.getId();
        Work work = workSnapshot.toObject(Work.class);
        Intent intent = new Intent(this.getContext(), WorkDetailActivity.class);
        intent.putExtra(Id_MESSAGE, id);
        intent.putExtra(WORK_MESSAGE, work);
        if (this.getContext() != null) {
            this.getContext().startActivity(intent);
        }
    }

    @Override
    public void onFilter(Filters filters) {
        // construct basic query
        Query query = mFirestore.collection("works");
        // email filter
        String email = mHomeViewModel.getEmail();
        query = query.whereEqualTo("email", email);
        // name filter
        if (filters.hasName()) {
            query = query.whereEqualTo("title", filters.getName());
        }
        // tag filter
        if (filters.hasTag()) {
            query = query.whereArrayContains("tags", filters.getTag());
        }
        // deadline filter
        if (filters.hasDeadline()) {
            switch(filters.getDeadline_constraint()) {
                case -1:
                    query = query.whereLessThanOrEqualTo("deadline", filters.getDeadline());
                    break;
                case 0:
                    query = query.whereGreaterThanOrEqualTo("deadline", filters.getDeadline());
                    query = query.whereLessThan("deadline", filters.getDeadline() + ";");
                    break;
                case 1:
                    query = query.whereGreaterThanOrEqualTo("deadline", filters.getDeadline());
                    break;
                default:
                    break;
            }
        }
        // progress filter
        if (filters.hasProgress()) {
            query = query.whereEqualTo("progress", filters.getProgress());
        }
        // importance filter
        if (filters.hasImportance()) {
            query = query.whereEqualTo("importance", filters.getImportance());
        }
        // show past filter
        if (!filters.getShowPast()) {
            String currentTime = WorkUtil.getCurrentTimeString();
            query = query.whereGreaterThanOrEqualTo("deadline", currentTime);
        }
        // sort with deadlines by ascending order
        query = query.orderBy(Work.FIELD_DEADLINE, Query.Direction.ASCENDING);
        // update the query
        mQuery = query.limit(100);
        mAdapter.setQuery(query);
        // set header
        mCurrentSearchView.setText(Html.fromHtml(
                filters.getSearchDescription(this.getContext()), FROM_HTML_MODE_COMPACT));
        mCurrentSortByView.setText(getString(R.string.sorted_by_deadline));
        // save filters
        mHomeViewModel.setFilters(filters);
    }

    public void onFilterClicked() {
        // show the dialog containing filter options
        if (!mFilterDialog.isAdded()) {
            mFilterDialog.show(getParentFragmentManager(), null);
        }
    }

    public void onClearFilterClicked() {
        // reset the filter as default
        mFilterDialog.setShouldResetFilter(true);
        onFilter(Filters.getDefault());
    }

    private void initRecyclerView() {
        mAdapter = new WorkAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // show/hide content if the query returns empty
                if (getItemCount() == 0) {
                    mWorksRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mWorksRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
            @Override
            protected void onError(FirebaseFirestoreException e) {
                // show a snackbar on errors
                if (getView() != null) {
                    Snackbar.make(getView().findViewById(android.R.id.content),
                            "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
                }
            }
        };
        mWorksRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mWorksRecycler.setAdapter(mAdapter);
    }
}