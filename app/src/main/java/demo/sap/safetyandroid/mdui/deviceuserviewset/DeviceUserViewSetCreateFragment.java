package demo.sap.safetyandroid.mdui.deviceuserviewset;

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
import demo.sap.safetyandroid.databinding.FragmentDeviceuserviewsetCreateBinding;
import demo.sap.safetyandroid.mdui.BundleKeys;
import demo.sap.safetyandroid.mdui.InterfacedFragment;
import demo.sap.safetyandroid.mdui.UIConstants;
import demo.sap.safetyandroid.repository.OperationResult;
import demo.sap.safetyandroid.viewmodel.deviceuserviewsettype.DeviceUserViewSetTypeViewModel;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.android.odata.v2.DeviceUserViewSetType;
import com.sap.cloud.android.odata.v2.v2Metadata.EntityTypes;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell;
import com.sap.cloud.mobile.odata.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fragment that presents a screen to either create or update an existing DeviceUserViewSetType entity.
 * This fragment is contained in the {@link DeviceUserViewSetActivity}.
 */
public class DeviceUserViewSetCreateFragment extends InterfacedFragment<DeviceUserViewSetType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceUserViewSetCreateFragment.class);
    //The key for the saved instance of the working entity for device configuration change
    private static final String KEY_WORKING_COPY = "WORKING_COPY";

    /** DeviceUserViewSetType object and it's copy: the modifications are done on the copied object. */
    private DeviceUserViewSetType deviceUserViewSetTypeEntity;
    private DeviceUserViewSetType deviceUserViewSetTypeEntityCopy;

    /** DataBinding generated class */
    private FragmentDeviceuserviewsetCreateBinding binding;

    /** Indicate what operation to be performed */
    private String operation;

    /** DeviceUserViewSetType ViewModel */
    private DeviceUserViewSetTypeViewModel viewModel;

    /** The update menu item */
    private MenuItem updateMenuItem;

    /**
     * This fragment is used for both update and create for DeviceUserViewSet to enter values for the properties.
     * When used for update, an instance of the entity is required. In the case of create, a new instance
     * of the entity with defaults will be created. The default values may not be acceptable for the
     * OData service.
     * Arguments: Operation: [OP_CREATE | OP_UPDATE]
     *            DeviceUserViewSetType if Operation is update
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu = R.menu.itemlist_edit_options;
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        operation = bundle.getString(BundleKeys.OPERATION);
        if (UIConstants.OP_CREATE.equals(operation)) {
            activityTitle = currentActivity.getResources().getString(R.string.title_create_fragment, EntityTypes.deviceUserViewSetType.getLocalName());
        } else {
            activityTitle = currentActivity.getResources().getString(R.string.title_update_fragment) + " " + EntityTypes.deviceUserViewSetType.getLocalName();
        }

        ((DeviceUserViewSetActivity)currentActivity).isNavigationDisabled = true;
        viewModel = new ViewModelProvider(currentActivity).get(DeviceUserViewSetTypeViewModel.class);
        viewModel.getCreateResult().observe(this, result -> onComplete(result));
        viewModel.getUpdateResult().observe(this, result -> onComplete(result));

        if(UIConstants.OP_CREATE.equals(operation)) {
            deviceUserViewSetTypeEntity = createDeviceUserViewSetType();
        } else {
            deviceUserViewSetTypeEntity = viewModel.getSelectedEntity().getValue();
        }

        DeviceUserViewSetType workingCopy = null;
        if( savedInstanceState != null ) {
            workingCopy =  (DeviceUserViewSetType)savedInstanceState.getParcelable(KEY_WORKING_COPY);
        }
        if( workingCopy == null ) {
            deviceUserViewSetTypeEntityCopy = (DeviceUserViewSetType) deviceUserViewSetTypeEntity.copy();
            deviceUserViewSetTypeEntityCopy.setEntityTag(deviceUserViewSetTypeEntity.getEntityTag());
            deviceUserViewSetTypeEntityCopy.setOldEntity(deviceUserViewSetTypeEntity);
            deviceUserViewSetTypeEntityCopy.setEditLink((deviceUserViewSetTypeEntity.getEditLink()));
        } else {
            //in this case, the old entity and entity tag should already been set.
            deviceUserViewSetTypeEntityCopy = workingCopy;
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
        outState.putParcelable(KEY_WORKING_COPY, deviceUserViewSetTypeEntityCopy);
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
        if (!isDeviceUserViewSetTypeValid()) {
            return false;
        }
        //set 'isNavigationDisabled' false here to make sure the logic in list is ok, and set it to true if update fails.
        ((DeviceUserViewSetActivity)currentActivity).isNavigationDisabled = false;
        if( progressBar != null ) progressBar.setVisibility(View.VISIBLE);
        if (operation.equals(UIConstants.OP_CREATE)) {
            viewModel.create(deviceUserViewSetTypeEntityCopy);
        } else {
            viewModel.update(deviceUserViewSetTypeEntityCopy);
        }
        return true;
    }

    /**
     * Create a new DeviceUserViewSetType instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new DeviceUserViewSetType instance
     */
    private DeviceUserViewSetType createDeviceUserViewSetType() {
        DeviceUserViewSetType deviceUserViewSetTypeEntity = new DeviceUserViewSetType(true);
        return deviceUserViewSetTypeEntity;
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private void onComplete(@NonNull OperationResult<DeviceUserViewSetType> result) {
        if( progressBar != null ) progressBar.setVisibility(View.INVISIBLE);
        enableUpdateMenuItem(true);
        if (result.getError() != null) {
            ((DeviceUserViewSetActivity)currentActivity).isNavigationDisabled = true;
            handleError(result);
        } else {
            boolean isMasterDetail = currentActivity.getResources().getBoolean(R.bool.two_pane);
            if( UIConstants.OP_UPDATE.equals(operation) && !isMasterDetail) {
                viewModel.setSelectedEntity(deviceUserViewSetTypeEntityCopy);
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
        binding = FragmentDeviceuserviewsetCreateBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.setDeviceUserViewSetType(deviceUserViewSetTypeEntityCopy);
        return rootView;
    }

    /** Validate the edited inputs */
    private boolean isDeviceUserViewSetTypeValid() {
        LinearLayout linearLayout = getView().findViewById(R.id.create_update_deviceuserviewsettype);
        boolean isValid = true;
        // validate properties i.e. check non-nullable properties are truly non-null
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View viewItem = linearLayout.getChildAt(i);
            SimplePropertyFormCell simplePropertyFormCell = (SimplePropertyFormCell)viewItem;
            String propertyName = (String) simplePropertyFormCell.getTag();
            Property property = EntityTypes.deviceUserViewSetType.getProperty(propertyName);
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
    private void handleError(@NonNull OperationResult<DeviceUserViewSetType> result) {
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
