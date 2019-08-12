package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class ReservedTransactionQuery implements RequestComponent {
    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("SEB_KUND_NR")
    private String customerNumber;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonCreator
    public ReservedTransactionQuery(
            @JsonProperty("SEB_KUND_NR") String customerNumber,
            @JsonProperty("KONTO_NR") String accountNumber,
            @JsonProperty("ROW_ID") Integer rowId) {
        this.customerNumber = customerNumber;
        this.accountNumber = accountNumber;
        this.rowId = rowId;
    }

    @JsonIgnore
    public String getCustomerNumber() {
        return customerNumber;
    }

    @JsonIgnore
    public String getAccountNumber() {
        return accountNumber;
    }
}
