package demo.sap.safetyandroid.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import demo.sap.safetyandroid.archcomp.SingleLiveEvent;
import demo.sap.safetyandroid.mediaresource.EntityMediaResource;

import demo.sap.safetyandroid.repository.OperationResult.Operation;

import com.sap.cloud.android.odata.v2.v2;

import com.sap.cloud.mobile.odata.ChangeSet;
import com.sap.cloud.mobile.odata.DataQuery;
import com.sap.cloud.mobile.odata.DataType;
import com.sap.cloud.mobile.odata.DataValue;
import com.sap.cloud.mobile.odata.EntitySet;
import com.sap.cloud.mobile.odata.EntityValue;
import com.sap.cloud.mobile.odata.EntityValueList;
import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.SortOrder;
import com.sap.cloud.mobile.odata.StreamBase;
import com.sap.cloud.mobile.odata.core.Action0;
import com.sap.cloud.mobile.odata.core.Action1;
import com.sap.cloud.mobile.odata.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Generic type representing repository with type being one of the entity types.
 * In other words, each entity type has its own repository and an in-memory store of all the entities
 * of that type.
 * Repository exposed the list of entities as LiveData and four events (CRUD) as SingleLiveEvent
 */
public class Repository<T extends EntityValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);

    /*
     * OData service
     */
    private v2 v2;

    /*
     * OrderBy property, used to order the collection retrieved from OData service
     */
    private Property orderByProperty;

    /*
     * Entity set associated with this repository
     */
    private EntitySet entitySet;

    /*
     * Indicate if metadata=full parameter needs to be set during query for the entity set
     * V4 and higher OData version services do not return metadata as part of the result preventing the
     * the construction of download url for use by Glide.
     */
    private boolean needFullMetadata = false;

    /*
     * Cache is only in-memory but can be extended to persist to avoid fetching on application re-launches
     */
    private List<T> entities = new ArrayList<>();

    /*
     * Cache for the related entities;
     */
    private List<T> relatedEntities = new ArrayList<>();

    /*
     * LiveData for the list of entities returned by OData service for this entity set
     */
    private MutableLiveData<List<T>> observableEntities;

    /*
     * Event to notify of async completion of create operation
     */
    private final SingleLiveEvent<OperationResult<T>> createResult = new SingleLiveEvent<>();

    /*
     * Event to notify of async completion of read/query operation
     */
    private final SingleLiveEvent<OperationResult<T>> readResult = new SingleLiveEvent<>();

    /*
     * Event to notify of async completion of update operation
     */
    private final SingleLiveEvent<OperationResult<T>> updateResult = new SingleLiveEvent<>();

    /*
     * Event to notify of async completion of delete operation
     */
    private final SingleLiveEvent<OperationResult<T>> deleteResult = new SingleLiveEvent<>();

    /**
     * Flag to indicate if repository has been populated with an initial read
     */
    private boolean initialReadDone;

    /**
     * Construct a repository for performing operation against the OData service
     * Determine if full metadata parameter is required to advise OData service returning full metadata on queries
     * when entity set has media resource (media linked entity or having named resources as properties).
     * Versions below 4.0 always return query resutls with metadata.
     * @param v2 - OData service
     * @param entitySet - the entity set this repository provides access for
     */
    public Repository(@NonNull v2 v2, @NonNull EntitySet entitySet, @Nullable Property orderByProperty) {
        this.v2 = v2;
        this.entitySet = entitySet;
        this.orderByProperty = orderByProperty;
        observableEntities = new MutableLiveData<>();
        if (EntityMediaResource.isV4(v2.getMetadata().getVersionCode()) && EntityMediaResource.hasMediaResources(entitySet)) {
            needFullMetadata = true;
        }
    }

    @NonNull
    public SingleLiveEvent<OperationResult<T>> getCreateResult() {
        return createResult;
    }

    @NonNull
    public SingleLiveEvent<OperationResult<T>> getReadResult() {
        return readResult;
    }

    @NonNull
    public SingleLiveEvent<OperationResult<T>> getUpdateResult() { return updateResult; }

    @NonNull
    public SingleLiveEvent<OperationResult<T>> getDeleteResult() { return deleteResult; }

    @NonNull
    public LiveData<List<T>> getObservableEntities() {
        return observableEntities;
    }

    /*
     * For convenience of code generation, read is implemented using dynamic API.
     * However, if we are to create an entity set specific repository, it is highly recommended that
     * the generated getters for the entity set being utilized as they return strongly type proxy class
     * that will simplify consumption. For example, get<entity set>Async should be used to simplify the
     * implementation for read when we are dealing the entity type associated with the entity set.
     *
     * Read method to retrieve all entities of the entity set
     */
    public void read() {
        relatedEntities.clear();

        DataQuery dataQuery = new DataQuery().from(entitySet);
        if (orderByProperty != null) {
            dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING);
        }
        v2.executeQueryAsync(dataQuery,
            result -> {
                OperationResult<T> operationResult;
                List<T> entitiesRead = this.<T>convert(result.getEntityList());
                entities.clear();
                entities.addAll(entitiesRead);
                // Update observables
                observableEntities.setValue(entitiesRead);
                operationResult = new OperationResult<>(Operation.READ);
                readResult.setValue(operationResult);
            },
            error -> {
                LOGGER.debug("Error encountered during fetch of Category collection", error);
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(error, Operation.READ);
                readResult.setValue(operationResult);
            },
            getHttpHeaders());
    }

    /**
     * This version of the read operation is used to get the related objects to a
     * given entity.
     *
     * @param parent - the original entity, the starting point of the navigation
     * @param navigationPropertyName - the name of the link to the related entity set
     */
    public void read(EntityValue parent, String navigationPropertyName) {
        relatedEntities.clear();

        Property navigationProperty = parent.getEntityType().getProperty(navigationPropertyName);
        DataQuery dataQuery = new DataQuery();
        if (navigationProperty.isCollection() && orderByProperty != null) {
            dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING);
        }
        v2.loadPropertyAsync(navigationProperty, parent, dataQuery,
            () -> {
                DataValue relatedData = parent.getDataValue(navigationProperty);
                relatedEntities = new ArrayList<>();
                switch (navigationProperty.getDataType().getCode()) {
                    case DataType.ENTITY_VALUE_LIST:
                        relatedEntities = this.<T>convert((EntityValueList) relatedData);
                        break;
                    case DataType.ENTITY_VALUE:
                        if (relatedData != null) {
                          EntityValue entity = (EntityValue) relatedData;
                          relatedEntities.add((T) entity);
                        }
                        break;
                }
                initialReadDone = true;
                // Update observables
                observableEntities.setValue(relatedEntities);
                OperationResult<T> operationResult = new OperationResult<>(Operation.READ);
                readResult.setValue(operationResult);
            },
            (error) -> {
                LOGGER.debug("Error encountered during fetch of Category collection", error);
                OperationResult<T> operationResult;
                operationResult = new OperationResult<T>(error, Operation.READ);
                readResult.setValue(operationResult);
            },
            getHttpHeaders());
    }

    /**
     * Create method for Entity type that is a Media Linked Entity
     * caller must provide the media resource associated with the MLE
     * @param newEntity - the MLE entity instance
     * @param media - byte or character stream of the media resource
     */
    public void create(@NonNull T newEntity, @NonNull StreamBase media) {
        if (newEntity.getEntityType().isMedia()) {
            v2.createMediaAsync(newEntity, media,
                () -> {
                    insertToCache(newEntity, entities);
                    if (relatedEntities.size() > 0) {
                        insertToCache(newEntity, relatedEntities);     
                        observableEntities.setValue(relatedEntities);
                    } else {
                        observableEntities.setValue(entities);
                    }
                    OperationResult<T> operationResult;
                    operationResult = new OperationResult<>(newEntity, Operation.CREATE);
                    createResult.setValue(operationResult);
                },
                (error) -> {
                    LOGGER.debug("Media Linked Entity creation failed:", error);
                    OperationResult<T> operationResult;
                    operationResult = new OperationResult<>(error, Operation.CREATE);
                    createResult.setValue(operationResult);
                });
        }
    }

    /**
     * Create method for the entity set
     * @param newEntity - entity to create
     */
    public void create(@NonNull T newEntity) {
        if (newEntity.getEntityType().isMedia()) {
            OperationResult<T> operationResult;
            operationResult = new OperationResult<T>(new IllegalStateException("Specify media resource for Media Linked Entity"), Operation.CREATE);
            createResult.setValue(operationResult);
            return;
        }
        v2.createEntityAsync(newEntity,
            () -> {
                insertToCache(newEntity, entities);
                if (relatedEntities.size() > 0) {
                    insertToCache(newEntity, relatedEntities);
                    observableEntities.setValue(relatedEntities);
                } else {
                    observableEntities.setValue(entities);
                }
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(newEntity, Operation.CREATE);
                createResult.setValue(operationResult);
            },
            error -> {
                LOGGER.debug("Entity creation failed:", error);
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(error, Operation.CREATE);
                createResult.setValue(operationResult);
            });
    }

    /**
     * Update method for the entity set
     * @param updateEntity - entity to update
     */
    public void update(@NonNull T updateEntity) {
        v2.updateEntityAsync(updateEntity,
            () -> {
                replaceInCache(updateEntity, entities);
                if (relatedEntities.size() > 0) {
                    replaceInCache(updateEntity, relatedEntities);
                    observableEntities.setValue(relatedEntities);
                } else {
                    observableEntities.setValue(entities);
                }
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(updateEntity, Operation.UPDATE);
                updateResult.setValue(operationResult);
            },
            error -> {
                LOGGER.debug("Error encountered during update of entity", error);
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(error, Operation.UPDATE);
                updateResult.setValue(operationResult);
            });
    }

    /**
     * Delete method for the entity set
     * @param deleteEntities - list of entities to be deleted
     *
     * Implementation uses a ChangeSet to guarantee that either all specified entities are deleted or none
     * For best effort delete, multiple ChangeSets within a Batch can be used
     */
    public void delete(@NonNull List<T> deleteEntities) {
        ChangeSet deleteChangeSet = new ChangeSet();
        for (T entityToDelete: deleteEntities) {
            deleteChangeSet.deleteEntity(entityToDelete);
        }
        v2.applyChangesAsync(deleteChangeSet,
            () -> {
                // Change Set success means all deletes are completed
                for (T entityToDelete: deleteEntities) {
                    if (relatedEntities.size() > 0) {
                        removeFromCache(entityToDelete, relatedEntities);
                    }
                    removeFromCache(entityToDelete, entities);
                }
                
                if (relatedEntities.size() > 0) {
                    observableEntities.setValue(relatedEntities);
                } else {
                    observableEntities.setValue(entities);
                }
                OperationResult<T> operationResult;
                operationResult = new OperationResult<>(deleteEntities, Operation.DELETE);
                deleteResult.setValue(operationResult);
            },
            error -> {
                LOGGER.debug("Error encountered during deletion of entities:", error);
                OperationResult operationResult;
                operationResult = new OperationResult<>(error, Operation.DELETE);
                deleteResult.setValue(operationResult);
            });
    }


    /**
     * For use by View Model to populate the repository. Only if an initial read has not been done will
     * an attempt be made to read in data from the collection.
     * @param failureHandler
     */
    public void initialRead(@Nullable Action0 successHandler, @NonNull Action1<RuntimeException> failureHandler) {
        relatedEntities.clear();

        if (initialReadDone && entities.size() > 0) {
            observableEntities.setValue(entities);
            return;
        }

        DataQuery dataQuery = new DataQuery().from(entitySet);
        if (orderByProperty != null) {
            dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING);
        }
        v2.executeQueryAsync(dataQuery,
            result -> {
                List<T> entitiesRead = this.<T>convert(result.getEntityList());
                entities.clear();
                entities.addAll(entitiesRead);
                initialReadDone = true;
                observableEntities.setValue(entitiesRead);
                if (successHandler != null) {
                    successHandler.call();
                }
            },
            failureHandler,
            getHttpHeaders());
    }

    /*
     * A simple function to convert from generic EntityValueList to type specified list
     */
    @NonNull
    private ArrayList<T> convert(@NonNull EntityValueList entityValueList) {
        ArrayList<T> result = new ArrayList<>(entityValueList.length());
        Iterator iterator = entityValueList.iterator();
        while (iterator.hasNext()) {
            result.add((T)iterator.next());
        }
        return result;
    }

    /**
     * Insert the new entity into cache and in order if needed
     * @param newEntity
     * @param cache
     */
    private void insertToCache(@NonNull T newEntity, List<T> cache) {
        if (orderByProperty != null) {
            insertOrderByProperty(newEntity, cache);
        } else {
            cache.add(0, newEntity);
        }
    }

    /*
     * Replace the entity in cache that has the same key(s) of the updated entity
     * Since we do not know if the value for order by property has been change, we have to do remove
     * followed by insert
     * Note: implementation should be optimized to obtain better than linear scaling for very large collection
     * @param updateEntity - updated entity to be replaced with
     * @param cache - entity or related entity cache
     */
    private void replaceInCache(@NonNull T updateEntity, List<T> cache) {
        int index = 0;
        for (T entity: cache) {
                if (EntityValue.equalKeys(entity, updateEntity)) {
                    if (orderByProperty != null) {
                        cache.remove(index);
                        insertOrderByProperty(updateEntity, cache);
                    } else {
                        cache.set(index, updateEntity);
                    }
                    break;
                }
            index++;
        }
    }

    /**
     * Remove the specified entity from cache
     * Note: implementation should be optimized to obtain better than linear scaling for very large collection
     * @param deleteEntity - deleted entity to be removed from cache
     * @param cache - entity or related entity cache
     */
    private void removeFromCache(@NonNull T deleteEntity, List<T> cache) {
        int index = 0;
        for (T entity: cache) {
                if (EntityValue.equalKeys(entity, deleteEntity)) {
                cache.remove(index);
                break;
            }
            index++;
        }
    }

   /**
     * Insert the new entity into the cache list based on list is sorted in ascending order
     * It is possible that we have a null for the value of the order by property. In that case
     * we will assign a default string.
     * @param entity to insert
     * @param cache - used storage
     */
    private void insertOrderByProperty(@NonNull T entity, List<T> cache) {
        DataValue propertyValue = entity.getDataValue(orderByProperty);
        String insertOrderByPropertyString = " ";
        String listOrderByPropertyString;
        if (propertyValue != null) {
            insertOrderByPropertyString = propertyValue.toString();
        }
        int index = 0;
        for (T listEntity: cache) {
            if (listEntity.getDataValue(orderByProperty) == null) {
                listOrderByPropertyString = " ";
            } else {
                listOrderByPropertyString = listEntity.getDataValue(orderByProperty).toString();
            }
            if (insertOrderByPropertyString.compareTo(listOrderByPropertyString) < 0) {
                cache.add(index, entity);
                return;
            }
            index++;
        }
        cache.add(entity);
    }

    /**
     * Return a suitable HttpHeader based on whether full metadata parameter is required
     * @return HttpHeader for query
     */
    @NonNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders;
        if (needFullMetadata) {
            httpHeaders = new HttpHeaders();
            httpHeaders.set("Accept", "application/json;odata.metadata=full");
        } else {
            httpHeaders = HttpHeaders.empty;
        }
        return httpHeaders;
    }
    
    /**
     * Repository provides an empty data list, but the in-memory cache is retained. Calling
     * read clears the cache, as well.
     */
	public void clear() {
	    observableEntities.setValue(new ArrayList<T>());
	}
}
