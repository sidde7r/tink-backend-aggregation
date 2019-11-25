package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetInvestmentsBodyEntity {
    @JsonProperty("Carteiras")
    private List<FundsPortfolioEntity> fundsPortfolio;

    @JsonProperty("Dossiers")
    private List<DossiersEntity> dossiers;

    @JsonProperty("DossierSelecionado")
    private String selectedDossier;

    @JsonProperty("ValorTotalDossier")
    private BigDecimal dossierTotalValue;

    @JsonProperty("Moeda")
    private String currency;

    public List<FundsPortfolioEntity> getFundsPortfolio() {
        return fundsPortfolio;
    }

    public List<DossiersEntity> getDossiers() {
        return dossiers;
    }

    public String getSelectedDossier() {
        return selectedDossier;
    }

    public BigDecimal getDossierTotalValue() {
        return dossierTotalValue;
    }

    public String getCurrency() {
        return currency;
    }
}
