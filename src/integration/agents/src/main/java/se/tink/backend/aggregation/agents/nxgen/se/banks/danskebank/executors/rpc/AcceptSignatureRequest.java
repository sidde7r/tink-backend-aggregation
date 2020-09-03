package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Builder
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AcceptSignatureRequest {
    private String signatureId;
    private String signaturePackage;
    private String language;
}
