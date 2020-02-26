package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class EngagementsEntity {
    private String name;
    private String productName;
    private String id;
    private String type;
    private List<RiskCoversEntity> riskCovers;
    private String status;
    private double investmentValue;
    // `agreedPremium` is null - cannot define it!
    // `premiumModelTerm` is null - cannot define it!
    private String owner;
    private String ownerId;
    private String productType;
    private String productCategory;
    private boolean hasDetail;

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount() {
//        return InvestmentAccount.nxBuilder().`withoutPortfolios().withCashBalance().withId().
//        InvestmentAccount.nxBuilder().withPortfolios().
//        PortfolioModule.builder().withType(PortfolioType.PENSION).withUniqueIdentifier("sdfsdf").withCashValue(23423).
        return null;
    }
}
