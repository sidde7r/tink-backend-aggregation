package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@RequiredArgsConstructor
@JsonObject
public class DataSetEntity {
    private final String value;
    private final String key;
}
