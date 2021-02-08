package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("Amount")
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("Date")
    private Date date;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeName")
    private String typeName = "";

    @JsonProperty("DisplayTypeName")
    private String displayTypeName = "";

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("PurchaseDate")
    private Date purchaseDate;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setDate(Date date) {
        this.date = date;
    }

    @JsonIgnore
    private String getNote() {
        // No case where the field "note" is missing has been found.
        return Optional.ofNullable(note).orElse(displayTypeName);
    }

    @JsonIgnore
    private Date getDate() {
        return Optional.ofNullable(purchaseDate).orElse(date);
    }

    @JsonIgnore
    public Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                amount, SkandiaBankenConstants.Currency.SEK.toString()))
                .setDate(getDate())
                .setDescription(getNote())
                .setPending(isPending)
                .build();
    }
}
