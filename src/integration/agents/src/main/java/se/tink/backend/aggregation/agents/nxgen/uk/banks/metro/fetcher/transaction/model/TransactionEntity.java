package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity {
    private BigDecimal amount;

    private LocalDate date;

    private List<DetailsEntity> details;

    @JsonProperty("line1")
    private String firstDescription;

    @JsonProperty("line2")
    private String secondDescription;

    private String transactionId;

    public String getDescription() {
        return Optional.ofNullable(secondDescription)
                .map(s -> firstDescription + " - " + s)
                .orElse(firstDescription);
    }

    public List<DetailsEntity> getRawDetails(){
        return details;
    }
}
