package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.SebConstants.Urls.PROVIDER_MARKET;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PendingEntity {

    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private String creditorName;

    private TransactionAmountEntity transactionAmount;

    private String pendingType;

    private String remittanceInformationStructuredReference;

    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(transactionAmount.getAmount())
                        .setDate(valueDate)
                        .setDescription(creditorName)
                        .setPending(true)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionReference(remittanceInformationStructuredReference)
                        .setProviderMarket(PROVIDER_MARKET)
                        .build();
    }
}
