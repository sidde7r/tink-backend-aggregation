package se.tink.backend.common.tasks.interfaces;


/**
 * Handles a "raw" (not deserialized) task.
 * 
 * @see also {@link TaskHandler}.
 */
public interface GenericTaskHandler {
    void handle(byte[] payload);
}
