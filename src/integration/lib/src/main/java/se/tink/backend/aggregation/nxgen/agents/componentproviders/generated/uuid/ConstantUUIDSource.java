package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid;

import java.util.UUID;

public final class ConstantUUIDSource implements UUIDSource {

    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";

    @Override
    public UUID getUUID() {
        return java.util.UUID.fromString(VALID_V4_UUID);
    }
}
