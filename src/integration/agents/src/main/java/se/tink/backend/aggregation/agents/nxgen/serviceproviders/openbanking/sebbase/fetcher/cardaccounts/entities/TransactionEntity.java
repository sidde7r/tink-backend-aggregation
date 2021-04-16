package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    /** Description: Booking date expressed as ISODate in the YYYY-MM-DD format */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    /** Description: Value date expressed as ISODate in the YYYY-MM-DD format */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String cardAcceptorCity;
    private String cardAcceptorCountryCode;
    private String cardTransactionId;
    private ExchangeRateEntity exchangeRate;
    private Boolean invoiced;
    private String maskedPan;
    private String nameOnCard;
    private OriginalAmountEntity originalAmount;
    private String proprietaryBankTransactionCode;
    private TransactionAmountEntity transactionAmount;
    private String transactionDetails;

    public String getMaskedPan() {
        return Strings.emptyToNull(maskedPan);
    }

    public String getNameOnCard() {
        return Strings.emptyToNull(nameOnCard);
    }

    public CreditCardTransaction toTinkTransaction(boolean isPending) {
        Builder builder =
                CreditCardTransaction.builder()
                        .setAmount(transactionAmount.getTinkAmount())
                        .setCreditCard(CreditCard.create(getNameOnCard(), getMaskedPan()))
                        .setPending(isPending)
                        .setDate(valueDate)
                        .setDescription(transactionDetails)
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                cardTransactionId)
                        .setProprietaryFinancialInstitutionType(proprietaryBankTransactionCode);

        return (CreditCardTransaction) builder.build();
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
