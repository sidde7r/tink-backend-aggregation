package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NemidParamsEntity {
    private String clientflow;
    private String digestSignature;
    private String enableAwaitingAppApprovalEvent;
    private String language;
    private String origin;
    private String paramsDigest;
    private String rememberUserid;
    private String samlRequest;
    private String suppressPushToDevice;
    private String timestamp;
}
