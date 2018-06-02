package se.tink.backend.client;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import java.util.Optional;
import se.tink.backend.api.ApplicationService;
import se.tink.backend.api.CalendarService;
import se.tink.backend.categorization.factory.ShadowCategorizersFactoryCreator;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.controllers.EmailAndPasswordAuthenticationServiceController;
import se.tink.backend.main.resources.AbnAmroServiceResource;
import se.tink.backend.main.resources.AuthorizationServiceResource;
import se.tink.backend.main.resources.CredentialsServiceResource;
import se.tink.backend.main.resources.DocumentServiceResource;
import se.tink.backend.main.resources.FraudServiceResource;
import se.tink.backend.main.resources.LoanServiceResource;
import se.tink.backend.main.resources.MerchantServiceResource;
import se.tink.backend.main.resources.MonitoringServiceResource;
import se.tink.backend.main.resources.NotificationServiceResource;
import se.tink.backend.main.resources.OAuth2ServiceResource;
import se.tink.backend.main.resources.SearchServiceResource;
import se.tink.backend.main.resources.SubscriptionServiceResource;
import se.tink.backend.main.resources.TransactionServiceResource;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.oauth.OAuthGrpcClient;

public class InProcessServiceFactoryBuilder {
    private Optional<Environment> environment;
    private ServiceContext serviceContext;
    private MetricRegistry metricRegistry;
    private ClusterCategories categories;

    public InProcessServiceFactoryBuilder(ServiceContext serviceContext, Optional<Environment> environment,
            MetricRegistry metricRegistry, ClusterCategories categories) {
        this.serviceContext = serviceContext;
        this.environment = environment;
        this.metricRegistry = metricRegistry;
        this.categories = categories;
    }

    public InProcessServiceFactory buildAndRegister(Injector injector) {
        Preconditions.checkNotNull(serviceContext);

        FirehoseQueueProducer firehoseQueueProducer = injector.getInstance(FirehoseQueueProducer.class);
        ShadowCategorizersFactoryCreator shadowCategorizersFactoryCreator = injector
                .getInstance(ShadowCategorizersFactoryCreator.class);
        ElasticSearchClient elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
        InProcessServiceFactory inProcessServiceFactory = (InProcessServiceFactory) serviceContext.getServiceFactory();

        UserDeviceRepository userDeviceRepository = serviceContext.getRepository(UserDeviceRepository.class);
        UserDeviceController userDeviceController = new UserDeviceController(userDeviceRepository);

        // For all clusters.
        inProcessServiceFactory.setCredentialsService(new CredentialsServiceResource(
                serviceContext,
                userDeviceController,
                firehoseQueueProducer,
                metricRegistry));

        inProcessServiceFactory.setDocumentService(new DocumentServiceResource(serviceContext));
        inProcessServiceFactory.setSearchService(new SearchServiceResource(serviceContext, elasticSearchClient));
        inProcessServiceFactory
                .setTransactionService(new TransactionServiceResource(serviceContext, firehoseQueueProducer,
                        metricRegistry, categories, shadowCategorizersFactoryCreator, elasticSearchClient));
        inProcessServiceFactory.setNotificationService(new NotificationServiceResource(serviceContext));
        inProcessServiceFactory.setSubscriptionService(new SubscriptionServiceResource(serviceContext));
        inProcessServiceFactory.setLoanService(new LoanServiceResource(serviceContext));
        inProcessServiceFactory.setMerchantService(new MerchantServiceResource(serviceContext, elasticSearchClient));
        inProcessServiceFactory.setFraudService(new FraudServiceResource(serviceContext, metricRegistry));
        inProcessServiceFactory.setMonitoringService(new MonitoringServiceResource(serviceContext));
        inProcessServiceFactory.setApplicationService(injector.getInstance(ApplicationService.class));
        inProcessServiceFactory.setCalendarService(injector.getInstance(CalendarService.class));

        if (environment.isPresent()) {

            environment.get().jersey().register(inProcessServiceFactory.getAccountService());
            environment.get().jersey().register(inProcessServiceFactory.getActivityService());
            environment.get().jersey().register(inProcessServiceFactory.getApplicationService());
            environment.get().jersey().register(inProcessServiceFactory.getCalendarService());
            environment.get().jersey().register(inProcessServiceFactory.getCategoryService());
            environment.get().jersey().register(inProcessServiceFactory.getConsentService());
            environment.get().jersey().register(inProcessServiceFactory.getCredentialsService());
            environment.get().jersey().register(inProcessServiceFactory.getDocumentService());
            environment.get().jersey().register(inProcessServiceFactory.getFollowService());
            environment.get().jersey().register(inProcessServiceFactory.getFraudService());
            environment.get().jersey().register(inProcessServiceFactory.getLoanService());
            environment.get().jersey().register(inProcessServiceFactory.getMerchantService());
            environment.get().jersey().register(inProcessServiceFactory.getMonitoringService());
            environment.get().jersey().register(inProcessServiceFactory.getNotificationService());
            environment.get().jersey().register(inProcessServiceFactory.getProviderService());
            environment.get().jersey().register(inProcessServiceFactory.getSearchService());
            environment.get().jersey().register(inProcessServiceFactory.getStatisticsService());
            environment.get().jersey().register(inProcessServiceFactory.getSubscriptionService());
            environment.get().jersey().register(inProcessServiceFactory.getTransactionService());
            environment.get().jersey().register(inProcessServiceFactory.getUserService());
            environment.get().jersey().register(inProcessServiceFactory.getVersionService());
            environment.get().jersey().register(inProcessServiceFactory.getUserDataControlService());
        }

        // Tink cluster services.

        if (serviceContext.getConfiguration().getCluster() == Cluster.TINK) {
            inProcessServiceFactory.setAuthorizationService(new AuthorizationServiceResource(serviceContext));

            // TODO: nina.olofsson@tink.se: step-by-step replacing with gRPC here.
            EndpointConfiguration oauth = serviceContext.getConfiguration().getEndpoints().getOauth();
            OAuthGrpcClient oauthClient = (oauth == null || oauth.getUrl() == null) ?
                    null :
                    injector.getInstance(OAuthGrpcClient.class);

            inProcessServiceFactory.setOAuth2Service(new OAuth2ServiceResource(serviceContext,
                    metricRegistry, oauthClient, injector.getInstance(MailSender.class), injector.getInstance(
                    EmailAndPasswordAuthenticationServiceController.class)));

            if (environment.isPresent()) {
                environment.get().jersey().register(inProcessServiceFactory.getAuthenticationService());
                environment.get().jersey().register(inProcessServiceFactory.getAuthorizationService());
                environment.get().jersey().register(inProcessServiceFactory.getOAuth2Service());
                environment.get().jersey().register(inProcessServiceFactory.getPropertyService());
            }
        }

        // ABN AMRO cluster services

        if (serviceContext.getConfiguration().getCluster() == Cluster.ABNAMRO) {
            inProcessServiceFactory.setAbnAmroService(new AbnAmroServiceResource(metricRegistry, serviceContext));

            if (environment.isPresent()) {
                environment.get().jersey().register(inProcessServiceFactory.getAbnAmroService());
            }
        }

        return inProcessServiceFactory;
    }
}
