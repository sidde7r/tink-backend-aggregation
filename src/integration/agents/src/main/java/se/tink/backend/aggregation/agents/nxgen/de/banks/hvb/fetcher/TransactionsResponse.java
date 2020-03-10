package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Data
@Accessors(chain = true)
@JsonObject
public class TransactionsResponse {

    private Response response;

    @Data
    @Accessors(chain = true)
    @JsonObject
    static class Response {

        @JsonProperty("transactionList")
        private List<ItemContainer> itemContainers;

        @Data
        @Accessors(chain = true)
        @JsonObject
        static class ItemContainer {

            @JsonProperty("itms")
            private List<Item> items;

            @Data
            @Accessors(chain = true)
            @JsonObject
            static class Item {

                @JsonDeserialize(using = LocalDateDeserializer.class)
                private LocalDate bookDate;

                @JsonProperty("accordionData")
                private List<String> descriptionLines;

                @JsonProperty("postingEntryAmount")
                private AmountEntry amountEntry;

                @Data
                @Accessors(chain = true)
                @JsonObject
                static class AmountEntry {

                    private BigDecimal amount;
                    private String currency;

                    ExactCurrencyAmount toExactCurrencyAmount() {
                        return ExactCurrencyAmount.of(amount, currency);
                    }
                }

                ExactCurrencyAmount getExactCurrencyAmount() {
                    return Optional.ofNullable(getAmountEntry())
                            .map(AmountEntry::toExactCurrencyAmount)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Can't obtain exact amount."));
                }

                boolean isPending() {
                    return getBookDate() == null;
                }

                String getDescription() {
                    return Optional.ofNullable(descriptionLines)
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .collect(Collectors.joining(" "));
                }
            }
        }
    }
}
