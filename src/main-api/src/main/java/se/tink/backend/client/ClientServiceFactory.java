package se.tink.backend.client;

import se.tink.backend.api.AbnAmroService;
import se.tink.backend.api.AccountService;
import se.tink.backend.api.ActivityService;
import se.tink.backend.api.ApplicationService;
import se.tink.backend.api.AuthenticationService;
import se.tink.backend.api.AuthorizationService;
import se.tink.backend.api.CalendarService;
import se.tink.backend.api.CategoryService;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.api.DeviceService;
import se.tink.backend.api.DocumentService;
import se.tink.backend.api.UserDataControlService;
import se.tink.backend.api.FollowService;
import se.tink.backend.api.FraudService;
import se.tink.backend.api.MerchantService;
import se.tink.backend.api.MonitoringService;
import se.tink.backend.api.NotificationService;
import se.tink.backend.api.OAuth2Service;
import se.tink.backend.api.PropertyService;
import se.tink.backend.api.ProviderService;
import se.tink.backend.api.SearchService;
import se.tink.backend.api.StatisticsService;
import se.tink.backend.api.TrackingService;
import se.tink.backend.api.TransactionService;
import se.tink.backend.api.TransferService;
import se.tink.backend.api.UserService;
import se.tink.backend.api.VersionService;
import se.tink.backend.api.ConsentService;
import se.tink.libraries.http.client.ServiceClassBuilder;

public class ClientServiceFactory implements ServiceFactory {

    private ServiceClassBuilder builder;
    private ClientAuthorizationConfigurator authenticationConfigurator;

    public ClientServiceFactory(ServiceClassBuilder builder, ClientAuthorizationConfigurator authenticationConfigurator) {
        this.builder = builder;
        this.authenticationConfigurator = authenticationConfigurator;
    }

    @Override
    public AccountService getAccountService() {
        return builder.build(AccountService.class);
    }

    @Override
    public ActivityService getActivityService() {
        return builder.build(ActivityService.class);
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return builder.build(AuthenticationService.class);
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return builder.build(AuthorizationService.class);
    }

    @Override
    public CalendarService getCalendarService() {
        return builder.build(CalendarService.class);
    }

    @Override
    public CategoryService getCategoryService() {
        return builder.build(CategoryService.class);
    }

    @Override
    public CredentialsService getCredentialsService() {
        return builder.build(CredentialsService.class);
    }

    @Override
    public DeviceService getDeviceService() {
        return builder.build(DeviceService.class);
    }

    @Override
    public DocumentService getDocumentService() {
        return builder.build(DocumentService.class);
    }

    @Override
    public FollowService getFollowService() {
        return builder.build(FollowService.class);
    }

    @Override
    public MerchantService getMerchantService() {
        return builder.build(MerchantService.class);
    }
    
    @Override
    public NotificationService getNotificationService() {
        return builder.build(NotificationService.class);
    }

    @Override
    public FraudService getFraudService() {
        return builder.build(FraudService.class);
    }
    
    @Override
    public SearchService getSearchService() {
        return builder.build(SearchService.class);
    }
    
    @Override
    public MonitoringService getMonitoringService() {
        return builder.build(MonitoringService.class);
    }

    @Override
    public ApplicationService getApplicationService() {
        return builder.build(ApplicationService.class);
    }

    @Override
    public ProviderService getProviderService() {
        return builder.build(ProviderService.class);
    }

    @Override
    public VersionService getVersionService() {
        return builder.build(VersionService.class);
    }

    @Override
    public OAuth2Service getOAuth2Service() {
        return builder.build(OAuth2Service.class);
    }

    @Override
    public PropertyService getPropertyService() {
        return builder.build(PropertyService.class);
    }

    @Override
    public ConsentService getConsentService() {
        return builder.build(ConsentService.class);
    }

    @Override
    public StatisticsService getStatisticsService() {
        return builder.build(StatisticsService.class);
    }

    @Override
    public TrackingService getTrackingService() {
        return builder.build(TrackingService.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return builder.build(TransactionService.class);
    }

    @Override
    public TransferService getTransferService() {
        return builder.build(TransferService.class);
    }

    @Override
    public UserService getUserService() {
        return builder.build(UserService.class);
    }

    @Override
    public UserDataControlService getUserDataControlService() {
        return builder.build(UserDataControlService.class);
    }
    
    @Override
    public AbnAmroService getAbnAmroService() {
        return builder.build(AbnAmroService.class);
    }
}
