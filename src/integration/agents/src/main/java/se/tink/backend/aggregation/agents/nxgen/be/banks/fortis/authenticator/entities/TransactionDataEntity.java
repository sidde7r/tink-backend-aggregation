package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class TransactionDataEntity {
    private final String tokenId;
    private final String securityType;
    private final String signatureType;
}
