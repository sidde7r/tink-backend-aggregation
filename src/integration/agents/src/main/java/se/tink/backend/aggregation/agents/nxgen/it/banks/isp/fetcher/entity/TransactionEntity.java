package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    @JsonProperty("divisa")
    private String currency;

    @JsonProperty("importo")
    private BigDecimal amount;

    @JsonProperty("descrizioneBreve")
    private String shortDescription;

    @JsonProperty("descrizioneEstesa")
    private String longDescription;

    @JsonProperty("dataContabilizzazione")
    private Long bookedDateEpochMili;

    @JsonProperty("dataValuta")
    private Long valueDateEpochMili;

    @JsonProperty("contabilizzato")
    private boolean booked;

    @JsonProperty("detectedCategories")
    private List<CategoryEntity> categories;

    public Transaction toTinkTransaction() {
        LocalDate transactionDate =
                bookedDateEpochMili != null
                        ? toLocalDate(bookedDateEpochMili)
                        : toLocalDate(valueDateEpochMili);
        Transaction.Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDescription(shortDescription)
                        .setDate(transactionDate)
                        .setPending(!booked)
                        .setPayload(TransactionPayloadTypes.DETAILS, longDescription);

        if (categories != null) {
            builder =
                    builder.setPayload(
                            TransactionPayloadTypes.BANK_PROVIDED_CATEGORIZATION,
                            categories.toString());
        }

        return builder.build();
    }

    private LocalDate toLocalDate(Long bookedDate) {
        return Instant.ofEpochMilli(bookedDate).atZone(ZoneId.of("UTC")).toLocalDate();
    }
}
