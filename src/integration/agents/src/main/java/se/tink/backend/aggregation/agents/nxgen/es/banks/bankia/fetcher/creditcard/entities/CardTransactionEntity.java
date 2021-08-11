package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class CardTransactionEntity {

    @JsonProperty("identificadorMovimiento")
    private String transactionId;

    @JsonProperty("importeMovimiento")
    private AmountEntity movementAmount;

    @JsonProperty("fechaMovimiento")
    private DateEntity date;

    @JsonProperty("lugarMovimiento")
    private String localization;

    @JsonProperty("descripcionMovimiento")
    private String description;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount creditCardAccount) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setAmount(movementAmount.toTinkAmount())
                        .setDate(date.toJavaLangDate())
                        .setDescription(getDescription())
                        .setRawDetails(this)
                        .setCreditAccount(creditCardAccount)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(
                                                new AvailableDateInformation(date.getLocalDate()))
                                        .build())
                        .setPending(false)
                        .setMutable(false)
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }

    private String getDescription() {
        return Optional.ofNullable(localization)
                .map(loc -> description + ", " + loc)
                .orElse(description);
    }
}
