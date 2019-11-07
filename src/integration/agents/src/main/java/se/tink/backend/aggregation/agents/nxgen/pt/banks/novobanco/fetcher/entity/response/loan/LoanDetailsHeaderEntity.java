package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@JsonObject
public class LoanDetailsHeaderEntity {
    @JsonProperty("Titulo")
    private String title;

    @JsonProperty("SubTitulo")
    private LoanSubTitleEntity subTitle;

    @JsonProperty("Linhas")
    private List<LoanLinesEntity> lines;

    public String getTitle() {
        return title;
    }

    public String getInterestRate() {
        return Optional.ofNullable(lines)
                .map(Collection::stream).orElse(Stream.empty())
                .filter(line -> "TAN".equals(line.getL()))
                .map(LoanLinesEntity::getV).findFirst().orElse(null);
    }

    public String getCurrentBalance() {
        return Optional.ofNullable(lines)
                .map(Collection::stream).orElse(Stream.empty())
                .filter(line -> "Capital em divida".equals(line.getL()))
                .map(LoanLinesEntity::getV).findFirst().orElse(null);
    }
}