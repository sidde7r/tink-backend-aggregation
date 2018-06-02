package se.tink.backend.system.cli.seeding;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Optional;
import se.tink.backend.aggregation.rpc.RefreshApplicationRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationCredentialsController;
import se.tink.backend.common.application.ApplicationProcessor;
import se.tink.backend.common.application.ApplicationProcessorFactory;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.uuid.UUIDUtils;

public class RefreshApplicationsController {
    private final LogUtils log;
    private final ApplicationDAO applicationDAO;
    private final ApplicationProcessorFactory applicationProcessorFactory;
    private final ApplicationCredentialsController applicationCredentialsController;
    private final ServiceContext serviceContext;
    private final AggregationControllerCommonClient aggregationControllerClient;

    public RefreshApplicationsController(ServiceContext serviceContext) {
        this.log = new LogUtils(RefreshApplicationsController.class);

        this.serviceContext = serviceContext;
        this.applicationDAO = serviceContext.getDao(ApplicationDAO.class);


        ProviderImageProvider providerImageProvider = new ProviderImageProvider(
                serviceContext.getRepository(ProviderImageRepository.class));

        this.aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();
        this.applicationProcessorFactory = new ApplicationProcessorFactory(serviceContext, providerImageProvider);
        this.applicationCredentialsController = new ApplicationCredentialsController(
                serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                serviceContext.getRepository(CredentialsEventRepository.class),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getRepository(ProviderRepository.class),
                serviceContext.getRepository(UserRepository.class),
                serviceContext.getDao(ProductDAO.class),
                serviceContext.getAggregationServiceFactory(),
                new AnalyticsController(serviceContext.getEventTracker()),
                serviceContext.isProvidersOnAggregation());
    }

    public void refreshApplications(User user) {
        for (Application application : applicationDAO.findByUserId(UUIDUtils.fromTinkUUID(user.getId()))) {
            refreshApplication(user, application);
        }
    }

    public void refreshApplication(User user, Application application) {
        if (!Objects.equal(application.getType(), ApplicationType.SWITCH_MORTGAGE_PROVIDER)) {
            return;
        }

        switch (application.getStatus().getKey()) {
        case SIGNED:
        case SUPPLEMENTAL_INFORMATION_REQUIRED:
        case APPROVED: {
            HashMap<ApplicationPropertyKey, Object> properties = application.getProperties();
            Object externalId = properties.get(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID);

            if (externalId == null) {
                log.error(user.getId(), String.format(
                        "There's no external reference attached to the application [applicationId:%s].",
                        UUIDUtils.toTinkUUID(application.getId())));
                return;
            }

            ApplicationProcessor processor = applicationProcessorFactory.create(application, user);
            processor.attachProduct(application);

            Optional<ProductArticle> article = application.getProductArticle();

            GenericApplication genericApplication = processor.getGenericApplication(application);

            HashMap<RefreshApplicationParameterKey, Object> parameters = Maps.newHashMap();
            parameters.put(RefreshApplicationParameterKey.EXTERNAL_ID, externalId);

            Provider provider;
            if (serviceContext.isProvidersOnAggregation()) {
                provider = aggregationControllerClient.getProviderByName(article.get().getProviderName());
            } else {
                provider = serviceContext.getRepository(ProviderRepository.class).findByName(
                        article.get().getProviderName());
            }

            Credentials credentials = applicationCredentialsController.getOrCreateCredentials(genericApplication);

            if (serviceContext.isUseAggregationController()) {
                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshApplicationRequest refreshApplicationRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshApplicationRequest();
                refreshApplicationRequest.setApplicationId(application.getId());
                refreshApplicationRequest.setCredentials(credentials);
                refreshApplicationRequest.setParameters(parameters);
                refreshApplicationRequest.setProductType(ProductType.MORTGAGE);
                refreshApplicationRequest.setProvider(provider);
                refreshApplicationRequest.setUser(user);
                try {
                    log.debug(user.getId(), String.format(
                            "Refresh application [applicationId:%s, providerName:%s, externalId:%s]",
                            UUIDUtils.toTinkUUID(application.getId()), credentials.getProviderName(),
                            String.valueOf(externalId)));

                    serviceContext.getAggregationControllerCommonClient().
                            refreshApplication(refreshApplicationRequest);
                } catch (Exception e) {
                    log.error(
                            user.getId(),
                            String.format("Unable to call aggregation service [applicationId:%s].",
                                    UUIDUtils.toTinkUUID(application.getId())), e);
                }
            } else {
                RefreshApplicationRequest request = new RefreshApplicationRequest();
                request.setApplicationId(application.getId());
                request.setCredentials(CoreCredentialsMapper.toAggregationCredentials(credentials));
                request.setParameters(parameters);
                request.setProductType(CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE));
                request.setProvider(CoreProviderMapper.toAggregationProvider(provider));
                request.setUser(CoreUserMapper.toAggregationUser(user));

                try {
                    log.debug(user.getId(), String.format(
                            "Refresh application [applicationId:%s, providerName:%s, externalId:%s]",
                            UUIDUtils.toTinkUUID(application.getId()), credentials.getProviderName(),
                            String.valueOf(externalId)));

                    serviceContext.getAggregationServiceFactory().getAggregationService().refreshApplication(request);
                } catch (Exception e) {
                    log.error(
                            user.getId(),
                            String.format("Unable to call aggregation service [applicationId:%s].",
                                    UUIDUtils.toTinkUUID(application.getId())), e);
                }
            }
            break;
        }
        default:
            // Do nothing.
            // The statuses before `SIGNED` are internal, and the other external statuses are to be
            // interpreted as final states.
        }
    }
}
