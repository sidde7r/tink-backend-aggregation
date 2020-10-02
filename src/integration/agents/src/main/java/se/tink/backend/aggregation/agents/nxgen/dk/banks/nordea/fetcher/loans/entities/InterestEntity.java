package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InterestEntity {

    private Double rate;
    private String interestChangeDate;

    public LocalDate getInterestChangeDateAsLocalDate() {
        return Optional.ofNullable(interestChangeDate)
                .map(date -> LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .orElse(null);
    }
}
