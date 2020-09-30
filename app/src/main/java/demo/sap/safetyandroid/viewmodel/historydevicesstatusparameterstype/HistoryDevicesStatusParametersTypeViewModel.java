package demo.sap.safetyandroid.viewmodel.historydevicesstatusparameterstype;

import android.app.Application;
import android.os.Parcelable;

import demo.sap.safetyandroid.viewmodel.EntityViewModel;
import com.sap.cloud.android.odata.v2.HistoryDevicesStatusParametersType;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;

/*
 * Represents View model for HistoryDevicesStatusParametersType
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and
 * return the view model of that type. This is because the ViewModelStore of
 * ViewModelProvider cannot not be able to tell the difference between EntityViewModel<type1>
 * and EntityViewModel<type2>.
 */
public class HistoryDevicesStatusParametersTypeViewModel extends EntityViewModel<HistoryDevicesStatusParametersType> {

    /**
    * Default constructor for a specific view model.
    * @param application - parent application
    */
    public HistoryDevicesStatusParametersTypeViewModel(Application application) {
        super(application, EntitySets.historyDevicesStatusParameters, HistoryDevicesStatusParametersType.startDate);
    }

    /**
    * Constructor for a specific view model with navigation data.
    * @param application - parent application
    * @param navigationPropertyName - name of the navigation property
    * @param entityData - parent entity (starting point of the navigation)
    */
	 public HistoryDevicesStatusParametersTypeViewModel(Application application, String navigationPropertyName, Parcelable entityData) {
        super(application, EntitySets.historyDevicesStatusParameters, HistoryDevicesStatusParametersType.startDate, navigationPropertyName, entityData);
    }
}
