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

public interface ServiceFactory {
    String SERVICE_NAME = "main";

    AccountService getAccountService();

    ActivityService getActivityService();

    CalendarService getCalendarService();

    CategoryService getCategoryService();

    CredentialsService getCredentialsService();

    DeviceService getDeviceService();

    DocumentService getDocumentService();

    FollowService getFollowService();

    SearchService getSearchService();

    StatisticsService getStatisticsService();

    TransactionService getTransactionService();

    UserService getUserService();

    AuthenticationService getAuthenticationService();

    AuthorizationService getAuthorizationService();

    TrackingService getTrackingService();

    TransferService getTransferService();

    MerchantService getMerchantService();

    NotificationService getNotificationService();

    FraudService getFraudService();

    AbnAmroService getAbnAmroService();

    MonitoringService getMonitoringService();

    ApplicationService getApplicationService();

    ProviderService getProviderService();

    VersionService getVersionService();

    OAuth2Service getOAuth2Service();

    PropertyService getPropertyService();

    ConsentService getConsentService();

    UserDataControlService getUserDataControlService();
}
