package se.tink.backend.aggregation.s3storage;

import java.io.IOException;

public interface AgentDebugStorageHandler {

    boolean isAvailable();

    String store(String name, String content) throws IOException;

}
