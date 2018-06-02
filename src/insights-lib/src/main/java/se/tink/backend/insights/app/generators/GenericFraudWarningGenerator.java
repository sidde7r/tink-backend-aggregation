package se.tink.backend.insights.app.generators;

import java.util.List;
import javax.inject.Inject;
import se.tink.backend.core.User;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateGenericFraudWarningInsightCommand;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.identity.IdentityQueryService;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.identity.model.IdentityEvent;

public class GenericFraudWarningGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(GenericFraudWarningGenerator.class);

    private UserQueryService userQueryService;
    private IdentityQueryService identityQueryService;
    private CommandGateway commandGateway;

    @Inject
    public GenericFraudWarningGenerator(
            UserQueryService userQueryService, IdentityQueryService identityQueryService,
            CommandGateway commandGateway) {
        this.userQueryService = userQueryService;
        this.identityQueryService = identityQueryService;
        this.commandGateway = commandGateway;
    }

    @Override
    public void generateIfShould(UserId userId) {
        User user = userQueryService.findById(userId);

        if (user == null) {
            log.warn(userId, "No insight generated. Reason: No user found");
            return;
        }

        // add logic if the insight is already generated and valid

        List<IdentityEvent> fraudIdentityEvents = identityQueryService
                .getFraudIdentityEvents(userId, user.getLocale(), user.getProfile().getCurrency());

        if (fraudIdentityEvents.size() <= 0) {
            log.info(userId, "No insight generated. Reason: No fraud identity events found");
            return;
        }

        CreateGenericFraudWarningInsightCommand command = new CreateGenericFraudWarningInsightCommand(userId, fraudIdentityEvents);
        commandGateway.on(command);
    }
}
