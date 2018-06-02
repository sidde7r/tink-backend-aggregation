package se.tink.backend.insights.app;

import se.tink.backend.insights.app.commands.ArchiveInsightCommand;
import se.tink.backend.insights.app.commands.CreateAccountBalanceLowInsightCommand;
import se.tink.backend.insights.app.commands.CreateAllBanksConnectedCommand;
import se.tink.backend.insights.app.commands.CreateBudgetCloseInsightCommand;
import se.tink.backend.insights.app.commands.CreateBudgetOverspendInsightCommand;
import se.tink.backend.insights.app.commands.CreateEinvoiceOverdueInsightCommand;
import se.tink.backend.insights.app.commands.CreateEinvoicesInsightCommand;
import se.tink.backend.insights.app.commands.CreateGenericFraudWarningInsightCommand;
import se.tink.backend.insights.app.commands.CreateHigherIncomeThanCertainPercentileCommand;
import se.tink.backend.insights.app.commands.CreateIncreaseCategorizationLevelInsightCommand;
import se.tink.backend.insights.app.commands.CreateLeftToSpendHighInsightCommand;
import se.tink.backend.insights.app.commands.CreateLeftToSpendLowInsightCommand;
import se.tink.backend.insights.app.commands.CreateMonthlySummaryInsightCommand;
import se.tink.backend.insights.app.commands.CreateRateAppInsightCommand;
import se.tink.backend.insights.app.commands.CreateResidenceDoYouOwnItCommand;
import se.tink.backend.insights.app.commands.CreateWeeklySummaryInsightCommand;
import se.tink.backend.insights.app.commands.RemovePreviousInsightsCommand;
import se.tink.backend.insights.app.commands.SetInsightChoiceCommand;

public interface CommandGateway {
    void on(CreateMonthlySummaryInsightCommand command);

    void on(CreateRateAppInsightCommand command);

    void on(CreateEinvoicesInsightCommand command);

    void on(CreateEinvoiceOverdueInsightCommand command);

    void on(CreateLeftToSpendHighInsightCommand command);

    void on(CreateAccountBalanceLowInsightCommand command);

    void on(CreateLeftToSpendLowInsightCommand command);

    void on(CreateHigherIncomeThanCertainPercentileCommand command);

    void on(CreateWeeklySummaryInsightCommand command);

    void on(CreateGenericFraudWarningInsightCommand command);

    void on(ArchiveInsightCommand command);

    void on(SetInsightChoiceCommand command);

    void on(CreateIncreaseCategorizationLevelInsightCommand command);

    void on(CreateResidenceDoYouOwnItCommand command);

    void on(CreateBudgetCloseInsightCommand command);

    void on(RemovePreviousInsightsCommand command);

    void on(CreateBudgetOverspendInsightCommand command);

    void on(CreateAllBanksConnectedCommand command);
}
