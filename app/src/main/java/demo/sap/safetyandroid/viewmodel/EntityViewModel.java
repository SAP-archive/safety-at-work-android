package demo.sap.safetyandroid.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.content.res.Resources;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;


import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.repository.OperationResult;
import demo.sap.safetyandroid.repository.Repository;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.archcomp.SingleLiveEvent;
import com.sap.cloud.mobile.odata.EntitySet;
import com.sap.cloud.mobile.odata.EntityValue;
import com.sap.cloud.mobile.odata.Property;

import com.sap.cloud.mobile.odata.StreamBase;
import java.util.ArrayList;
import java.util.List;

/*
 * Generic type representing the view model with type being one of the entity types.
 * Each entity type has its own view model.
 * Note: an entity set is a collection of entities of an entity set.
 * View model exposed the list of entities as LiveData and four events (CRUD) as SingleLiveEvent
 * acquired from the repository for the entity type
 * Note: This class extends AndroidViewModel so it has access to the application object
 */
public class EntityViewModel<T extends EntityValue> extends AndroidViewModel {

    /*
     * Event to notify of async completion of read/query operation
     */
    private final SingleLiveEvent<OperationResult<T>> readResult;

    /*
     * Event to notify of async completion of create operation
     */
    private final SingleLiveEvent<OperationResult<T>> createResult;

    /*
     * Event to notify of async completion of update operation
     */
    private final SingleLiveEvent<OperationResult<T>> updateResult;

    /*
     * Event to notify of async completion of delete operation
     */
    private final SingleLiveEvent<OperationResult<T>> deleteResult;

    /*
     * LiveData of entities for this entity set
     */
    private final LiveData<List<T>> entities;

    /*
     * Repository of type of the entity type for a specified entity set
     */
    private final Repository<T> repository;

    /*
     * Selected items via long press action
     */
    private List<T> selectedItems = new ArrayList<>();

    /*
     * Identifier of item in focus via click action
     */
    private long inFocusId;

    /*
     * Flag for avoiding continuous retry.
     */
    private boolean isDownloadError;

    /**
     * Constructs a view model for the specified entity set
     * @param application - required as it extends AndroidViewModel
     * @param entitySet - entity set that this view represents
     */
    public EntityViewModel(@NonNull Application application, @NonNull EntitySet entitySet, @Nullable Property orderByProperty) {
        super(application);
        repository = ((SAPWizardApplication) application)
            .getRepositoryFactory()
            .getRepository(entitySet, orderByProperty);
        entities = repository.getObservableEntities();
        readResult = repository.getReadResult();
        createResult = repository.getCreateResult();
        updateResult = repository.getUpdateResult();
        deleteResult = repository.getDeleteResult();
    }

   /**
    * Creates a view model with navigation information
    * @param application - required as it extends AndroidViewModel
    * @param entitySet - entity set that this view represents
    * @param orderByProperty - property used for ordering the entity list
    * @param navPropName - name of the navigation property
    * @param entityData - parent entity
    */
	public EntityViewModel(Application application, @NonNull EntitySet entitySet, @NonNull Property orderByProperty, String navPropName, Parcelable entityData) {
        super(application);
        repository = ((SAPWizardApplication) application)
                .getRepositoryFactory()
                .getRepository(entitySet, orderByProperty);
        entities = repository.getObservableEntities();
        readResult = repository.getReadResult();
        createResult = repository.getCreateResult();
        updateResult = repository.getUpdateResult();
        deleteResult = repository.getDeleteResult();
        repository.clear();
        repository.read((EntityValue) entityData, navPropName);
    }

    @NonNull
    public LiveData<List<T>> getObservableItems() {
        return entities;
    }

    @NonNull
    public LiveData<OperationResult<T>> getCreateResult() {
        return createResult;
    }

    @NonNull
    public LiveData<OperationResult<T>> getReadResult() {
        return readResult;
    }

    @NonNull
    public LiveData<OperationResult<T>> getUpdateResult() {
        return updateResult;
    }

    @NonNull
    public LiveData<OperationResult<T>> getDeleteResult() {
        return deleteResult;
    }

    public void refresh() {
        repository.read();
    }

    public void clear() {
        repository.clear();
    }

    public void refresh(EntityValue parent, String navPropName) {
        repository.read(parent, navPropName);
    }

    public void create(@NonNull T entity, @NonNull StreamBase media) {
        repository.create(entity, media);
    }

    public void create(@NonNull T entity) {
        repository.create(entity);
    }

    public void update(@NonNull T entity) {
        repository.update(entity);
    }

    public void delete(@NonNull List<T> entities) {
        repository.delete(entities);
    }

    public void deleteSelected() {
        repository.delete(selectedItems);
    }


    /**
     * Perform initial read of repository. However, if data is already available, read is not be performed.
     */
    public void initialRead(InitialReadListener listener) {
        if (!isDownloadError) {
            repository.initialRead( 
                () -> {
                    isDownloadError = false;
                },
                error -> {
                    isDownloadError = true;
                    Resources resources = getApplication().getResources();
                    listener.onError(resources.getString(R.string.read_failed_detail));
                }
            );
        }
    }

    public interface InitialReadListener {
        void onError(String errorMessage);
    }

    /*
     * For management of items selected via long press action
     */
    public void addSelected(@NonNull T selected) {
        boolean found = false;
        for  (T item: selectedItems) {
            if (item.equals(selected)) {
                found = true;
                break;
            }
        }
        if (!found) {
            selectedItems.add(selected);
        }
    }

    public void removeSelected(@NonNull T selected) {
        if (selectedItems.contains(selected)) {
            selectedItems.remove(selected);
        }
    }

    @Nullable
    public T getSelected(int index) {
        if (index >= selectedItems.size() || index < 0) {
            return null;
        }
        return selectedItems.get(index);
    }

    public void removeAllSelected() {
        selectedItems.clear();
    }

    public int numberOfSelected() {
        return selectedItems.size();
    }

    public boolean selectedContains(@NonNull T member) {
        return selectedItems.contains(member);
    }

    /*
     * For management of active item selected via click action
     */
    public long getInFocusId() {
        return inFocusId;
    }

    public void setInFocusId(long inFocusId) {
        this.inFocusId = inFocusId;
    }

    /** The observable data for the selection in the list */
    private MutableLiveData<T> selectedEntity = new MutableLiveData<T>();
    public void setSelectedEntity(@NonNull T v) {
        selectedEntity.setValue(v);
    }
    @Nullable
    public MutableLiveData<T> getSelectedEntity() {
        return selectedEntity;
    }
}