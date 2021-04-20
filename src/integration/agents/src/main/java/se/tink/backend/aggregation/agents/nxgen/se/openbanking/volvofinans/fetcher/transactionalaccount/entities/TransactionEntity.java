package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private String transactionId;
    private String proprietaryBankTransactionCode;
    private String transactionDetails;
    private TransactionAmount transactionAmount;
    private TransactionAmount originalAmount;
    private TransactionAmount foreignTransactionFee;
    private TransactionAmount withdrawalFee;
    private TransactionAmount discount;
    private TransactionAmount loyaltyCheck;
    private String fromAccount;
    private String toAccount;
    private String bank;
    private String clearingNumber;
    private String bankAccountNumber;
    private String ownMessage;

    public Transaction toTinkTransaction(boolean isPending) {
        Builder builder =
                Transaction.builder()
                        .setPending(isPending)
                        .setAmount(transactionAmount.toAmount())
                        .setDate(transactionDate)
                        .setDescription(transactionDetails)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setProprietaryFinancialInstitutionType(proprietaryBankTransactionCode)
                        .setProviderMarket(VolvoFinansConstants.PROVIDER_MARKET);

        return (Transaction) builder.build();
    }
}
