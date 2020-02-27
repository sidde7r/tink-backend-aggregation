package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import java.util.stream.Collectors;
import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Portfolio.Type;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LifeInsurancesEntity {
    private String startDate;
    private String lifeInsuranceType;
    private String concept;
    private String insuranceNumber;
    private String referenceNumber;
    private ProductEntity product;
    private String status;
    private String beneficiaryCode;
    private double totalPremiumPaid;
    private double investmentValue;
    private String termsDate;
    private String taxClass;
    private List<TermsAndConditionsEntity> termsAndConditions;
    private String agreementNumber;
    private SavingEntity saving;
    private DeathRiskCoverEntity deathRiskCover;
    private PremiumEntity premium;
    private IdentityEntity insured;
    private IdentityEntity owner;
    private List<FundSelectionsEntity> fundSelections;
    private LifeInsuranceStatusEntity lifeInsuranceStatus;
    private AssetSummarysEntity assetSummarys;

    @JsonIgnore
    public Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(investmentValue);
        portfolio.setUniqueIdentifier(referenceNumber.trim());
        portfolio.setType(Type.PENSION);
        portfolio.setRawType(lifeInsuranceType);
        portfolio.setInstruments(
                fundSelections.stream()
                        .map(FundSelectionsEntity::toTinkInstrument)
                        .collect((Collectors.toList())));

        return portfolio;
    }
}
