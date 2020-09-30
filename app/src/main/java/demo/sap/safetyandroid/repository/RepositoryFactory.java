package demo.sap.safetyandroid.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import demo.sap.safetyandroid.service.SAPServiceManager;

import com.sap.cloud.android.odata.v2.v2;
import com.sap.cloud.android.odata.v2.v2Metadata.EntitySets;

import com.sap.cloud.android.odata.v2.DeviceSetType;
import com.sap.cloud.android.odata.v2.DeviceTagSetType;
import com.sap.cloud.android.odata.v2.DeviceUserViewSetType;
import com.sap.cloud.android.odata.v2.EphemeralIDInfectedSetType;
import com.sap.cloud.android.odata.v2.EphemeralIDSetType;
import com.sap.cloud.android.odata.v2.EventSetType;
import com.sap.cloud.android.odata.v2.HistoryDevicesStatusType;
import com.sap.cloud.android.odata.v2.HistoryDevicesStatusParametersType;
import com.sap.cloud.android.odata.v2.InfectedSetType;
import com.sap.cloud.android.odata.v2.ProximityDetectedSetType;
import com.sap.cloud.android.odata.v2.RealTimeRoomStatusType;
import com.sap.cloud.android.odata.v2.RealTimeRoomStatusParametersType;
import com.sap.cloud.android.odata.v2.TagSetType;

import com.sap.cloud.mobile.odata.EntitySet;
import com.sap.cloud.mobile.odata.Property;

import java.util.WeakHashMap;

/*
 * Repository factory to construct repository for an entity set
 */
public class RepositoryFactory {

    /*
     * Cache all repositories created to avoid reconstruction and keeping the entities of entity set
     * maintained by each repository in memory. Use a weak hash map to allow recovery in low memory
     * conditions
     */
    private WeakHashMap<String, Repository> repositories;

    /*
     * Service manager to interact with OData service
     */
    private SAPServiceManager sapServiceManager;

    /**
     * Construct a RepositoryFactory instance. There should only be one repository factory and used
     * throughout the life of the application to avoid caching entities multiple times.
     * @param sapServiceManager - Service manager for interaction with OData service
     */
    public RepositoryFactory(SAPServiceManager sapServiceManager) {
        repositories = new WeakHashMap<>();
        this.sapServiceManager = sapServiceManager;
    }

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    public Repository getRepository(@NonNull EntitySet entitySet, @Nullable Property orderByProperty) {
        v2 v2 = sapServiceManager.getv2();
        String key = entitySet.getLocalName();
        Repository repository = repositories.get(key);
        if (repository == null) {
            if (key.equals(EntitySets.deviceSet.getLocalName())) {
                repository = new Repository<DeviceSetType>(v2, EntitySets.deviceSet, orderByProperty);
            } else if (key.equals(EntitySets.deviceTagSet.getLocalName())) {
                repository = new Repository<DeviceTagSetType>(v2, EntitySets.deviceTagSet, orderByProperty);
            } else if (key.equals(EntitySets.deviceUserViewSet.getLocalName())) {
                repository = new Repository<DeviceUserViewSetType>(v2, EntitySets.deviceUserViewSet, orderByProperty);
            } else if (key.equals(EntitySets.ephemeralIDInfectedSet.getLocalName())) {
                repository = new Repository<EphemeralIDInfectedSetType>(v2, EntitySets.ephemeralIDInfectedSet, orderByProperty);
            } else if (key.equals(EntitySets.ephemeralIDSet.getLocalName())) {
                repository = new Repository<EphemeralIDSetType>(v2, EntitySets.ephemeralIDSet, orderByProperty);
            } else if (key.equals(EntitySets.eventSet.getLocalName())) {
                repository = new Repository<EventSetType>(v2, EntitySets.eventSet, orderByProperty);
            } else if (key.equals(EntitySets.historyDevicesStatus.getLocalName())) {
                repository = new Repository<HistoryDevicesStatusType>(v2, EntitySets.historyDevicesStatus, orderByProperty);
            } else if (key.equals(EntitySets.historyDevicesStatusParameters.getLocalName())) {
                repository = new Repository<HistoryDevicesStatusParametersType>(v2, EntitySets.historyDevicesStatusParameters, orderByProperty);
            } else if (key.equals(EntitySets.infectedSet.getLocalName())) {
                repository = new Repository<InfectedSetType>(v2, EntitySets.infectedSet, orderByProperty);
            } else if (key.equals(EntitySets.proximityDetectedSet.getLocalName())) {
                repository = new Repository<ProximityDetectedSetType>(v2, EntitySets.proximityDetectedSet, orderByProperty);
            } else if (key.equals(EntitySets.realTimeRoomStatus.getLocalName())) {
                repository = new Repository<RealTimeRoomStatusType>(v2, EntitySets.realTimeRoomStatus, orderByProperty);
            } else if (key.equals(EntitySets.realTimeRoomStatusParameters.getLocalName())) {
                repository = new Repository<RealTimeRoomStatusParametersType>(v2, EntitySets.realTimeRoomStatusParameters, orderByProperty);
            } else if (key.equals(EntitySets.tagSet.getLocalName())) {
                repository = new Repository<TagSetType>(v2, EntitySets.tagSet, orderByProperty);
            } else {
                throw new AssertionError("Fatal error, entity set[" + key + "] missing in generated code");
            }
            repositories.put(key, repository);
        }
        return repository;
    }

    /**
     * Get rid of all cached repositories
     */
    public void reset() {
        repositories.clear();
    }
 }
