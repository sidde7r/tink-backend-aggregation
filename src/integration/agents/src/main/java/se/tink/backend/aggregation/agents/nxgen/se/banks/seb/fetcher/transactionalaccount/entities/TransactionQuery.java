package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class TransactionQuery implements RequestComponent {
    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("SEB_KUND_NR")
    private String customerNumber;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("PCB_BOKFDAT_BLADDR")
    private String fromDate;

    @JsonProperty("BELOPP_FROM")
    private Integer fromAmount;

    @JsonProperty("BELOPP_TOM")
    private Integer toAmount;

    @JsonProperty("TRANSLOPNR")
    private Integer fromBatchNumber;

    @JsonProperty("MAX_ROWS")
    private Integer maxRows;

    public TransactionQuery() {}

    @JsonIgnore
    public TransactionQuery(String customerNumber, String accountNumber, Integer maxRows) {
        this.customerNumber = customerNumber;
        this.accountNumber = accountNumber;
        this.maxRows = maxRows;
    }

    @JsonIgnore
    public TransactionQuery(
            String customerNumber,
            String accountNumber,
            Integer maxRows,
            LocalDate fromDate,
            Integer batchNumber) {
        this.customerNumber = customerNumber;
        this.accountNumber = accountNumber;
        this.maxRows = maxRows;
        this.fromDate = fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.fromBatchNumber = batchNumber;
    }

    @JsonIgnore
    public Integer getMaxRows() {
        return maxRows;
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
