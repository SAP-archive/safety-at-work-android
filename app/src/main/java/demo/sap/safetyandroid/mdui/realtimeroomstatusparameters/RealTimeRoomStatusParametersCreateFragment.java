package demo.sap.safetyandroid.mdui.realtimeroomstatusparameters;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.databinding.FragmentRealtimeroomstatusparametersCreateBinding;
import demo.sap.safetyandroid.mdui.BundleKeys;
import demo.sap.safetyandroid.mdui.InterfacedFragment;
import demo.sap.safetyandroid.mdui.UIConstants;
import demo.sap.safetyandroid.repository.OperationResult;
import demo.sap.safetyandroid.viewmodel.realtimeroomstatusparameterstype.RealTimeRoomStatusParametersTypeViewModel;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.android.odata.v2.RealTimeRoomStatusParametersType;
import com.sap.cloud.android.odata.v2.v2Metadata.EntityTypes;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell;
import com.sap.cloud.mobile.odata.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fragment that presents a screen to either create or update an existing RealTimeRoomStatusParametersType entity.
 * This fragment is contained in the {@link RealTimeRoomStatusParametersActivity}.
 */
public class RealTimeRoomStatusParametersCreateFragment extends InterfacedFragment<RealTimeRoomStatusParametersType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeRoomStatusParametersCreateFragment.class);
    //The key for the saved instance of the working entity for device configuration change
    private static final String KEY_WORKING_COPY = "WORKING_COPY";

    /** RealTimeRoomStatusParametersType object and it's copy: the modifications are done on the copied object. */
    private RealTimeRoomStatusParametersType realTimeRoomStatusParametersTypeEntity;
    private RealTimeRoomStatusParametersType realTimeRoomStatusParametersTypeEntityCopy;

    /** DataBinding generated class */
    private FragmentRealtimeroomstatusparametersCreateBinding binding;

    /** Indicate what operation to be performed */
    private String operation;

    /** RealTimeRoomStatusParametersType ViewModel */
    private RealTimeRoomStatusParametersTypeViewModel viewModel;

    /** The update menu item */
    private MenuItem updateMenuItem;

    /**
     * This fragment is used for both update and create for RealTimeRoomStatusParameters to enter values for the properties.
     * When used for update, an instance of the entity is required. In the case of create, a new instance
     * of the entity with defaults will be created. The default values may not be acceptable for the
     * OData service.
     * Arguments: Operation: [OP_CREATE | OP_UPDATE]
     *            RealTimeRoomStatusParametersType if Operation is update
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu = R.menu.itemlist_edit_options;
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        operation = bundle.getString(BundleKeys.OPERATION);
        if (UIConstants.OP_CREATE.equals(operation)) {
            activityTitle = currentActivity.getResources().getString(R.string.title_create_fragment, EntityTypes.realTimeRoomStatusParametersType.getLocalName());
        } else {
            activityTitle = currentActivity.getResources().getString(R.string.title_update_fragment) + " " + EntityTypes.realTimeRoomStatusParametersType.getLocalName();
        }

        ((RealTimeRoomStatusParametersActivity)currentActivity).isNavigationDisabled = true;
        viewModel = new ViewModelProvider(currentActivity).get(RealTimeRoomStatusParametersTypeViewModel.class);
        viewModel.getCreateResult().observe(this, result -> onComplete(result));
        viewModel.getUpdateResult().observe(this, result -> onComplete(result));

        if(UIConstants.OP_CREATE.equals(operation)) {
            realTimeRoomStatusParametersTypeEntity = createRealTimeRoomStatusParametersType();
        } else {
            realTimeRoomStatusParametersTypeEntity = viewModel.getSelectedEntity().getValue();
        }

        RealTimeRoomStatusParametersType workingCopy = null;
        if( savedInstanceState != null ) {
            workingCopy =  (RealTimeRoomStatusParametersType)savedInstanceState.getParcelable(KEY_WORKING_COPY);
        }
        if( workingCopy == null ) {
            realTimeRoomStatusParametersTypeEntityCopy = (RealTimeRoomStatusParametersType) realTimeRoomStatusParametersTypeEntity.copy();
            realTimeRoomStatusParametersTypeEntityCopy.setEntityTag(realTimeRoomStatusParametersTypeEntity.getEntityTag());
            realTimeRoomStatusParametersTypeEntityCopy.setOldEntity(realTimeRoomStatusParametersTypeEntity);
            realTimeRoomStatusParametersTypeEntityCopy.setEditLink((realTimeRoomStatusParametersTypeEntity.getEditLink()));
        } else {
            //in this case, the old entity and entity tag should already been set.
            realTimeRoomStatusParametersTypeEntityCopy = workingCopy;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ObjectHeader objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if( objectHeader != null ) objectHeader.setVisibility(View.GONE);
        return setupDataBinding(inflater, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_WORKING_COPY, realTimeRoomStatusParametersTypeEntityCopy);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(secondaryToolbar != null) {
            secondaryToolbar.setTitle(activityTitle);
        } else {
            getActivity().setTitle(activityTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                updateMenuItem = item;
                enableUpdateMenuItem(false);
                return onSaveItem();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** 
     * Enables or disables the update menu item base on the given 'enable'
     * @param enable true to enable the menu item, false otherwise
     */
    private void enableUpdateMenuItem(boolean enable) {
        updateMenuItem.setEnabled(enable);
        updateMenuItem.getIcon().setAlpha( enable ? 255 : 130);
    }

    /**
     * Saves the entity
     */
    private boolean onSaveItem() {
        if (!isRealTimeRoomStatusParametersTypeValid()) {
            return false;
        }
        //set 'isNavigationDisabled' false here to make sure the logic in list is ok, and set it to true if update fails.
        ((RealTimeRoomStatusParametersActivity)currentActivity).isNavigationDisabled = false;
        if( progressBar != null ) progressBar.setVisibility(View.VISIBLE);
        if (operation.equals(UIConstants.OP_CREATE)) {
            viewModel.create(realTimeRoomStatusParametersTypeEntityCopy);
        } else {
            viewModel.update(realTimeRoomStatusParametersTypeEntityCopy);
        }
        return true;
    }

    /**
     * Create a new RealTimeRoomStatusParametersType instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new RealTimeRoomStatusParametersType instance
     */
    private RealTimeRoomStatusParametersType createRealTimeRoomStatusParametersType() {
        RealTimeRoomStatusParametersType realTimeRoomStatusParametersTypeEntity = new RealTimeRoomStatusParametersType(true);
        return realTimeRoomStatusParametersTypeEntity;
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private void onComplete(@NonNull OperationResult<RealTimeRoomStatusParametersType> result) {
        if( progressBar != null ) progressBar.setVisibility(View.INVISIBLE);
        enableUpdateMenuItem(true);
        if (result.getError() != null) {
            ((RealTimeRoomStatusParametersActivity)currentActivity).isNavigationDisabled = true;
            handleError(result);
        } else {
            boolean isMasterDetail = currentActivity.getResources().getBoolean(R.bool.two_pane);
            if( UIConstants.OP_UPDATE.equals(operation) && !isMasterDetail) {
                viewModel.setSelectedEntity(realTimeRoomStatusParametersTypeEntityCopy);
            }
            currentActivity.onBackPressed();
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private boolean isValidProperty(@NonNull Property property, @NonNull String value) {
        boolean isValid = true;
        if (!property.isNullable() && value.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Set up data binding for this view
     * @param inflater - layout inflater from onCreateView
     * @param container - view group from onCreateView
     * @return view - rootView from generated data binding code
     */
    private View setupDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentRealtimeroomstatusparametersCreateBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.setRealTimeRoomStatusParametersType(realTimeRoomStatusParametersTypeEntityCopy);
        return rootView;
    }

    /** Validate the edited inputs */
    private boolean isRealTimeRoomStatusParametersTypeValid() {
        LinearLayout linearLayout = getView().findViewById(R.id.create_update_realtimeroomstatusparameterstype);
        boolean isValid = true;
        // validate properties i.e. check non-nullable properties are truly non-null
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View viewItem = linearLayout.getChildAt(i);
            SimplePropertyFormCell simplePropertyFormCell = (SimplePropertyFormCell)viewItem;
            String propertyName = (String) simplePropertyFormCell.getTag();
            Property property = EntityTypes.realTimeRoomStatusParametersType.getProperty(propertyName);
            String value = simplePropertyFormCell.getValue().toString();
            if (!isValidProperty(property, value)) {
                simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, true);
                String errorMessage = getResources().getString(R.string.mandatory_warning);
                simplePropertyFormCell.setErrorEnabled(true);
                simplePropertyFormCell.setError(errorMessage);
                isValid = false;
            }
            else {
                if (simplePropertyFormCell.isErrorEnabled()){
                    boolean hasMandatoryError = (Boolean)simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR);
                    if (!hasMandatoryError) {
                        isValid = false;
                    } else {
                        simplePropertyFormCell.setErrorEnabled(false);
                    }
                }
                simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, false);
            }
        }
        return isValid;
    }

    /**
     * Notify user of error encountered while execution the operation
     * @param result - operation result with error
     */
    private void handleError(@NonNull OperationResult<RealTimeRoomStatusParametersType> result) {
        String errorMessage;
        switch (result.getOperation()) {
            case UPDATE:
                errorMessage = getResources().getString(R.string.update_failed_detail);
                break;
            case CREATE:
                errorMessage = getResources().getString(R.string.create_failed_detail);
                break;
            default:
                throw new AssertionError();
        }
        showError(errorMessage);
    }
}
