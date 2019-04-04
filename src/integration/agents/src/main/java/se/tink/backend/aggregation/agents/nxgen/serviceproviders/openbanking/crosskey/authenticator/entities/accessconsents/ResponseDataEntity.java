package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ResponseDataEntity {

    private String consentId;
    private String creationDateTime;
    private String expirationDateTime;
    private List<String> permissions;
    private String status;
    private String statusUpdateDateTime;
    private Date transactionFromDateTime;
    private Date transactionToDateTime;

    public String getConsentId() {
        return consentId;
    }

    public String getStatus() {
        return status;
    }

    public Date getTransactionFromDateTime() {
        return transactionFromDateTime;
    }

    public Date getTransactionToDateTime() {
        return transactionToDateTime;
    }
}
