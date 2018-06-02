package se.tink.backend.insights.app;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import se.tink.backend.insights.app.generators.AccountBalanceLowGenerator;
import se.tink.backend.insights.app.generators.AllBanksConnectedGenerator;
import se.tink.backend.insights.app.generators.BudgetCloseGenerator;
import se.tink.backend.insights.app.generators.BudgetOverspendGenerator;
import se.tink.backend.insights.app.generators.EinvoiceGenerator;
import se.tink.backend.insights.app.generators.EinvoiceOverdueGenerator;
import se.tink.backend.insights.app.generators.GenericFraudWarningGenerator;
import se.tink.backend.insights.app.generators.HigherIncomeThanCertainPercentileGenerator;
import se.tink.backend.insights.app.generators.IncreaseCategorizationLevelGenerator;
import se.tink.backend.insights.app.generators.InsightGenerator;
import se.tink.backend.insights.app.generators.LeftToSpendHighGenerator;
import se.tink.backend.insights.app.generators.LeftToSpendLowGenerator;
import se.tink.backend.insights.app.generators.MonthlySummaryGenerator;
import se.tink.backend.insights.app.generators.RateAppGenerator;
import se.tink.backend.insights.app.generators.ResidenceDoYouOwnItGenerator;
import se.tink.backend.insights.app.generators.WeeklySummaryGenerator;
import se.tink.backend.insights.core.valueobjects.UserId;

public class GeneratorsProviderImpl implements GeneratorsProvider {
    private final ImmutableList<InsightGenerator> generators;

    @Inject
    public GeneratorsProviderImpl(
            AccountBalanceLowGenerator accountBalanceLowGenerator,
            AllBanksConnectedGenerator allBanksConnectedGenerator,
            BudgetCloseGenerator budgetCloseGenerator,
            BudgetOverspendGenerator budgetOverspendGenerator,
            EinvoiceGenerator einvoiceGenerator,
            EinvoiceOverdueGenerator einvoiceOverdueGenerator,
            GenericFraudWarningGenerator genericFraudWarningGenerator,
            HigherIncomeThanCertainPercentileGenerator higherIncomeThanCertainPercentileGenerator,
            IncreaseCategorizationLevelGenerator increaseCategorizationLevelGenerator,
            LeftToSpendHighGenerator leftToSpendHighGenerator,
            LeftToSpendLowGenerator leftToSpendLowGenerator,
            MonthlySummaryGenerator monthlySummaryGenerator,
            RateAppGenerator rateAppGenerator,
            ResidenceDoYouOwnItGenerator residenceDoYouOwnItGenerator,
            WeeklySummaryGenerator weeklySummaryGenerator) {
        this.generators = ImmutableList.of(
                accountBalanceLowGenerator,
                allBanksConnectedGenerator,
                budgetCloseGenerator,
                budgetOverspendGenerator,
                einvoiceGenerator,
                einvoiceOverdueGenerator,
                genericFraudWarningGenerator,
                higherIncomeThanCertainPercentileGenerator,
                increaseCategorizationLevelGenerator,
                leftToSpendHighGenerator,
                leftToSpendLowGenerator,
                monthlySummaryGenerator,
                rateAppGenerator,
                residenceDoYouOwnItGenerator,
                weeklySummaryGenerator);
    }

    @Override
    public ImmutableList<InsightGenerator> getGenerators(UserId userId) {
        return generators;
    }
}
