package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid;

import java.util.UUID;

public final class RandomUUIDSource implements UUIDSource {

    public UUID getUUID() {
        return UUID.randomUUID();
    }
}
