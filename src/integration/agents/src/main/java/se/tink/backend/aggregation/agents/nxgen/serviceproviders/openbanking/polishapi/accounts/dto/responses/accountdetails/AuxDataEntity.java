package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class AuxDataEntity {
    private String owner;
    private String interestRate;
    private String prevPeriodInterest;
    private String currentMinDue;
    private String minOutstandingDue;
    private String lastStatementDue;
    private String nextStatementDue;
    private String totalMinDue;
    private String paymentBy;
    private String shadowDebit;
    private String automaticPayment;
    private String limit;
    private String user;
}
