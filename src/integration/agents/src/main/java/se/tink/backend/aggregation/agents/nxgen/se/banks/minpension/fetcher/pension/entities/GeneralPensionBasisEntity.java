package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;

@Getter
@JsonObject
public class GeneralPensionBasisEntity {
    @JsonProperty("intjanandear")
    private int earningsYear;

    @JsonProperty("inkomstar")
    private int incomeYear;

    @JsonProperty("aktuellUttagsandelAlderspension")
    private Double currentWithdrawalSharesAgePension;

    @JsonProperty("inkomstpensionsbehallningBelopp")
    private Double incomePensionBalanceAmount;

    @JsonProperty("inkomstpensionsbehallningBeslutsdatum")
    private String incomePensionBalanceDecisionDate;

    @JsonProperty("inkomstpensionManatligtIntjanande")
    private Double incomePensionMonthlyEarnings;

    @JsonProperty("premiepensionssaldoBelopp")
    private Double premiumPensionBalanceAmount;

    @JsonProperty("premiepensionssaldoKursdatum")
    private String premiumPensionRateDate;

    @JsonProperty("premiepensionManatligtIntjanande")
    private Double premiumPensionMonthlyEarnings;

    @JsonProperty("premiepensionHasEfterlevandeskydd")
    private boolean premiumPensionHasSurvivorsProtection;

    @JsonProperty("premiepensionForvaltningstyp")
    private String premiumPensionManagementType;

    @JsonProperty("avtaladUttagsalderManader")
    private int agreedWithdrawalAgeMonths;

    @JsonProperty("avtaladUttagstidTyp")
    private String agreedWithdrawalPeriodTime;

    @JsonProperty("avtaladUttagstidManader")
    private int agreedWithdrawalPeriodMonths;

    @JsonProperty("leveransdatum")
    private String deliveryDate;

    @JsonProperty("ppKontoList")
    private List<PremiumPensionAccountEntity> premiumPensionKontoList;

    List<PortfolioModule> getPremiumPensionPortfolios() {
        return ListUtils.emptyIfNull(premiumPensionKontoList).stream()
                .map(PremiumPensionAccountEntity::getPremiumPension)
                .collect(Collectors.toList());
    }

    PortfolioModule getIncomePensionPortfolio() {
        return PortfolioModule.builder()
                .withType(PortfolioType.PENSION)
                .withUniqueIdentifier(AccountTypes.INCOME_PENSION)
                .withCashValue(0.00)
                .withTotalProfit(incomePensionBalanceAmount)
                .withTotalValue(incomePensionBalanceAmount)
                .withoutInstruments()
                .build();
    }
}
