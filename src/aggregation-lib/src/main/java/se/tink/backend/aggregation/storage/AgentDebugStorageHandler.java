package se.tink.backend.aggregation.storage;

import java.io.File;
import java.io.IOException;

public interface AgentDebugStorageHandler {

    boolean isAvailable();

    String store(String content, File file) throws IOException;

}
