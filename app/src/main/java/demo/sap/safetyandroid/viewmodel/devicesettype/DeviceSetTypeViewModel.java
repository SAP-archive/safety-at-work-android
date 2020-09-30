package demo.sap.safetyandroid.viewmodel.devicesettype;

import android.app.Application;
import android.os.Parcelable;

import demo.sap.safetyandroid.viewmodel.EntityViewModel;
import com.sap.cloud.android.odata.v2.DeviceSetType;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;

/*
 * Represents View model for DeviceSetType
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and
 * return the view model of that type. This is because the ViewModelStore of
 * ViewModelProvider cannot not be able to tell the difference between EntityViewModel<type1>
 * and EntityViewModel<type2>.
 */
public class DeviceSetTypeViewModel extends EntityViewModel<DeviceSetType> {

    /**
    * Default constructor for a specific view model.
    * @param application - parent application
    */
    public DeviceSetTypeViewModel(Application application) {
        super(application, EntitySets.deviceSet, DeviceSetType.type_);
    }

    /**
    * Constructor for a specific view model with navigation data.
    * @param application - parent application
    * @param navigationPropertyName - name of the navigation property
    * @param entityData - parent entity (starting point of the navigation)
    */
	 public DeviceSetTypeViewModel(Application application, String navigationPropertyName, Parcelable entityData) {
        super(application, EntitySets.deviceSet, DeviceSetType.type_, navigationPropertyName, entityData);
    }
}
