package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class TransactionEntity {
    private String id;
    private String amountInteger;
    private String amountFraction;
    private String type;
    private String description;
    private String date;
    private String interestDate;
    private Boolean incoming;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Sparebank1AmountUtils.constructAmount(amountInteger, amountFraction))
                .setDate(DateUtils.parseDate(date))
                .setDescription(getTinkFormattedDescription(description))
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmountInteger() {
        return amountInteger;
    }

    public void setAmountInteger(String amountInteger) {
        this.amountInteger = amountInteger;
    }

    public String getAmountFraction() {
        return amountFraction;
    }

    public void setAmountFraction(String amountFraction) {
        this.amountFraction = amountFraction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInterestDate() {
        return interestDate;
    }

    public void setInterestDate(String interestDate) {
        this.interestDate = interestDate;
    }

    public Boolean getIncoming() {
        return incoming;
    }

    public void setIncoming(Boolean incoming) {
        this.incoming = incoming;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }
}
