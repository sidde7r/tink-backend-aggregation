package se.tink.backend.insights.app.generators;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.insights.accounts.AccountQueryService;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateAccountBalanceLowInsightCommand;
import se.tink.backend.insights.core.valueobjects.Account;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.utils.LogUtils;

public class AccountBalanceLowGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(AccountBalanceLowGenerator.class);
    private CommandGateway gateway;
    private AccountQueryService accountQueryService;

    // TODO:: change the number
    private final static double ACCOUNT_BALANCE_LOW_THRESHOLD = 27000;

    @Inject
    public AccountBalanceLowGenerator(CommandGateway gateway,
            AccountQueryService accountQueryService) {
        this.gateway = gateway;
        this.accountQueryService = accountQueryService;
    }

    @Override
    public void generateIfShould(UserId userId) {

        List<Account> eligibleAccounts = accountQueryService.getCheckingAccounts(userId)
                .stream()
                .filter(a -> !a.getBalance().greaterThan(ACCOUNT_BALANCE_LOW_THRESHOLD))
                .collect(Collectors.toList());

        if (eligibleAccounts.size() < 1) {
            log.info(userId, "No insight generated. Reason: No eligible accounts found");
            return;
        }

        CreateAccountBalanceLowInsightCommand command = new CreateAccountBalanceLowInsightCommand(userId,
                eligibleAccounts);
        gateway.on(command);

    }
}
