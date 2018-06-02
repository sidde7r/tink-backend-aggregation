package se.tink.backend.connector.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.api.VersionService;
import se.tink.backend.connector.rpc.VersionResponse;

public class VersionServiceResource implements VersionService {

    private VersionResponse versionResponse;

    public VersionServiceResource() {
        try {
            versionResponse = SerializationUtils.deserializeFromString(
                    Files.toString(new File("data/version.json"), Charsets.UTF_8), VersionResponse.class);
        } catch (Exception e) {
            versionResponse = new VersionResponse();
            versionResponse.setVersion("DEVELOPMENT");
        }
    }

    @Override
    public VersionResponse getVersion() {
        return versionResponse;
    }
}
