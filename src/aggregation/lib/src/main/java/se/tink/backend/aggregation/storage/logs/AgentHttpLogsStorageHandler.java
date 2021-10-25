package se.tink.backend.aggregation.storage.logs;

import java.io.IOException;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.HttpLogType;

/**
 * This class is responsible for the actual saving of logs to different types of storage, e.g. local
 * storage or s3.
 */
public interface AgentHttpLogsStorageHandler {

    String storeLog(String content, String filePath, HttpLogType logType) throws IOException;

    boolean isEnabled();
}
