package se.tink.backend.insights.app.generators;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateAllBanksConnectedCommand;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.utils.LogUtils;

public class AllBanksConnectedGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(AllBanksConnectedGenerator.class);

    private CommandGateway gateway;
    private CredentialsRepository credentialsRepository;

    @Inject
    public AllBanksConnectedGenerator(CommandGateway gateway, CredentialsRepository credentialsRepository) {
        this.gateway = gateway;
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    public void generateIfShould(UserId userId) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(userId.value());

        // TODO: do we need any filtering on credential status maybe
        if (credentials.size() != 1) { // maybe ok to change to > 1
            log.info(userId, "No insight generated. Reason: User has either zero or more than one credential");
            return;
        }

        CreateAllBanksConnectedCommand command = new CreateAllBanksConnectedCommand(userId);
        gateway.on(command);
    }
}
