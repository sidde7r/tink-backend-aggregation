package se.tink.backend.aggregation.storage.debug;

import java.io.IOException;
import se.tink.backend.aggregation.storage.debug.handlers.AgentDebugLogConstants.AgentDebugLogBucket;

/**
 * This class is responsible for the actual saving of logs to different types of storage, e.g. local
 * storage or s3.
 */
public interface AgentDebugLogStorageHandler {

    String storeDebugLog(String content, String filePath, AgentDebugLogBucket bucket)
            throws IOException;

    boolean isEnabled();
}
