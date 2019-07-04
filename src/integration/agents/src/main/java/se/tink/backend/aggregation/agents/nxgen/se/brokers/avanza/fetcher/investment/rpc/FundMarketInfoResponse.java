package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.FundCompanyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.RelatedFundsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundMarketInfoResponse extends MarketInfoResponse {
    private String description;
    private String domicile;
    private Integer risk;
    private Double sharpeRatio;
    private String startDate;
    private Double standardDeviation;
    private String riskLevel;
    private Integer rating;
    private Double capital;
    private Double managementFee;
    private Double buyFee;
    private Double sellFee;
    private Double normanAmount;
    private String administrators;
    private String prospectus;
    private String tradingCurrency;
    private Double loanFactor;
    private Boolean buyable;
    private Boolean sellable;
    private String preliminaryFeesType;

    @JsonProperty("NAV")
    private Double nav;

    @JsonProperty("NAVLastUpdated")
    private String navLastUpdated;

    private Double changeSinceOneDay;
    private Double changeSinceOneWeek;
    private Double changeSinceOneMonth;
    private Double changeSinceThreeMonths;
    private Double changeSinceSixMonths;
    private Double changeSinceTurnOfTheYear;
    private Double changeSinceOneYear;
    private Double changeSinceThreeYears;
    private Double changeSinceFiveYears;
    private Double changeSinceTenYears;
    private List<RelatedFundsEntity> relatedFunds;
    private FundCompanyEntity fundCompany;
    private Integer numberOfOwners;
    private List<PositionEntity> positions;
    private Double positionsTotalValue;
    private Integer numberOfPriceAlerts;
    private Boolean autoPortfolio;
    private String type;
    private String subCategory;
    private String otherFees;

    public String getDescription() {
        return description;
    }

    public String getDomicile() {
        return domicile;
    }

    public Integer getRisk() {
        return risk;
    }

    public Double getSharpeRatio() {
        return sharpeRatio;
    }

    public String getStartDate() {
        return startDate;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public Integer getRating() {
        return rating;
    }

    public Double getCapital() {
        return capital;
    }

    public Double getManagementFee() {
        return managementFee;
    }

    public Double getBuyFee() {
        return buyFee;
    }

    public Double getSellFee() {
        return sellFee;
    }

    public Double getNormanAmount() {
        return normanAmount;
    }

    public String getAdministrators() {
        return administrators;
    }

    public String getProspectus() {
        return prospectus;
    }

    public String getTradingCurrency() {
        return tradingCurrency;
    }

    public Double getLoanFactor() {
        return loanFactor;
    }

    public Boolean getBuyable() {
        return buyable;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public String getPreliminaryFeesType() {
        return preliminaryFeesType;
    }

    public Double getNav() {
        return nav;
    }

    public String getNavLastUpdated() {
        return navLastUpdated;
    }

    public Double getChangeSinceOneDay() {
        return changeSinceOneDay;
    }

    public Double getChangeSinceOneWeek() {
        return changeSinceOneWeek;
    }

    public Double getChangeSinceOneMonth() {
        return changeSinceOneMonth;
    }

    public Double getChangeSinceThreeMonths() {
        return changeSinceThreeMonths;
    }

    public Double getChangeSinceSixMonths() {
        return changeSinceSixMonths;
    }

    public Double getChangeSinceTurnOfTheYear() {
        return changeSinceTurnOfTheYear;
    }

    public Double getChangeSinceOneYear() {
        return changeSinceOneYear;
    }

    public Double getChangeSinceThreeYears() {
        return changeSinceThreeYears;
    }

    public Double getChangeSinceFiveYears() {
        return changeSinceFiveYears;
    }

    public Double getChangeSinceTenYears() {
        return changeSinceTenYears;
    }

    public List<RelatedFundsEntity> getRelatedFunds() {
        return Optional.ofNullable(relatedFunds).orElseGet(Collections::emptyList);
    }

    public FundCompanyEntity getFundCompany() {
        return fundCompany;
    }

    public Integer getNumberOfOwners() {
        return numberOfOwners;
    }

    public List<PositionEntity> getPositions() {
        return Optional.ofNullable(positions).orElseGet(Collections::emptyList);
    }

    public Double getPositionsTotalValue() {
        return positionsTotalValue;
    }

    public Integer getNumberOfPriceAlerts() {
        return numberOfPriceAlerts;
    }

    public Boolean getAutoPortfolio() {
        return autoPortfolio;
    }

    public String getType() {
        return type;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getOtherFees() {
        return otherFees;
    }

    @Override
    public String getMarketPlace() {
        if (fundCompany != null) {
            return fundCompany.getName();
        }
        return super.getMarketPlace();
    }
}
