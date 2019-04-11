package se.tink.backend.aggregation.storage.debug;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;

public class AgentDebugLocalStorage implements AgentDebugStorageHandler {

    @Override
    public String store(String content, File file) throws IOException {
        Files.write(content, file, Charsets.UTF_8);
        return file.getAbsolutePath();
    }

    @Override
    public boolean isLocalStorage() {
        return true;
    }
}
