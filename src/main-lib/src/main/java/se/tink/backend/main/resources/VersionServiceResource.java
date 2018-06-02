package se.tink.backend.main.resources;

import com.google.inject.Inject;
import javax.ws.rs.Path;
import se.tink.backend.api.VersionService;
import se.tink.backend.common.VersionInformation;
import se.tink.backend.rpc.VersionResponse;

@Path("/api/v1/version")
public class VersionServiceResource implements VersionService {

    private final VersionResponse versionResponse = new VersionResponse();

    @Inject
    public VersionServiceResource(VersionInformation versionInfo) {
        VersionInformation.Version version = versionInfo.getVersion();
        versionResponse.setCommit(version.getCommit());
        versionResponse.setDate(version.getDate());
        versionResponse.setVersion(version.getVersion());
    }

    @Override
    public VersionResponse getVersion() {
        return versionResponse;
    }
}
