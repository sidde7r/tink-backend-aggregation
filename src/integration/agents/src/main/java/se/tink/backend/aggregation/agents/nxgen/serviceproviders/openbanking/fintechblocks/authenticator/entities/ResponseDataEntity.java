package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseDataEntity {

    @JsonProperty("ConsentId")
    private String consentId;

    @JsonProperty("CreationDateTime")
    private String creationDateTime;

    @JsonProperty("ExpirationDateTime")
    private String expirationDateTime;

    @JsonProperty("Permissions")
    private List<String> permissions;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("StatusUpdateDateTime")
    private String statusUpdateDateTime;

    @JsonProperty("TransactionFromDateTime")
    private String transactionFromDateTime;

    @JsonProperty("TransactionToDateTime")
    private String transactionToDateTime;

    public String getConsentId() {
        return consentId;
    }
}
