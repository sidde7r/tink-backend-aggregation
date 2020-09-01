package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class InterestEntity {

    private double rate;
    private String interestChangeDate;

    public LocalDate getInterestChangeDateAsLocalDate() {
        return Optional.ofNullable(interestChangeDate)
                .map(date -> LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .orElse(null);
    }
}
