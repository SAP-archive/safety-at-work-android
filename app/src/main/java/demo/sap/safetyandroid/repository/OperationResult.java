package demo.sap.safetyandroid.repository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * Generic type to report CRUD operation result for entity type as type
 */
public class OperationResult<T> {

    /*
     * CRUD Operations
     */
    public enum Operation {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    /*
     * List of entities for the entity type returned as a result of the operation
     */
    private List<T> result;

    /*
     * Exception encountered performing operation
     */
    private Exception error;

    /*
     * Operation performed
     */
    private Operation operation;

    /**
     * Used by READ as query results are notified via LiveData
     * @param op
     */
    public OperationResult(@NonNull Operation op) {
        this.result = null;
        this.operation = op;
    }

    /**
     * Used by either UPDATE or CREATE  modified/created entity returned
     * @param result -  modified/created entity returned
     * @param op
     */
    public OperationResult(@NonNull T result, @NonNull Operation op) {
        this.result = new ArrayList<>();
        this.result.add(result);
        this.operation = op;
    }

    /**
     * Used by delete to confirm items in the list have been removed successfully
     * @param result - List of entities deleted
     * @param op
     */
    public OperationResult(@NonNull List<T> result, @NonNull Operation op) {
        this.result = result;
        this.operation = op;
    }

    /**
     * Used to report an exception encountered during execution
     * @param error
     * @param op
     */
    public OperationResult(@NonNull Exception error, @NonNull Operation op) {
        this.error = error;
        this.operation = op;
    }

    @NonNull
    public Operation getOperation() {
        return this.operation;
    }

    @Nullable
    public List<T> getResult() {
        return result;
    }

    @Nullable
    public T getSingleResult() { return result.get(0); }

    @Nullable
    public Exception getError() {
        return error;
    }
}