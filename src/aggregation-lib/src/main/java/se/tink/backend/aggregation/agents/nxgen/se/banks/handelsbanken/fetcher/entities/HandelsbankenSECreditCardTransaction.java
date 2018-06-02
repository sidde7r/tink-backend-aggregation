package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.StringCleaningDeserializer;
import se.tink.backend.core.Amount;

@JsonObject
public class HandelsbankenSECreditCardTransaction {
    private static final Pattern PENDING_PATTERN = Pattern.compile("^prel\\.?(\\s)?", Pattern.CASE_INSENSITIVE);

    private HandelsbankenAmount amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    @JsonDeserialize(using = StringCleaningDeserializer.class)
    private String description;

    public CreditCardTransaction toTinkTransaction(HandelsbankenSECreditCard creditcard, CreditCardAccount account) {
        String formattedDescription = description;
        boolean pending = false;

        Matcher matcher = PENDING_PATTERN.matcher(formattedDescription);
        if (matcher.find()) {
            formattedDescription = matcher.replaceFirst("");
            pending = true;
        }

        return CreditCardTransaction.builder()
                .setAmount(Amount.inSEK(calculateAmount(creditcard)))
                .setDate(date)
                .setDescription(formattedDescription)
                .setPending(pending)
                .setCreditAccount(account)
                .build();
    }

    private Double calculateAmount(
            HandelsbankenSECreditCard creditcard) {
        Double amount = this.amount.asDouble();
        if (creditcard != null && creditcard.hasInvertedTransactions()) {
            return -amount;
        }
        return amount;
    }
}
