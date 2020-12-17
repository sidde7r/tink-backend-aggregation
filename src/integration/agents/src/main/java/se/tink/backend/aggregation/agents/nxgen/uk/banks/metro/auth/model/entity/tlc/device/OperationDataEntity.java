package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class OperationDataEntity {
    private final CollectionResultEntity collectionResult;

    private final PublicKeyEntity publicKey;

    private final PublicKeyEntity encryptionPublicKey;

    @JsonProperty("params")
    private final ParametersEntity parameters;

    private final String policyRequestId;
}
