package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities.DataRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountSetupRequest {
    @JsonProperty("Data")
    private DataRequestEntity data = new DataRequestEntity();
    @JsonProperty("Risk")
    private RiskEntity risk = new RiskEntity();

    public AccountSetupRequest setup(List<String> permissions, Date fromDate, Date toDate, Date expirationDate)
    {
        data.setPermissions(permissions);
        data.setTransactionFromDateString(formatDate(fromDate));
        data.setTransactionToDateString(formatDate(toDate));
        data.setExpirationDateString(formatDate(expirationDate));
        return this;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
