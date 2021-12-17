package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities.transactions;

import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;

@JsonObject
public class TransactionsItemEntity {

    private String transactionId;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private Date bookingDate;
    private Date transactionDate;

    public Transaction toTinkTransactions(boolean pending) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toAmount())
                        .setDate(bookingDate != null ? bookingDate : transactionDate)
                        .setDescription(remittanceInformationUnstructured)
                        .setPending(pending);

        if (!Strings.isNullOrEmpty(transactionId)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, transactionId);
        }

        return builder.build();
    }
}
