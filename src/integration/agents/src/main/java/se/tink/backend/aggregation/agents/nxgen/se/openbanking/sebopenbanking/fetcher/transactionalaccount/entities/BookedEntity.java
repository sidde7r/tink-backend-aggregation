package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class BookedEntity {
    private String transactionId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    private String entryReference;

    private String descriptiveText;

    private TransactionAmountEntity transactionAmount;

    private String balanceAfterTransaction;

    private int proprietaryBankTransactionCode;

    private String proprietaryBankTransactionCodeText;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public Transaction toTinkTransaction(SebApiClient apiClient) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.getAmount())
                        .setDate(bookingDate)
                        .setDescription(getDescription(apiClient))
                        .setPending(false)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(proprietaryBankTransactionCodeText)
                        .setProviderMarket(SebConstants.MARKET);

        return (Transaction) builder.build();
    }

    @JsonIgnore
    private String getDescription(SebApiClient apiClient) {
        // In case of Foreign transactions we don't get good enough information in 'descriptiveText'
        if (proprietaryBankTransactionCode == 10 && !Objects.isNull(links)) {
            TransactionDetailsEntity detailsEntity =
                    apiClient.fetchTransactionDetails(links.getTransactions().getHref());
            return detailsEntity.getCardAcceptorId();
        } else {
            return descriptiveText;
        }
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(valueDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
