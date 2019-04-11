package se.tink.backend.aggregation.storage.debug;

import java.io.File;
import java.io.IOException;

public interface AgentDebugStorageHandler {

    String store(String content, File file) throws IOException;

    boolean isLocalStorage();
}
