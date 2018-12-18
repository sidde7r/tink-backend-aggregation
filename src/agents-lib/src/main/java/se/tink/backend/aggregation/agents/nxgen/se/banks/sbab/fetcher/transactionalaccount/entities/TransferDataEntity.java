package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferDataEntity {
    @JsonProperty("counter_part_account")
    private String counterPartAccount;

    @JsonProperty("amount_to_transfer")
    private long amountToTransfer;

    @JsonProperty("statement")
    private String statement;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transfer_date")
    private Date transferDate;

    @JsonProperty("counter_part_statement")
    private String counterPartStatement;

    public String getCounterPartAccount() {
        return counterPartAccount;
    }

    public long getAmountToTransfer() {
        return amountToTransfer;
    }

    public String getStatement() {
        return statement;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public String getCounterPartStatement() {
        return counterPartStatement;
    }
}
