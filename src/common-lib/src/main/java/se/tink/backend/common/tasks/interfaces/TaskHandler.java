package se.tink.backend.common.tasks.interfaces;

import java.util.Set;
import se.tink.backend.system.tasks.Task;

/**
 * Handles a deserialized job if it is an instance of any of {@link #handles()}.
 * 
 * @see also {@link GenericTaskHandler}.
 */
public interface TaskHandler<T extends Task<?>> {
    Set<Class<? extends T>> handles();

    void handle(Task<?> deserializedPayload);
}
