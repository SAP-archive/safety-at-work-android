package demo.sap.safetyandroid.mdui.deviceset;

import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import demo.sap.safetyandroid.service.SAPServiceManager;
import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.databinding.FragmentDevicesetDetailBinding;
import demo.sap.safetyandroid.mdui.BundleKeys;
import demo.sap.safetyandroid.mdui.InterfacedFragment;
import demo.sap.safetyandroid.mdui.UIConstants;
import demo.sap.safetyandroid.mdui.EntityKeyUtil;
import demo.sap.safetyandroid.repository.OperationResult;
import demo.sap.safetyandroid.viewmodel.devicesettype.DeviceSetTypeViewModel;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;
import com.sap.cloud.android.odata.v2.DeviceSetType;
import com.sap.cloud.mobile.fiori.object.ObjectHeader;
import com.sap.cloud.mobile.odata.DataValue;
import demo.sap.safetyandroid.mdui.deviceset.DeviceSetActivity;
import demo.sap.safetyandroid.mdui.ephemeralidset.EphemeralIDSetActivity;
import demo.sap.safetyandroid.mdui.infectedset.InfectedSetActivity;
import demo.sap.safetyandroid.mdui.devicetagset.DeviceTagSetActivity;

/**
 * A fragment representing a single DeviceSetType detail screen.
 * This fragment is contained in an DeviceSetActivity.
 */
public class DeviceSetDetailFragment extends InterfacedFragment<DeviceSetType> {

    /** Generated data binding class based on layout file */
    private FragmentDevicesetDetailBinding binding;

    /** DeviceSetType entity to be displayed */
    private DeviceSetType deviceSetTypeEntity = null;

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private ObjectHeader objectHeader;

    /** View model of the entity type that the displayed entity belongs to */
    private DeviceSetTypeViewModel viewModel;

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private SAPServiceManager sapServiceManager;

    /** Arguments: DeviceSetType for display */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menu = R.menu.itemlist_view_options;
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return setupDataBinding(inflater, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(currentActivity).get(DeviceSetTypeViewModel.class);
        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), this::onDeleteComplete);
        viewModel.getSelectedEntity().observe(getViewLifecycleOwner(), entity -> {
            deviceSetTypeEntity = entity;
            binding.setDeviceSetType(entity);
            setupObjectHeader();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_item:
                listener.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, deviceSetTypeEntity);
                return true;
            case R.id.delete_item:
                listener.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onNavigationClickedToDeviceSet_ownedBy(View v) {
        Intent intent = new Intent(this.currentActivity, DeviceSetActivity.class);
        intent.putExtra("parent", deviceSetTypeEntity);
        intent.putExtra("navigation", "ownedBy");
        startActivity(intent);
    }

    public void onNavigationClickedToEphemeralIDSet_toEIDs(View v) {
        Intent intent = new Intent(this.currentActivity, EphemeralIDSetActivity.class);
        intent.putExtra("parent", deviceSetTypeEntity);
        intent.putExtra("navigation", "toEIDs");
        startActivity(intent);
    }

    public void onNavigationClickedToInfectedSet_toInfections(View v) {
        Intent intent = new Intent(this.currentActivity, InfectedSetActivity.class);
        intent.putExtra("parent", deviceSetTypeEntity);
        intent.putExtra("navigation", "toInfections");
        startActivity(intent);
    }

    public void onNavigationClickedToDeviceTagSet_toTags(View v) {
        Intent intent = new Intent(this.currentActivity, DeviceTagSetActivity.class);
        intent.putExtra("parent", deviceSetTypeEntity);
        intent.putExtra("navigation", "toTags");
        startActivity(intent);
    }


    /** Completion callback for delete operation */
    private void onDeleteComplete(@NonNull OperationResult<DeviceSetType> result) {
        if( progressBar != null ) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        viewModel.removeAllSelected(); //to make sure the 'action mode' not activated in the list
        Exception ex = result.getError();
        if (ex != null) {
            showError(getString(R.string.delete_failed_detail));
            return;
        }
        listener.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, deviceSetTypeEntity);
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private void setDetailImage(@NonNull ObjectHeader objectHeader, @NonNull DeviceSetType deviceSetTypeEntity) {
        if (deviceSetTypeEntity.getDataValue(DeviceSetType.type_) != null && !deviceSetTypeEntity.getDataValue(DeviceSetType.type_).toString().isEmpty()) {
            objectHeader.setDetailImageCharacter(deviceSetTypeEntity.getDataValue(DeviceSetType.type_).toString().substring(0, 1));
        } else {
            objectHeader.setDetailImageCharacter("?");
        }
    }

    /**
     * Setup ObjectHeader with an instance of DeviceSetType
     */
    private void setupObjectHeader() {
        Toolbar secondToolbar = currentActivity.findViewById(R.id.secondaryToolbar);
        if (secondToolbar != null) {
            secondToolbar.setTitle(deviceSetTypeEntity.getEntityType().getLocalName());
        } else {
            currentActivity.setTitle(deviceSetTypeEntity.getEntityType().getLocalName());
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader);
        if (objectHeader != null) {
            // Use of getDataValue() avoids the knowledge of what data type the master property is.
            // This is a convenience for wizard generated code. Normally, developer will use the proxy class
            // get<Property>() method and add code to convert to string
            DataValue dataValue = deviceSetTypeEntity.getDataValue(DeviceSetType.type_);
            if (dataValue != null) {
                objectHeader.setHeadline(dataValue.toString());
            } else {
                objectHeader.setHeadline(null);
            }
            // EntityKey in string format: '{"key":value,"key2":value2}'
            objectHeader.setSubheadline(EntityKeyUtil.getOptionalEntityKey(deviceSetTypeEntity));
            objectHeader.setTag("#tag1", 0);
            objectHeader.setTag("#tag3", 2);
            objectHeader.setTag("#tag2", 1);

            objectHeader.setBody("You can set the header body text here.");
            objectHeader.setFootnote("You can set the header footnote here.");
            objectHeader.setDescription("You can add a detailed item description here.");

            setDetailImage(objectHeader, deviceSetTypeEntity);
            objectHeader.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up databinding for this view
     *
     * @param inflater - layout inflater from onCreateView
     * @param container - view group from onCreateView
     * @return view - rootView from generated databinding code
     */
    private View setupDataBinding(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentDevicesetDetailBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.setHandler(this);
        return rootView;
    }
}
