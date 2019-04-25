package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class RequestDataEntity {

    private String expirationDateTime;
    private List<String> permissions;
    private String transactionFromDateTime;
    private String transactionToDateTime;

    public RequestDataEntity(
            String expirationDateTime,
            List<String> permissions,
            String transactionFromDateTime,
            String transactionToDateTime) {
        this.expirationDateTime = expirationDateTime;
        this.permissions = permissions;
        this.transactionFromDateTime = transactionFromDateTime;
        this.transactionToDateTime = transactionToDateTime;
    }
}
