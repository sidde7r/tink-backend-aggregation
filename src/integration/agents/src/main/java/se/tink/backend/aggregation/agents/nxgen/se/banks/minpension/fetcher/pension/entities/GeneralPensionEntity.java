package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class GeneralPensionEntity {
    @JsonProperty("produkttyp")
    private String productType;

    private SummaryEntity summary;

    @JsonProperty("apUnderlag")
    private GeneralPensionBasisEntity generalPensionUnderlag;

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount(String ssn) {
        List<PortfolioModule> portfolios = generalPensionUnderlag.getPremiumPensionPortfolios(ssn);
        final PortfolioModule incomePension = generalPensionUnderlag.getIncomePensionPortfolio(ssn);
        portfolios.add(incomePension);

        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolios)
                .withCashBalance(ExactCurrencyAmount.zero(MinPensionConstants.CURRENCY_SEK))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(AccountTypes.GENERAL_PENSION + ssn)
                                .withAccountNumber(AccountTypes.GENERAL_PENSION)
                                .withAccountName(AccountTypes.GENERAL_PENSION)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK,
                                                AccountTypes.GENERAL_PENSION))
                                .build())
                .build();
    }
}
