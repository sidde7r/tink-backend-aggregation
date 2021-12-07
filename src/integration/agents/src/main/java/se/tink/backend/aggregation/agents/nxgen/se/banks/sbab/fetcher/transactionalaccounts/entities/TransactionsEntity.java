package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class TransactionsEntity {
    private String accountNumberFrom;
    private String accountNumberTo;
    private BigDecimal amount;
    private String bankNameFrom;
    private String bankNameTo;
    private String narrativeFrom;
    private String narrativeTo;
    private BigDecimal runningBalance;
    private String transactionIdentifier;
    private String transferType;
    private Date valueDate;

    public Transaction toTinkTransaction() {

        Builder builder =
                Transaction.builder()
                        .setAmount(getAmount())
                        .setType(getTransferType())
                        .setRawDetails(getRawDetails())
                        .setDate(getValueDate())
                        .setDescription(getNarrative());

        if (!Strings.isNullOrEmpty(transactionIdentifier)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                    transactionIdentifier);
        }

        return builder.build();
    }

    public String getNarrative() {
        return Optional.ofNullable(getNarrativeFrom()).orElse(getNarrativeTo());
    }

    public String getRawDetails() {
        return getAccountNumberFrom()
                + getAccountNumberTo()
                + getBankNameFrom()
                + getBankNameTo()
                + getTransactionIdentifier();
    }

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, SBABConstants.CURRENCY);
    }

    public TransactionTypes getTransferType() {
        return SBABConstants.TRANSACTION_TYPES.get(transferType);
    }
}
