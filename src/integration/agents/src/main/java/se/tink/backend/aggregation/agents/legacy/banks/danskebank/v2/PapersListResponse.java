package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AbstractResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PapersListResponse extends AbstractResponse {
    @JsonProperty("Papers")
    private List<PaperEntity> papers;

    @JsonProperty("PortfolioChangeValue")
    private Double portfolioChangeValue;

    @JsonProperty("PortfolioName")
    private String portfolioName;

    @JsonProperty("PortfolioTotalValue")
    private Double portfolioTotalValue;

    @JsonProperty("Sorted")
    private Boolean sorted;

    public List<PaperEntity> getPapers() {
        return papers;
    }

    public void setPapers(List<PaperEntity> papers) {
        this.papers = papers;
    }

    public Double getPortfolioChangeValue() {
        return portfolioChangeValue;
    }

    public void setPortfolioChangeValue(Double portfolioChangeValue) {
        this.portfolioChangeValue = portfolioChangeValue;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public Double getPortfolioTotalValue() {
        return portfolioTotalValue;
    }

    public void setPortfolioTotalValue(Double portfolioTotalValue) {
        this.portfolioTotalValue = portfolioTotalValue;
    }

    public Boolean getSorted() {
        return sorted;
    }

    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }
}
