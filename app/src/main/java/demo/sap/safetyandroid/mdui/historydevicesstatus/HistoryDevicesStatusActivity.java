package demo.sap.safetyandroid.mdui.historydevicesstatus;

import android.content.Context;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import demo.sap.safetyandroid.R;
import com.sap.cloud.mobile.flowv2.core.DialogHelper;
import demo.sap.safetyandroid.mdui.BundleKeys;
import demo.sap.safetyandroid.mdui.EntitySetListActivity;
import demo.sap.safetyandroid.mdui.InterfacedFragment;
import demo.sap.safetyandroid.mdui.UIConstants;
import demo.sap.safetyandroid.viewmodel.historydevicesstatustype.HistoryDevicesStatusTypeViewModel;
import com.sap.cloud.android.odata.v2.HistoryDevicesStatusType;

/**
 * This activity handles three kind of {@link InterfacedFragment}s:
 * - {@link HistoryDevicesStatusCreateFragment}: to create or edit a(n) HistoryDevicesStatusType,
 * - {@link HistoryDevicesStatusDetailFragment}: to display the details of a(n) HistoryDevicesStatusType,
 * - {@link HistoryDevicesStatusListFragment}: to list HistoryDevicesStatus.
 *
 * The visibility of frames inside this activity depends on the width of the device. When screen provides at least 900dp
 * of width both masterFrame and detailFrame are visible. Only one frame is visible in smaller screen sizes.
 *
 * This activity is responsible to place, change and control visibilities of fragments. Fragments have no information
 * about other fragments, so when an user action occurs the fragment uses an interface to command the activity
 * what to do.
 */
public class HistoryDevicesStatusActivity extends AppCompatActivity implements InterfacedFragment.InterfacedFragmentListener<HistoryDevicesStatusType> {

    private static final String KEY_IS_NAVIGATION_DISABLED = "isNavigationDisabled";
    private static final String KEY_IS_NAVIGATION_FROM_HOME = "isNavigationFromHome";

    /** Flag to indicate whether both master and detail frames should be visible at the same time  */
    private Boolean isMasterDetailView;

    /** Flag to indicate whether requesting user confirmation before navigation is needed */
    protected boolean isNavigationDisabled = false;

    /** Flag to tell whether back action is from home click or or others */
    private boolean isConfirmDataLossFromHomeButton = false;

    /**
     * In this lifecycle state, some instance state variables gets loaded, {@link HistoryDevicesStatusListFragment} is
     * getting instantiated (which will load {@link HistoryDevicesStatusDetailFragment} if needed), visibilities of
     * master and detail frames gets set and the toolbar with buttons gets set.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMasterDetailView = getResources().getBoolean(R.bool.two_pane);
        setContentView(R.layout.activity_entityitem);

        if (savedInstanceState != null) {
            this.isNavigationDisabled = savedInstanceState.getBoolean(KEY_IS_NAVIGATION_DISABLED, false);
            this.isConfirmDataLossFromHomeButton = savedInstanceState.getBoolean(KEY_IS_NAVIGATION_FROM_HOME, false);
        } else {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.masterFrame, new HistoryDevicesStatusListFragment(), UIConstants.LIST_FRAGMENT_TAG)
                .commit();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** Let the Navigate Up button work like Back button */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IS_NAVIGATION_DISABLED, this.isNavigationDisabled);
        outState.putBoolean(KEY_IS_NAVIGATION_FROM_HOME, this.isConfirmDataLossFromHomeButton);
        super.onSaveInstanceState(outState);
    }

    /**
    * Handles backwards navigation when user presses back button.
    */
    @Override
    public void onBackPressed() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
        askUserBeforeNavigation();
    }

    /**
     * Every fragment handles its own OptionsMenu so the activity does not have to.
     *
     * @return false, because this activity does not handles OptionItems
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_home) {
            isConfirmDataLossFromHomeButton = true;
            askUserBeforeNavigation();
            return true;
        }
        return false;
    }

    @Override
    public void onFragmentStateChange(int eventId, @Nullable HistoryDevicesStatusType entity) {
        switch (eventId) {
            case UIConstants.EVENT_CREATE_NEW_ITEM:
                onCreateNewItem();
                break;
            case UIConstants.EVENT_ITEM_CLICKED:
                onItemClicked(entity);
                break;
            case UIConstants.EVENT_DELETION_COMPLETED:
                onDeleteComplete();
                break;
            case UIConstants.EVENT_EDIT_ITEM:
                onEditItem(entity);
                break;
            case UIConstants.EVENT_ASK_DELETE_CONFIRMATION:
                onConfirmDelete();
                break;
            case UIConstants.EVENT_BACK_NAVIGATION_CONFIRMED:
                isNavigationDisabled = false;
                if(isConfirmDataLossFromHomeButton) {
                    Intent intent = new Intent(this, EntitySetListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    super.onBackPressed();
                }
                break;
        }
    }

    /**
     * Opens the UI to create a new entity.
     */
    private void onCreateNewItem() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            String name = getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
            if (name == UIConstants.CREATE_FRAGMENT_TAG || name == UIConstants.MODIFY_FRAGMENT_TAG) {
                Toast.makeText(this, "Please save your changes first...", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Bundle arguments = new Bundle();
        arguments.putString(BundleKeys.OPERATION, UIConstants.OP_CREATE);
        HistoryDevicesStatusCreateFragment fragment = new HistoryDevicesStatusCreateFragment();
        fragment.setArguments(arguments);
        int containerId = isMasterDetailView ? R.id.detailFrame : R.id.masterFrame;
        getSupportFragmentManager().beginTransaction()
            .replace(containerId, fragment, UIConstants.CREATE_FRAGMENT_TAG)
            .addToBackStack(UIConstants.CREATE_FRAGMENT_TAG)
            .commit();
    }

    /**
     * Handles the item click event from the list.
     * 
     * @param entity The item clicked in the list 
     */
    private void onItemClicked(@Nullable HistoryDevicesStatusType entity) {
        if( !isMasterDetailView ) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.masterFrame, new HistoryDevicesStatusDetailFragment(), UIConstants.DETAIL_FRAGMENT_TAG)
                .addToBackStack(UIConstants.DETAIL_FRAGMENT_TAG)
                .commit();
        } else {
            Fragment detail = getSupportFragmentManager().findFragmentByTag(UIConstants.DETAIL_FRAGMENT_TAG);
            if( detail == null && entity != null ) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailFrame, new HistoryDevicesStatusDetailFragment(), UIConstants.DETAIL_FRAGMENT_TAG)
                    .commit();
                return;
            }

            if( detail != null && entity == null ) {
                getSupportFragmentManager().beginTransaction().remove(detail).commit();
                Toolbar sToolbar = findViewById(R.id.secondaryToolbar);
                if( sToolbar != null ) {
                    sToolbar.getMenu().clear();
                    sToolbar.setTitle("");
                }
            }
        }
    }

    /**
     * Handles the situation after an entity is deleted, hide the progress bar, go back if neccessary.
     */
    private void onDeleteComplete() {
        Toolbar secondaryToolbar = findViewById(R.id.secondaryToolbar);
        if( secondaryToolbar != null ) {
            secondaryToolbar.setVisibility(View.INVISIBLE);
        }
        if( !isMasterDetailView ) super.onBackPressed();
    }

    /**
     * Edits the <code>entity</code>.
     *
     * @param entity The entity to be edited.
     */
    private void onEditItem(@Nullable HistoryDevicesStatusType entity) {
        Bundle arguments = new Bundle();
        arguments.putString(BundleKeys.OPERATION, UIConstants.OP_UPDATE);
        HistoryDevicesStatusCreateFragment fragment = new HistoryDevicesStatusCreateFragment();
        fragment.setArguments(arguments);
        int containerId = isMasterDetailView ? R.id.detailFrame : R.id.masterFrame;
        getSupportFragmentManager().beginTransaction()
            .replace(containerId, fragment, UIConstants.MODIFY_FRAGMENT_TAG)
            .addToBackStack(UIConstants.MODIFY_FRAGMENT_TAG)
            .commit();
    }

    /**
     * Shows a dialog when user wants to delete an entity.
     */
    private void onConfirmDelete() {
        DeleteConfirmationDialogFragment dialog = new DeleteConfirmationDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), UIConstants.CONFIRMATION_FRAGMENT_TAG);
    }

    /**
     * Shows an AlertDialog when user wants to leave {@link HistoryDevicesStatusCreateFragment}.
     */
    public void askUserBeforeNavigation() {
        boolean editFragmentPresent = getSupportFragmentManager().findFragmentByTag(UIConstants.MODIFY_FRAGMENT_TAG) != null
            || getSupportFragmentManager().findFragmentByTag(UIConstants.CREATE_FRAGMENT_TAG) != null;
        if( editFragmentPresent && isNavigationDisabled ) {
            ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), UIConstants.CONFIRMATION_FRAGMENT_TAG);
        } else {
            onFragmentStateChange(UIConstants.EVENT_BACK_NAVIGATION_CONFIRMED,null);
        }
    }

    /**
     * Represents the confirmation dialog fragment when users tries to leave the create or edit
     * fragment to prevent data loss.
     */
    public static class ConfirmationDialogFragment extends DialogFragment {
        public ConfirmationDialogFragment() {
            super();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogStyle));
            builder.setTitle(R.string.before_navigation_dialog_title);
            builder.setMessage(R.string.before_navigation_dialog_message);
            builder.setPositiveButton(R.string.before_navigation_dialog_positive_button, (dialog, which) -> {
                ((HistoryDevicesStatusActivity)getActivity()).onFragmentStateChange(UIConstants.EVENT_BACK_NAVIGATION_CONFIRMED, null);
            });
            builder.setNegativeButton(R.string.before_navigation_dialog_negative_button, (dialog, which) -> {

            });
            return builder.create();
        }
    }

    /**
     * Represents the delete confirmation dialog fragment when users tries to delete an entity or entities
     */
    public static class DeleteConfirmationDialogFragment extends DialogFragment {
        public DeleteConfirmationDialogFragment() {
            super();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogStyle));
            HistoryDevicesStatusTypeViewModel viewModel = new ViewModelProvider(getActivity()).get(HistoryDevicesStatusTypeViewModel.class);
            if (viewModel.numberOfSelected() > 1) {
                builder.setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_more_items);
            } else {
                builder.setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_one_item);
            }

            builder.setPositiveButton(R.string.delete, (dialog,which) -> {
                try {
                    View progressBar = getActivity().findViewById(R.id.indeterminateBar);
                    if( progressBar != null ) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    if(viewModel.numberOfSelected() == 0) {
                        viewModel.addSelected(viewModel.getSelectedEntity().getValue());
                    }
                    viewModel.deleteSelected();
                } catch(Exception ex) {
                    new DialogHelper(getContext(), R.style.OnboardingDefaultTheme_Dialog_Alert)
                            .showOKOnlyDialog(
                                    getActivity().getSupportFragmentManager(),
                                    getResources().getString(R.string.delete_failed_detail),
                                    null, null, null);
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog,which) -> {
            });
            return builder.create();
        }
    }

}
