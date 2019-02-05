package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;

public class TransactionEntity extends LinksResponse {
    private String transactionId;
    @JsonFormat(pattern = "y-M-d")
    private Date date;
    private String creditorName;
    private AmountEntity amount;

    public String getTransactionId() {
        return transactionId;
    }

    public Date getDate() {
        return date;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
