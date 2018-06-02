package se.tink.backend.system.cli.applications;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationProcessor;
import se.tink.backend.common.application.ApplicationProcessorFactory;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationArchiveRow;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.document.DocumentCommandHandler;
import se.tink.backend.system.document.command.EmailDocumentsCommand;
import se.tink.backend.system.document.mapper.BackOfficeConfigurationToDocumentModeratorDetailsMapper;
import se.tink.backend.system.document.mapper.GenericApplicationToDocumentUserMapper;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class GenerateApplicationDocumentsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(GenerateApplicationDocumentsCommand.class);

    private ApplicationProcessorFactory applicationProcessorFactory;
    private BackOfficeConfiguration backOfficeConfiguration;
    private MailSender mailSender;
    private DocumentRepository documentRepository;

    public GenerateApplicationDocumentsCommand() {
        super("generate-application-documents", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.applicationProcessorFactory = new ApplicationProcessorFactory(serviceContext,
                new ProviderImageProvider(serviceContext.getRepository(ProviderImageRepository.class)));
        this.backOfficeConfiguration = configuration.getBackOffice();
        this.mailSender = serviceContext.getMailSender();
        this.documentRepository = serviceContext.getRepository(DocumentRepository.class);

        log.info(String.format("Back-office configuration: %s",
                SerializationUtils.serializeToString(backOfficeConfiguration)));

        if (!backOfficeConfiguration.isEnabled()) {
            log.debug("Aborting since back office is disabled. Change the configuration to enable it.");
            return;
        }

        String userId = Optional.ofNullable(System.getProperty("userId")).map(s -> s.replaceAll("-", "")).orElse(null);
        String applicationId = Optional.ofNullable(System.getProperty("applicationId")).map(s -> s.replaceAll("-", ""))
                .orElse(null);

        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(applicationId)) {
            log.error("You have to supply `userId` and `applicationId`.");
            return;
        }

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        User user = userRepository.findOne(userId);
        validate(user);

        ApplicationDAO applicationDAO = serviceContext.getDao(ApplicationDAO.class);
        Application application = applicationDAO
                .findByUserIdAndId(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(applicationId));
        validate(application);

        ApplicationArchiveRepository applicationArchiveRepository = serviceContext
                .getRepository(ApplicationArchiveRepository.class);
        Optional<ApplicationArchiveRow> applicationArchiveRow = applicationArchiveRepository
                .findByUserIdAndApplicationId(application.getUserId(), application.getId());
        validate(applicationArchiveRow);

        generateDocuments(user, application, applicationArchiveRow.get().getTimestamp());
    }

    private static void validate(User user) {
        Preconditions.checkNotNull(user);
    }

    private static void validate(Application application) {
        Preconditions.checkNotNull(application);
    }

    private static void validate(Optional<ApplicationArchiveRow> applicationArchiveRow) {
        if (!applicationArchiveRow.isPresent()) {
            throw new RuntimeException("Archived application is not available.");
        }

        if (!Objects.equals(applicationArchiveRow.get().getStatus(), ApplicationArchiveRow.Status.SIGNED)) {
            throw new RuntimeException("The application has not been signed yet.");
        }
    }

    private void generateDocuments(User user, Application application, Date signingDate) {

        ApplicationProcessor processor = applicationProcessorFactory.create(application, user, null);
        GenericApplication genericApplication = processor.getGenericApplication(application);

        EmailDocumentsCommand cmd = new EmailDocumentsCommand(
                GenericApplicationToDocumentUserMapper.translate(genericApplication, signingDate),
                BackOfficeConfigurationToDocumentModeratorDetailsMapper.translate(backOfficeConfiguration), user.getId());

        DocumentCommandHandler commandHandler = new DocumentCommandHandler(mailSender, documentRepository);
        commandHandler.on(cmd);
        log.info(user.getId(), String.format("Documents generated and sent successfully [applicationId:%s].",
                UUIDUtils.toTinkUUID(application.getId())));
    }
}
