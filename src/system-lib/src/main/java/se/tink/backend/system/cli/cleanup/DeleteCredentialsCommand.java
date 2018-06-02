package se.tink.backend.system.cli.cleanup;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class DeleteCredentialsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private final static LogUtils log = new LogUtils(DeleteCredentialsCommand.class);

    public DeleteCredentialsCommand() {
        super("delete-credentials", "Delete credentials and their transactions.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        DeleteController deleteController = new DeleteController(serviceContext);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        List<String> credentialsIds = Lists.newArrayList();

        String fileName = System.getProperty("file");

        if (!Strings.isNullOrEmpty(fileName)) {

            credentialsIds = Files.readLines(new File(fileName), Charsets.UTF_8,
                    new LineProcessor<List<String>>() {

                        private List<String> credentialsIds = Lists.newArrayList();

                        @Override
                        public boolean processLine(String line) throws IOException {
                            if (!Strings.isNullOrEmpty(line)) {
                                credentialsIds.add(line);
                            }
                            return true;
                        }

                        @Override
                        public List<String> getResult() {
                            return credentialsIds;
                        }
                    });
        } else {
            String credentialsId = System.getProperty("credentialsId");

            if (Strings.isNullOrEmpty(credentialsId)) {
                log.error("credentialsId is missing. Terminating.");
                return;
            }

            credentialsIds.add(credentialsId);
        }

        for (String credentialsId : credentialsIds) {

            Credentials credentials = credentialsRepository.findOne(credentialsId);

            if (credentials == null) {
                log.error(credentialsId, "Credentials does not exist.");
                continue;
            }

            User user = userRepository.findOne(credentials.getUserId());

            if (user == null) {
                log.error(credentials, "User does not exist.");
                continue;
            }

            log.info("Deleting credentials " + credentialsId);

            deleteController.deleteCredentials(user, credentialsId, false, Optional.empty());
        }

        log.info("Done deleting " + credentialsIds.size() + " credentials.");
    }

}
