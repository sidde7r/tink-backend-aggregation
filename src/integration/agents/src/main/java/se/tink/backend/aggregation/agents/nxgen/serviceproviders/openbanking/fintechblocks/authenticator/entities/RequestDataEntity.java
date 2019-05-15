package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestDataEntity {

    @JsonProperty("Permissions")
    private List<String> permissions;

    @JsonProperty("ExpirationDateTime")
    private String expirationDateTime;

    @JsonProperty("TransactionFromDateTime")
    private String transactionFromDateTime;

    @JsonProperty("TransactionToDateTime")
    private String transactionToDateTime;

    public RequestDataEntity(
            List<String> permissions,
            String expirationDateTime,
            String transactionFromDateTime,
            String transactionToDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
        this.transactionFromDateTime = transactionFromDateTime;
        this.transactionToDateTime = transactionToDateTime;
    }
}
