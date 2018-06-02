package se.tink.backend.insights.app;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
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
import se.tink.backend.insights.app.repositories.ArchivedInsightRepository;
import se.tink.backend.insights.app.repositories.InsightRepository;
import se.tink.backend.insights.core.domain.model.AccountBalanceLowInsight;
import se.tink.backend.insights.core.domain.model.AllBanksConnectedInsight;
import se.tink.backend.insights.core.domain.model.ArchivedInsight;
import se.tink.backend.insights.core.domain.model.BudgetCloseInsight;
import se.tink.backend.insights.core.domain.model.BudgetOverspendInsight;
import se.tink.backend.insights.core.domain.model.EinvoiceInsight;
import se.tink.backend.insights.core.domain.model.EinvoiceOverdueInsight;
import se.tink.backend.insights.core.domain.model.GenericInsight;
import se.tink.backend.insights.core.domain.model.HigherIncomeThanCertainPercentileInsight;
import se.tink.backend.insights.core.domain.model.IncreaseCategorizationLevelInsight;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.domain.model.LeftToSpendHighInsight;
import se.tink.backend.insights.core.domain.model.LeftToSpendLowInsight;
import se.tink.backend.insights.core.domain.model.MonthlySummaryInsight;
import se.tink.backend.insights.core.domain.model.RateThisAppInsight;
import se.tink.backend.insights.core.domain.model.ResidenceDoYouOwnItInsight;
import se.tink.backend.insights.core.domain.model.WeeklySummaryInsight;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.utils.LogUtils;

public class CommandHandler implements CommandGateway {
    private static final LogUtils log = new LogUtils(CommandHandler.class);
    private InsightRepository insightRepository;
    private ArchivedInsightRepository archivedRepository;

    @Inject
    public CommandHandler(InsightRepository insightRepository, ArchivedInsightRepository archivedRepository) {
        this.insightRepository = insightRepository;
        this.archivedRepository = archivedRepository;
    }

    @Override
    public void on(CreateMonthlySummaryInsightCommand command) {

        MonthlySummaryInsight insight = new MonthlySummaryInsight(
                command.getUserId(),
                command.getPeriod(),
                command.getTotalExpenses(),
                command.getTotalExpenseAverage(),
                command.getLargestExpense(),
                command.getLargestCategories()
        );

        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateRateAppInsightCommand command) {
        RateThisAppInsight insight = new RateThisAppInsight(command.getUserId());
        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateLeftToSpendHighInsightCommand command) {
        LeftToSpendHighInsight insight = new LeftToSpendHighInsight(command.getUserId(), command.getAmount());
        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateEinvoicesInsightCommand command) {
        List<Insight> insights = command.getEInvoices().stream()
                .map(e -> new EinvoiceInsight(command.getUserId(), e.geteInvoiceId(), e.getAmount(), e.getDueDate(),
                        e.getSourceMessage())).collect(Collectors.toList());
        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateEinvoiceOverdueInsightCommand command) {
        List<Insight> insights = command.getOverdueEinvoices().stream()
                .map(e -> new EinvoiceOverdueInsight(command.getUserId(), e.geteInvoiceId(), e.getAmount(),
                        e.getDueDate(),
                        e.getSourceMessage())).collect(Collectors.toList());
        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateAccountBalanceLowInsightCommand command) {
        List<Insight> insights = command.getAccounts().stream()
                .map(a -> new AccountBalanceLowInsight(command.getUserId(), a.getName(), a.getAccountId(),
                        a.getBalance())).collect(Collectors.toList());
        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateLeftToSpendLowInsightCommand command) {
        LeftToSpendLowInsight insight = new LeftToSpendLowInsight(command.getUserId(), command.getAmount());
        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateHigherIncomeThanCertainPercentileCommand command) {
        HigherIncomeThanCertainPercentileInsight insight = new HigherIncomeThanCertainPercentileInsight(
                command.getUserId(), command.getPercentileBetter());
        insightRepository.save(command.getUserId(), insight);
    }

    public void on(CreateWeeklySummaryInsightCommand command) {

        WeeklySummaryInsight insight = new WeeklySummaryInsight(
                command.getUserId(),
                command.getTotalAmount(),
                command.getLargestTransaction(),
                command.getWeeklySummaryInsightCategoryData(),
                command.getTransactionCount(),
                command.getWeek().getWeekOfYear(),
                command.getWeek().getWeekStartDate(),
                command.getWeek().getWeekEndDate());

        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateIncreaseCategorizationLevelInsightCommand command) {
        IncreaseCategorizationLevelInsight insight = new IncreaseCategorizationLevelInsight(
                command.getUserId(),
                command.getNumberOfNonCategorizedTransactions(),
                command.getLevelOfCategorization()
        );
        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateGenericFraudWarningInsightCommand command) {
        List<Insight> insights = command.getIdentityEvents().stream()
                .map(ie -> new GenericInsight(command.getUserId(), ie.getId(), ie.getDescription()))
                .collect(Collectors.toList());
        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateResidenceDoYouOwnItCommand command) {
        ResidenceDoYouOwnItInsight insight = new ResidenceDoYouOwnItInsight(command.getUserId(),
                command.getAddress(), command.getIdentityEventId());

        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(CreateBudgetCloseInsightCommand command) {
        List<Insight> insights = command.getFollowItemTransactions().stream()
                .map(fi -> new BudgetCloseInsight(command.getUserId(), Amount.of(fi.getLeftOfBudget()),
                        Amount.of(fi.getTargetAmount()), fi.getName())).collect(Collectors.toList());

        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateBudgetOverspendInsightCommand command) {
        List<Insight> insights = command.getFollowItemTransactions().stream()
                .map(fi -> new BudgetOverspendInsight(command.getUserId(), fi.getName()))
                .collect(Collectors.toList());

        insightRepository.save(command.getUserId(), insights);
    }

    @Override
    public void on(CreateAllBanksConnectedCommand command) {
        AllBanksConnectedInsight insight = new AllBanksConnectedInsight(command.getUserId());
        insightRepository.save(command.getUserId(), insight);
    }

    @Override
    public void on(RemovePreviousInsightsCommand command) {
        insightRepository.deleteByUserId(command.getUserId());
    }

    @Override
    public void on(SetInsightChoiceCommand command) {
        Insight insight = insightRepository.findByUserIdAndInsightId(command.getUserId(), command.getInsightId());

        if (insight == null) {
            return;
        }

        if (!insight.containsAction(command.getActionId())) {
            throw new IllegalArgumentException();
        }

        insight.selectActionById(command.getActionId());
    }

    @Override
    public void on(ArchiveInsightCommand command) {
        Insight insight = insightRepository.findByUserIdAndInsightId(command.getUserId(), command.getInsightId());
        ArchivedInsight archivedInsight = archivedRepository.save(insight);
        if (archivedInsight != null) {
            insightRepository.deleteByInsightId(command.getUserId(), command.getInsightId());
        }
    }

}
