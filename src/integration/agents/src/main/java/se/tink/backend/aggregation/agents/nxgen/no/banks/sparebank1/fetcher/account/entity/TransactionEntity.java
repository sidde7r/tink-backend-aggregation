package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public class TransactionEntity {
    private String id;
    private BigDecimal amount;
    private String description;
    private long date;
    private String currencyCode;
    private String type;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currencyCode))
                .setDate(new Date(date))
                .setDescription(
                        description == null ? type : getTinkFormattedDescription(description))
                .setPending(id == null)
                .build();
    }

    @JsonIgnore
    private String getTinkFormattedDescription(String rawDescription) {
        /*
        SpareBank 1 sometimes add a lot of meta information to the transaction description which
        we need to remove in order for categorization to work, so far we've seen the following patterns:

        Pattern: <date> <Actual merchant name>
        Example: 01.01 Spotify
        Formatted: Spotify

        Pattern: *<last four digits of cardnumber> <date> <currency> <amount> <Actual merchant name> Kurs: <exchange rate>
        Example: *1234 01.01 SEK 99.00 Spotify Kurs: 1.0000
        Formatted: Spotify
        */

        String prefixRegex = "^(\\*\\d{4}\\s)?\\d{2}\\.\\d{2}\\s(\\w{3}\\s\\d+\\.\\d{2}\\s)?";
        String suffixRegex = "\\sKurs:\\s\\d+.\\d{4}$";

        String prefixModifiedDescription = rawDescription.replaceAll(prefixRegex, "");

        if (!Objects.equal(prefixModifiedDescription, rawDescription)) {
            return prefixModifiedDescription.replaceAll(suffixRegex, "");
        }

        return rawDescription;
    }
}
