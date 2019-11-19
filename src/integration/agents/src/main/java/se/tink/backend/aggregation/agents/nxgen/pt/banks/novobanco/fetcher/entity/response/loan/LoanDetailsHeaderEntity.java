package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.CURRENT_BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.INTEREST_RATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

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
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(line -> INTEREST_RATE.equals(line.getL()))
                .map(LoanLinesEntity::getV)
                .findFirst()
                .orElse(null);
    }

    public String getCurrentBalance() {
        return Optional.ofNullable(lines)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(line -> CURRENT_BALANCE.equals(line.getL()))
                .map(LoanLinesEntity::getV)
                .findFirst()
                .orElse(null);
    }
}
