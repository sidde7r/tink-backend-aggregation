package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Getter
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private AmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountInfoEntity creditorAccount;
    private String debtorName;
    private TransactionAccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private List<String> remittanceInformationUnstructuredArray;
    private TransactionDetailsLinksEntity links;

    public Transaction toBookedTinkTransaction(String providerMarket) {
        return toTinkTransaction(false, providerMarket);
    }

    public Transaction toPendingTinkTransaction(String providerMarket) {
        return toTinkTransaction(true, providerMarket);
    }

    public Transaction toTinkTransaction(boolean isPending, String providerMarket) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toAmount())
                        .setDate(bookingDate)
                        .setDescription(getDescription())
                        .setPending(isPending)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProviderMarket(providerMarket);

        return (Transaction) builder.build();
    }

    private String getDescription() {
        if (CollectionUtils.isNotEmpty(remittanceInformationUnstructuredArray)) {
            return remittanceInformationUnstructuredArray.get(0);
        }
        return remittanceInformationUnstructured;
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(valueDate));

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }
}
