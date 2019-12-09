package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyEntity {
    private final List<KeysEntity> keys;

    public KeyEntity(List<KeysEntity> keys) {
        this.keys = keys;
    }
}
