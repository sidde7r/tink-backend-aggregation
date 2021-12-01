package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.StringCleaningDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class HandelsbankenFICreditCardTransaction {

    private HandelsbankenAmount amount;

    @JsonFormat(pattern = "d.M.y")
    private Date date;

    @JsonDeserialize(using = StringCleaningDeserializer.class)
    private String description;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {
        return CreditCardTransaction.builder()
                .setAmount(amount.toExactCurrencyAmount().negate())
                .setDate(date)
                .setCreditCardAccountNumber(account != null ? account.getAccountNumber() : null)
                .setDescription(description)
                .build();
    }
}
