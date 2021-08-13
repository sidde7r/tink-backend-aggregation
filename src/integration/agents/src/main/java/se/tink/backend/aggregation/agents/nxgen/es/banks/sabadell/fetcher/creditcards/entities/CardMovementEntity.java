package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Getter
public class CardMovementEntity {
    private String movementNumber;
    private String concept;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;

    private String hour;
    private String city;
    private boolean canSplit;
    private AmountEntity amount;
    private boolean indFracEnabled;
    private String indMov;
    private boolean isTraspasable;
    private AmountEntity commission;
    private AmountEntity originAmount;
    private String address;
    private boolean isConfirmed;
    private boolean isSplit;

    @JsonProperty("siaidcdmov")
    private String externalId;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount creditCardAccount) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(creditCardAccount)
                        .setAmount(amount.parseToNegativeTinkAmount())
                        .setDate(date)
                        .setDescription(concept)
                        .setPending(!isConfirmed)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                externalId)
                        .setMutable(!isConfirmed)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setValueDate(new AvailableDateInformation(date))
                                        .setBookingDate(new AvailableDateInformation(date))
                                        .setExecutionDate(new AvailableDateInformation(date))
                                        .build())
                        .setProviderMarket(MarketCode.ES.name())
                        .build();
    }

    public boolean isCanSplit() {
        return canSplit;
    }

    public boolean isIndFracEnabled() {
        return indFracEnabled;
    }

    public boolean isTraspasable() {
        return isTraspasable;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public boolean isSplit() {
        return isSplit;
    }
}
