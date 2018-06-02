package se.tink.backend.insights.app.generators;

import java.util.Date;
import javax.inject.Inject;
import se.tink.backend.common.config.RateThisAppConfiguration;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateRateAppInsightCommand;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class RateAppGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(RateAppGenerator.class);

    private CommandGateway gateway;
    private UserQueryService userQueryService;
    private final RateThisAppConfiguration rateThisAppConfiguration;

    @Inject
    public RateAppGenerator(CommandGateway gateway, UserQueryService userQueryService,
            RateThisAppConfiguration rateThisAppConfiguration) {
        this.gateway = gateway;
        this.userQueryService = userQueryService;
        this.rateThisAppConfiguration = rateThisAppConfiguration;
    }

    @Override
    public void generateIfShould(UserId userId) {

        UserState userState = userQueryService.getUserState(userId);
        if (!isEnabled() || !isActiveUser(userState, userQueryService.findById(userId)) ||
                hasInteractedWithActivity(userState)) {
            log.info(userId,
                    "No insight generated. Reasons: rate this app not enabled, "
                            + "is not active user or has interacted with this activity");
            return;
        }

        CreateRateAppInsightCommand command = new CreateRateAppInsightCommand(userId);
        gateway.on(command);
    }

    public boolean isEnabled() {
        return rateThisAppConfiguration.isEnabled();
    }

    public boolean hasInteractedWithActivity(UserState userState) {
        switch (userState.getRateThisAppStatus()) {
        case USER_CLICKED_IGNORE:
        case USER_CLICKED_RATE_IN_STORE:
            return true;
        default:
            return false;
        }
    }

    public boolean isActiveUser(UserState userState,
            User user) { // TODO: instead of user object, have a insights User Object

        return userState.getAmountCategorizationLevel() >= rateThisAppConfiguration.getMinCategorizationLevel() &&
                userState.getInitialAmountCategorizationLevel() <= rateThisAppConfiguration
                        .getMaxInitialCategorization() &&
                user.getCreated()
                        .before(DateUtils.addDays(new Date(), -rateThisAppConfiguration.getMinDaysSinceCreated())) &&
                user.getCreated()
                        .after(DateUtils.addDays(new Date(), -rateThisAppConfiguration.getMaxDaysSinceCreated()));
    }
}
