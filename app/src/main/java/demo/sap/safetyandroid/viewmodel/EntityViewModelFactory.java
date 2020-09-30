package demo.sap.safetyandroid.viewmodel;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.os.Parcelable;

import demo.sap.safetyandroid.viewmodel.devicesettype.DeviceSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.devicetagsettype.DeviceTagSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.deviceuserviewsettype.DeviceUserViewSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.ephemeralidinfectedsettype.EphemeralIDInfectedSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.ephemeralidsettype.EphemeralIDSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.eventsettype.EventSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.historydevicesstatustype.HistoryDevicesStatusTypeViewModel;
import demo.sap.safetyandroid.viewmodel.historydevicesstatusparameterstype.HistoryDevicesStatusParametersTypeViewModel;
import demo.sap.safetyandroid.viewmodel.infectedsettype.InfectedSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.proximitydetectedsettype.ProximityDetectedSetTypeViewModel;
import demo.sap.safetyandroid.viewmodel.realtimeroomstatustype.RealTimeRoomStatusTypeViewModel;
import demo.sap.safetyandroid.viewmodel.realtimeroomstatusparameterstype.RealTimeRoomStatusParametersTypeViewModel;
import demo.sap.safetyandroid.viewmodel.tagsettype.TagSetTypeViewModel;


/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 */
public class EntityViewModelFactory implements ViewModelProvider.Factory {

	// application class
    private Application application;
	// name of the navigation property
    private String navigationPropertyName;
	// parent entity
    private Parcelable entityData;

	/**
	 * Creates a factory class for entity view models created following a navigation link.
	 *
	 * @param application parent application
	 * @param navigationPropertyName name of the navigation link
	 * @param entityData parent entity
	 */
    public EntityViewModelFactory(Application application, String navigationPropertyName, Parcelable entityData) {
        this.application = application;
        this.navigationPropertyName = navigationPropertyName;
        this.entityData = entityData;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        T retValue = null;
		switch(modelClass.getSimpleName()) {



			case "DeviceSetTypeViewModel":
				retValue = (T) new DeviceSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "DeviceTagSetTypeViewModel":
				retValue = (T) new DeviceTagSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "DeviceUserViewSetTypeViewModel":
				retValue = (T) new DeviceUserViewSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "EphemeralIDInfectedSetTypeViewModel":
				retValue = (T) new EphemeralIDInfectedSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "EphemeralIDSetTypeViewModel":
				retValue = (T) new EphemeralIDSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "EventSetTypeViewModel":
				retValue = (T) new EventSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "HistoryDevicesStatusTypeViewModel":
				retValue = (T) new HistoryDevicesStatusTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "HistoryDevicesStatusParametersTypeViewModel":
				retValue = (T) new HistoryDevicesStatusParametersTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "InfectedSetTypeViewModel":
				retValue = (T) new InfectedSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "ProximityDetectedSetTypeViewModel":
				retValue = (T) new ProximityDetectedSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "RealTimeRoomStatusTypeViewModel":
				retValue = (T) new RealTimeRoomStatusTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "RealTimeRoomStatusParametersTypeViewModel":
				retValue = (T) new RealTimeRoomStatusParametersTypeViewModel(application, navigationPropertyName, entityData);
				break;
			case "TagSetTypeViewModel":
				retValue = (T) new TagSetTypeViewModel(application, navigationPropertyName, entityData);
				break;
		}
		return retValue;
	}
}