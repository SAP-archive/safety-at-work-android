package demo.sap.safetyandroid.mdui.historydevicesstatus;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.mdui.BundleKeys;
import demo.sap.safetyandroid.mdui.EntitySetListActivity;
import demo.sap.safetyandroid.mdui.EntitySetListActivity.EntitySetName;
import demo.sap.safetyandroid.mdui.InterfacedFragment;
import demo.sap.safetyandroid.mdui.UIConstants;
import demo.sap.safetyandroid.mdui.EntityKeyUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import demo.sap.safetyandroid.service.SAPServiceManager;
import demo.sap.safetyandroid.repository.OperationResult;
import demo.sap.safetyandroid.viewmodel.EntityViewModelFactory;
import demo.sap.safetyandroid.viewmodel.historydevicesstatustype.HistoryDevicesStatusTypeViewModel;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;
import com.sap.cloud.android.odata.v2.HistoryDevicesStatusType;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.EntityValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class HistoryDevicesStatusListFragment extends InterfacedFragment<HistoryDevicesStatusType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDevicesStatusActivity.class);

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private SAPServiceManager sapServiceManager;

    /**
     * List adapter to be used with RecyclerView containing all instances of historyDevicesStatus
     */
    private HistoryDevicesStatusTypeListAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    /**
     * View model of the entity type
     */
    private HistoryDevicesStatusTypeViewModel viewModel;

    private ActionMode actionMode;
    private Boolean isInActionMode = false;
    private List<Integer> selectedItems = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshLayout.setRefreshing(true);
                refreshListData();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTitle = getString(EntitySetListActivity.EntitySetName.HistoryDevicesStatus.getTitleId());
        menu = R.menu.itemlist_menu;
        setHasOptionsMenu(true);
        if( savedInstanceState != null ) {
            isInActionMode = savedInstanceState.getBoolean("ActionMode");
        }

        sapServiceManager = ((SAPWizardApplication) currentActivity.getApplication()).getSAPServiceManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null) {
            objectHeader.setVisibility(View.GONE);
        }
        return inflater.inflate(R.layout.fragment_entityitem_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(this.menu, menu);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        currentActivity.setTitle(activityTitle);
        RecyclerView recyclerView = currentActivity.findViewById(R.id.item_list);
        if (recyclerView == null) throw new AssertionError();
        this.adapter = new HistoryDevicesStatusTypeListAdapter(currentActivity);
        recyclerView.setAdapter(adapter);

        setupRefreshLayout();
        refreshLayout.setRefreshing(true);

        navigationPropertyName = currentActivity.getIntent().getStringExtra("navigation");
        parentEntityData = currentActivity.getIntent().getParcelableExtra("parent");

        FloatingActionButton floatButton = currentActivity.findViewById(R.id.fab);
        if (floatButton != null) {
            if (navigationPropertyName != null && parentEntityData != null) {
                floatButton.hide();
            } else {
                floatButton.setOnClickListener((v) -> {
                    listener.onFragmentStateChange(UIConstants.EVENT_CREATE_NEW_ITEM, null);
                });
            }
        }

        sapServiceManager.openODataStore(() -> {
            prepareViewModel();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ActionMode", isInActionMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListData();
    }

    /** Initializes the view model and add observers on it */
    private void prepareViewModel() {
        if( navigationPropertyName != null && parentEntityData != null ) {
            viewModel = new ViewModelProvider(currentActivity, new EntityViewModelFactory(currentActivity.getApplication(), navigationPropertyName, parentEntityData))
                .get(HistoryDevicesStatusTypeViewModel.class);
        } else {
            viewModel = new ViewModelProvider(currentActivity).get(HistoryDevicesStatusTypeViewModel.class);
            viewModel.initialRead(this::showError);
        }

        viewModel.getObservableItems().observe(getViewLifecycleOwner(), historyDevicesStatus -> {
            if (historyDevicesStatus != null) {
                adapter.setItems(historyDevicesStatus);

                HistoryDevicesStatusType item = containsItem(historyDevicesStatus, viewModel.getSelectedEntity().getValue());
                if (item == null) {
                    item = historyDevicesStatus.isEmpty() ? null : historyDevicesStatus.get(0);
                }

                if (item == null) {
                    hideDetailFragment();
                } else {
                    viewModel.setInFocusId(adapter.getItemIdForHistoryDevicesStatusType(item));
                    if (currentActivity.getResources().getBoolean(R.bool.two_pane)) {
                        viewModel.setSelectedEntity(item);
                        if (!isInActionMode && !((HistoryDevicesStatusActivity) currentActivity).isNavigationDisabled) {
                            listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            refreshLayout.setRefreshing(false);
        });

        viewModel.getReadResult().observe(getViewLifecycleOwner(), state -> {
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), result -> {
            onDeleteComplete(result);
        });
    }

    /** Searches 'item' in the refreshed list, if found, returns the one in list */
    private HistoryDevicesStatusType containsItem(List<HistoryDevicesStatusType> items, HistoryDevicesStatusType item) {
        HistoryDevicesStatusType found = null;
        if( item != null ) {
            for( HistoryDevicesStatusType entity: items ) {
                if( adapter.getItemIdForHistoryDevicesStatusType(entity) == adapter.getItemIdForHistoryDevicesStatusType(item)) {
                    found = entity;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Hides the detail fragment.
     */
    private void hideDetailFragment() {
        Fragment detailFragment = currentActivity.getSupportFragmentManager().findFragmentByTag(UIConstants.DETAIL_FRAGMENT_TAG);
        if( detailFragment != null ) {
            currentActivity.getSupportFragmentManager().beginTransaction()
                .remove(detailFragment).commit();
        }
        if( secondaryToolbar != null ) {
            secondaryToolbar.getMenu().clear();
            secondaryToolbar.setTitle("");
        }
        View objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null) {
            objectHeader.setVisibility(View.GONE);
        }
    }

    /** Callback function for delete operation */
    private void onDeleteComplete(@NonNull OperationResult<HistoryDevicesStatusType> result) {
        if( progressBar != null ) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        viewModel.removeAllSelected();
        if (actionMode != null) {
            actionMode.finish();
            isInActionMode = false;
        }

        if (result.getError() != null) {
            handleDeleteError();
        } else {
            refreshListData();
        }
    }

    /** Refreshes the list data */
    void refreshListData() {
        if (navigationPropertyName != null && parentEntityData != null) {
            viewModel.refresh((EntityValue) parentEntityData, navigationPropertyName);
        } else {
            viewModel.refresh();
        }
        adapter.notifyDataSetChanged();
    }

    /** Sets the selected item id into view model */
    private HistoryDevicesStatusType setItemIdSelected(int itemId) {
        LiveData<List<HistoryDevicesStatusType>> liveData = viewModel.getObservableItems();
        List<HistoryDevicesStatusType> historyDevicesStatus = liveData.getValue();
        if (historyDevicesStatus != null && historyDevicesStatus.size() > 0) {
            viewModel.setInFocusId(adapter.getItemIdForHistoryDevicesStatusType(historyDevicesStatus.get(itemId)));
            return historyDevicesStatus.get(itemId);
        }
        return null;
    }

    /** Sets up the refresh layout */
    private void setupRefreshLayout() {
        refreshLayout = currentActivity.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(UIConstants.FIORI_STANDARD_THEME_GLOBAL_DARK_BASE);
        refreshLayout.setProgressBackgroundColorSchemeColor(UIConstants.FIORI_STANDARD_THEME_BACKGROUND);
        refreshLayout.setOnRefreshListener(this::refreshListData);
    }

    /** Callback function to handle deletion error */
    private void handleDeleteError() {
        showError(getResources().getString(R.string.delete_failed_detail));
        refreshLayout.setRefreshing(false);
    }

    /**
     * Represents the action mode of the list.
     */
    public class HistoryDevicesStatusListActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            isInActionMode = true;
            FloatingActionButton fab = currentActivity.findViewById(R.id.fab);
            if (fab != null) {
                fab.hide();
            }
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.itemlist_view_options, menu);
            hideDetailFragment();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.update_item:
                    HistoryDevicesStatusType historyDevicesStatusTypeEntity = viewModel.getSelected(0);
                    if (viewModel.numberOfSelected() == 1 && historyDevicesStatusTypeEntity != null) {
                        isInActionMode = false;
                        actionMode.finish();
                        viewModel.setSelectedEntity(historyDevicesStatusTypeEntity);
                        if( currentActivity.getResources().getBoolean(R.bool.two_pane)) {
                            listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, historyDevicesStatusTypeEntity);
                        }
                        listener.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, historyDevicesStatusTypeEntity);
                    }
                    return true;
                case R.id.delete_item:
                    listener.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION, null);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            isInActionMode = false;
            if (!(navigationPropertyName != null && parentEntityData != null)) {
                FloatingActionButton fab = currentActivity.findViewById(R.id.fab);
                if (fab != null) {
                    fab.show();
                }
            }
            selectedItems.clear();
            viewModel.removeAllSelected();
            refreshListData();
        }
    }

    /**
     * List adapter to be used with RecyclerView. It contains the set of historyDevicesStatus.
     */
    public class HistoryDevicesStatusTypeListAdapter extends RecyclerView.Adapter<HistoryDevicesStatusTypeListAdapter.ViewHolder> {

        private Context context;

        /** Entire list of HistoryDevicesStatusType collection */
        private List<HistoryDevicesStatusType> historyDevicesStatus;

        /** RecyclerView this adapter is associate with */
        private RecyclerView recyclerView;

        /** Flag to indicate whether we have checked retained selected historyDevicesStatus */
        private boolean checkForSelectedOnCreate = false;

        public HistoryDevicesStatusTypeListAdapter(Context context) {
            this.context = context;
            this.recyclerView = currentActivity.findViewById(R.id.item_list);
            if (this.recyclerView == null) throw new AssertionError();
            setHasStableIds(true);
        }

        /**
         * Use DiffUtil to calculate the difference and dispatch them to the adapter
         * Note: Please use background thread for calculation if the list is large to avoid blocking main thread
         */
        @WorkerThread
        public void setItems(@NonNull List<HistoryDevicesStatusType> currentHistoryDevicesStatus) {
            if (historyDevicesStatus == null) {
                historyDevicesStatus = new ArrayList<>(currentHistoryDevicesStatus);
                notifyItemRangeInserted(0, currentHistoryDevicesStatus.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return historyDevicesStatus.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return currentHistoryDevicesStatus.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return historyDevicesStatus.get(oldItemPosition).getEntityKey().toString().equals(
                                currentHistoryDevicesStatus.get(newItemPosition).getEntityKey().toString());
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        HistoryDevicesStatusType historyDevicesStatusTypeEntity = historyDevicesStatus.get(oldItemPosition);
                        return !historyDevicesStatusTypeEntity.isUpdated() && currentHistoryDevicesStatus.get(newItemPosition).equals(historyDevicesStatusTypeEntity);
                    }

                    @Nullable
                    @Override
                    public Object getChangePayload(final int oldItemPosition, final int newItemPosition) {
                        return super.getChangePayload(oldItemPosition, newItemPosition);
                    }
                });
                historyDevicesStatus.clear();
                historyDevicesStatus.addAll(currentHistoryDevicesStatus);
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public final long getItemId(int position) {
            return getItemIdForHistoryDevicesStatusType(historyDevicesStatus.get(position));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_entityitem_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

            checkForRetainedSelection();

            final HistoryDevicesStatusType historyDevicesStatusTypeEntity = historyDevicesStatus.get(holder.getAdapterPosition());
            DataValue dataValue = historyDevicesStatusTypeEntity.getDataValue(HistoryDevicesStatusType.deviceID);
            if (dataValue != null) {
                holder.masterPropertyValue = dataValue.toString();
            }
            populateObjectCell(holder, historyDevicesStatusTypeEntity);

            boolean isActive = getItemIdForHistoryDevicesStatusType(historyDevicesStatusTypeEntity) == viewModel.getInFocusId();
            if (isActive) {
                setItemIdSelected(holder.getAdapterPosition());
            }
            boolean isHistoryDevicesStatusTypeSelected = viewModel.selectedContains(historyDevicesStatusTypeEntity);
            setViewBackground(holder.objectCell, isHistoryDevicesStatusTypeSelected, isActive);

            holder.view.setOnLongClickListener(new onActionModeStartClickListener(holder));
            setOnClickListener(holder, historyDevicesStatusTypeEntity);

            setOnCheckedChangeListener(holder, historyDevicesStatusTypeEntity);
            holder.setSelected(isHistoryDevicesStatusTypeSelected);
            setDetailImage(holder, historyDevicesStatusTypeEntity);
        }

        /**
         * Check to see if there are an retained selected historyDevicesStatusTypeEntity on start.
         * This situation occurs when a rotation with selected historyDevicesStatus is triggered by user.
         */
        private void checkForRetainedSelection() {
            if (!checkForSelectedOnCreate) {
                checkForSelectedOnCreate = true;
                if (viewModel.numberOfSelected() > 0) {
                    manageActionModeOnCheckedTransition();
                }
            }
        }

        /**
         * If there are selected historyDevicesStatus via long press, clear them as click and long press are mutually exclusive
         * In addition, since we are clearing all selected historyDevicesStatus via long press, finish the action mode.
         */
        private void resetSelected() {
            if (viewModel.numberOfSelected() > 0) {
                viewModel.removeAllSelected();
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            }
        }

        /**
         * Attempt to locate previously clicked view and reset its background
         * Reset view model's inFocusId
         */
        private void resetPreviouslyClicked() {
            long inFocusId = viewModel.getInFocusId();
            ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForItemId(inFocusId);
            if (viewHolder != null) {
                setViewBackground(viewHolder.objectCell, viewHolder.isSelected, false);
            } else {
                viewModel.refresh();
            }
        }

        private void processClickAction(@NonNull ViewHolder viewHolder, @NonNull HistoryDevicesStatusType historyDevicesStatusTypeEntity) {
            resetPreviouslyClicked();
            setViewBackground(viewHolder.objectCell, false, true);
            viewModel.setInFocusId(getItemIdForHistoryDevicesStatusType(historyDevicesStatusTypeEntity));
        }

        /**
         * Set ViewHolder's view onClickListener
         *
         * @param holder
         * @param historyDevicesStatusTypeEntity associated with this ViewHolder
         */
        private void setOnClickListener(@NonNull ViewHolder holder, @NonNull HistoryDevicesStatusType historyDevicesStatusTypeEntity) {
            holder.view.setOnClickListener(view -> {
                boolean isNavigationDisabled = ((HistoryDevicesStatusActivity) currentActivity).isNavigationDisabled;
                if(isNavigationDisabled) {
                    Toast.makeText(currentActivity, "Please save your changes first...", Toast.LENGTH_LONG).show();
                } else {
                    resetSelected();
                    resetPreviouslyClicked();
                    processClickAction(holder, historyDevicesStatusTypeEntity);
                    viewModel.setSelectedEntity(historyDevicesStatusTypeEntity);
                    listener.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, historyDevicesStatusTypeEntity);
                }
            });
        }

        /**
         * Represents the listener to start the action mode
         */
        public class onActionModeStartClickListener implements View.OnClickListener, View.OnLongClickListener {

            ViewHolder holder;

            public onActionModeStartClickListener(@NonNull ViewHolder viewHolder) {
                this.holder = viewHolder;
            }

            @Override
            public void onClick(View view) {
                onAnyKindOfClick();
            }

            @Override
            public boolean onLongClick(View view) {
                return onAnyKindOfClick();
            }

            /** callback function for both normal and long click on an entity */
            private boolean onAnyKindOfClick() {
                boolean isNavigationDisabled = ((HistoryDevicesStatusActivity) currentActivity).isNavigationDisabled;
                if( isNavigationDisabled ) {
                    Toast.makeText(currentActivity, "Please save your changes first...", Toast.LENGTH_LONG).show();
                } else {
                    if (!isInActionMode) {
                        actionMode = ((AppCompatActivity) currentActivity).startSupportActionMode(new HistoryDevicesStatusListActionMode());
                        adapter.notifyDataSetChanged();
                    }
                    holder.setSelected(!holder.isSelected);
                }
                return true;
            }
        }

        /** sets the detail image to the given <code>viewHolder</code> */
        private void setDetailImage(@NonNull ViewHolder viewHolder, @NonNull HistoryDevicesStatusType historyDevicesStatusTypeEntity) {
            if (isInActionMode) {
                int drawable;
                if (viewHolder.isSelected) {
                    drawable = R.drawable.ic_check_circle_black_24dp;
                } else {
                    drawable = R.drawable.ic_uncheck_circle_black_24dp;
                }
                viewHolder.objectCell.prepareDetailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context)
                        .load(getResources().getDrawable(drawable, null))
                        .apply(new RequestOptions().fitCenter())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(viewHolder.objectCell.prepareDetailImageView());
            } else if (viewHolder.masterPropertyValue != null && !viewHolder.masterPropertyValue.isEmpty()) {
                viewHolder.objectCell.setDetailImageCharacter(viewHolder.masterPropertyValue.substring(0, 1));
            } else {
                viewHolder.objectCell.setDetailImageCharacter("?");
            }
        }

        /**
         * Set ViewHolder's CheckBox onCheckedChangeListener
         *
         * @param holder
         * @param historyDevicesStatusTypeEntity associated with this ViewHolder
         */
        private void setOnCheckedChangeListener(@NonNull ViewHolder holder, @NonNull HistoryDevicesStatusType historyDevicesStatusTypeEntity) {
            holder.checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    viewModel.addSelected(historyDevicesStatusTypeEntity);
                    manageActionModeOnCheckedTransition();
                    resetPreviouslyClicked();
                } else {
                    viewModel.removeSelected(historyDevicesStatusTypeEntity);
                    manageActionModeOnUncheckedTransition();
                }
                setViewBackground(holder.objectCell, viewModel.selectedContains(historyDevicesStatusTypeEntity), false);
                setDetailImage(holder, historyDevicesStatusTypeEntity);
            });
        }

        /*
         * Start Action Mode if it has not been started
         * This is only called when long press action results in a selection. Hence action mode may
         * not have been started. Along with starting action mode, title will be set.
         * If this is an additional selection, adjust title appropriately.
         */
        private void manageActionModeOnCheckedTransition() {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) currentActivity).startSupportActionMode(new HistoryDevicesStatusListActionMode());
            }
            if (viewModel.numberOfSelected() > 1) {
                actionMode.getMenu().findItem(R.id.update_item).setVisible(false);
            }
            actionMode.setTitle(String.valueOf(viewModel.numberOfSelected()));
        }

        /*
         * This is called when one of the selected historyDevicesStatus has been de-selected
         * On this event, we will determine if update action needs to be made visible or
         * action mode should be terminated (no more selected)
         */
        private void manageActionModeOnUncheckedTransition() {
            switch (viewModel.numberOfSelected()) {
                case 1:
                    actionMode.getMenu().findItem(R.id.update_item).setVisible(true);
                    break;

                case 0:
                    if (actionMode != null) {
                        actionMode.finish();
                        actionMode = null;
                    }
                    return;

                default:
            }
            actionMode.setTitle(String.valueOf(viewModel.numberOfSelected()));
        }

        private void populateObjectCell(@NonNull ViewHolder viewHolder, @NonNull HistoryDevicesStatusType historyDevicesStatusTypeEntity) {

            DataValue dataValue = historyDevicesStatusTypeEntity.getDataValue(HistoryDevicesStatusType.deviceID);
            String masterPropertyValue = null;
            if (dataValue != null) {
                masterPropertyValue = dataValue.toString();
            }
            viewHolder.objectCell.setHeadline(masterPropertyValue);
            viewHolder.objectCell.setDetailImage(null);
            setDetailImage(viewHolder, historyDevicesStatusTypeEntity);

            viewHolder.objectCell.setSubheadline("Subheadline goes here");
            viewHolder.objectCell.setFootnote("Footnote goes here");
            if (masterPropertyValue == null || masterPropertyValue.isEmpty()) {
                viewHolder.objectCell.setIcon("?", 0);
            } else {
                viewHolder.objectCell.setIcon(masterPropertyValue.substring(0, 1), 0);
            }
            viewHolder.objectCell.setIcon(R.drawable.default_dot, 1, R.string.attachment_item_content_desc);
            viewHolder.objectCell.setIcon("!", 2);
        }

        /**
         * Set background of view to indicate historyDevicesStatusTypeEntity selection status
         * Selected and Active are mutually exclusive. Only one can be true
         *
         * @param view
         * @param isHistoryDevicesStatusTypeSelected - true if historyDevicesStatusTypeEntity is selected via long press action
         * @param isActive               - true if historyDevicesStatusTypeEntity is selected via click action
         */
        private void setViewBackground(@NonNull View view, boolean isHistoryDevicesStatusTypeSelected, boolean isActive) {
            boolean isMasterDetailView = currentActivity.getResources().getBoolean(R.bool.two_pane);
            if (isHistoryDevicesStatusTypeSelected) {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_selected));
            } else if (isActive && isMasterDetailView && !isInActionMode) {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_active));
            } else {
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.list_item_default));
            }
        }

        @Override
        public int getItemCount() {
            if (historyDevicesStatus == null) {
                return 0;
            } else {
                return historyDevicesStatus.size();
            }
        }

        /**
         * Computes a stable ID for each HistoryDevicesStatusType object for use to locate the ViewHolder
         *
         * @param historyDevicesStatusTypeEntity
         * @return an ID based on the primary key of HistoryDevicesStatusType
         */
        private long getItemIdForHistoryDevicesStatusType(HistoryDevicesStatusType historyDevicesStatusTypeEntity) {
            return historyDevicesStatusTypeEntity.getEntityKey().toString().hashCode();
        }

        /**
         * ViewHolder for RecyclerView.
         * Each view has a Fiori ObjectCell and a checkbox (used by long press)
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View view;

            public boolean isSelected;

            public String masterPropertyValue;

            /** Fiori ObjectCell to display historyDevicesStatusTypeEntity in list */
            public final ObjectCell objectCell;

            /** Checkbox for long press selection */
            public final CheckBox checkBox;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                objectCell = view.findViewById(R.id.content);
                checkBox = view.findViewById(R.id.cbx);
                isSelected = false;
            }

            public void setSelected(Boolean selected) {
                isSelected = selected;
                checkBox.setChecked(selected);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + objectCell.getDescription() + "'";
            }
        }
    }
}
