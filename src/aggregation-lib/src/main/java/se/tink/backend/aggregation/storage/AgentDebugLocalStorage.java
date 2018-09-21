package se.tink.backend.aggregation.storage;

import java.io.File;
import java.io.IOException;
import com.google.common.io.Files;
import com.google.common.base.Charsets;

public class AgentDebugLocalStorage implements AgentDebugStorageHandler{

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String store(String content, File file) throws IOException {
        Files.write(content, file, Charsets.UTF_8);
        return file.getAbsolutePath();
    }
}
