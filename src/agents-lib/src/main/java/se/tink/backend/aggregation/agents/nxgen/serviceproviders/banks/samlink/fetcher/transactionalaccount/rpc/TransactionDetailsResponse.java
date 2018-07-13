package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionDetailsResponse extends LinksResponse {
    private String transactionId;
    @JsonFormat(pattern = "y-M-d")
    private Date date;
    private AccountIdEntity creditorAccountId;
    private String creditorName;
    private String debtorName;
    private AmountEntity amount;
    private String message;

    public String getTransactionId() {
        return transactionId;
    }

    public Date getDate() {
        return date;
    }

    public AccountIdEntity getCreditorAccountId() {
        return creditorAccountId;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getMessage() {
        // Example message: `test1\n20171220MPAOKI-20171220593619027072`
        return message.split("\n")[0];
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(message)
                .setAmount(amount.toTinkAmount())
                .setDate(date)
                .build();
    }
}
