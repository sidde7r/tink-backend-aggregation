package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class CardTransactionEntity {

    @JsonProperty("movementdescription")
    private String movementDescription;

    @JsonProperty("movementamount")
    private String movementAmount;

    @JsonFormat(pattern = "yyyyMMdd")
    @JsonProperty("movementdate")
    private LocalDate movementDate;

    private String currency;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setAmount(ExactCurrencyAmount.of(movementAmount, getCurrency()))
                        .setDate(movementDate)
                        .setDescription(movementDescription)
                        .setPending(false)
                        .setMutable(false)
                        .setTransactionDates(getTransactionDates())
                        .setProviderMarket(MarketCode.ES.toString())
                        .setRawDetails(this)
                        .build();
    }

    private String getCurrency() {
        return Strings.isNullOrEmpty(currency) ? "EUR" : currency;
    }

    private TransactionDates getTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation(movementDate))
                .setBookingDate(new AvailableDateInformation(movementDate))
                .build();
    }
}
