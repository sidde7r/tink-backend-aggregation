package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class EasyPinEntity {
    private final String response;
}
