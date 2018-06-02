package se.tink.backend.common.tasks.interfaces;

import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.Future;
import se.tink.backend.system.tasks.Task;


/**
 * Task submitter submits task to a task queue (or handler).
 */
public interface TaskSubmitter {
    /**
     * Submit a task.
     *
     * @param task the task to be submitted.
     * @return a future to be able to make this a synchronous task. The future will always return `null` to avoid coupling to task queue implementation.
     */
    Future<Void> submit(Task<?> task);
}
