package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication.MaintenanceMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication.ServerStatusProperties;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServerStatusResponse {
    private ServerStatusProperties properties;
    private List<MaintenanceMessageEntity> maintenanceMessages;

    public ServerStatusProperties getProperties() {
        return properties;
    }

    public List<MaintenanceMessageEntity> getMaintenanceMessages() {
        return maintenanceMessages;
    }
}
