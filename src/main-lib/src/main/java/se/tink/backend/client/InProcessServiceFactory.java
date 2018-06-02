package se.tink.backend.client;

import com.google.inject.Inject;
import se.tink.backend.api.AbnAmroService;
import se.tink.backend.api.AccountService;
import se.tink.backend.api.ActivityService;
import se.tink.backend.api.ApplicationService;
import se.tink.backend.api.AuthenticationService;
import se.tink.backend.api.AuthorizationService;
import se.tink.backend.api.CalendarService;
import se.tink.backend.api.CategoryService;
import se.tink.backend.api.ConsentService;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.api.DeviceService;
import se.tink.backend.api.DocumentService;
import se.tink.backend.api.UserDataControlService;
import se.tink.backend.api.FollowService;
import se.tink.backend.api.FraudService;
import se.tink.backend.api.LoanService;
import se.tink.backend.api.MerchantService;
import se.tink.backend.api.MonitoringService;
import se.tink.backend.api.NotificationService;
import se.tink.backend.api.OAuth2Service;
import se.tink.backend.api.PropertyService;
import se.tink.backend.api.ProviderService;
import se.tink.backend.api.SearchService;
import se.tink.backend.api.StatisticsService;
import se.tink.backend.api.SubscriptionService;
import se.tink.backend.api.TrackingService;
import se.tink.backend.api.TransactionService;
import se.tink.backend.api.TransferService;
import se.tink.backend.api.UserService;
import se.tink.backend.api.VersionService;

public class InProcessServiceFactory implements ServiceFactory {
    private final AccountService accountService;
    private final ActivityService activityService;
    private final PropertyService propertyService;
    private final AuthenticationService authenticationService;
    private AuthorizationService authorizationService;
    private CalendarService calendarService;
    private final CategoryService categoryService;
    private CredentialsService credentialsService;
    private DocumentService documentService;
    private final FollowService followService;
    private FraudService fraudService;
    private LoanService loanService;
    private MerchantService merchantService;
    private MonitoringService monitoringService;
    private NotificationService notificationService;
    private SearchService searchService;
    private final StatisticsService statisticsService;
    private SubscriptionService subscriptionService;
    private final TrackingService trackingService;
    private TransactionService transactionService;
    private final TransferService transferService;
    private final UserService userService;
    private AbnAmroService abnAmroService;
    private final DeviceService deviceService;
    private ApplicationService applicationService;
    private final ProviderService providerService;
    private VersionService versionService;
    private OAuth2Service oauth2Service;
    private final ConsentService consentService;
    private UserDataControlService userDataControlService;

    @Inject
    public InProcessServiceFactory(AccountService accountService,
            ActivityService activityService,
            CategoryService categoryService,
            StatisticsService statisticsService,
            TrackingService trackingService, UserService userService,
            ProviderService providerService,
            VersionService versionService,
            PropertyService propertyService,
            AuthenticationService authenticationService, FollowService followService,
            DeviceService deviceService,
            TransferService transferService,
            ConsentService consentService,
            UserDataControlService userDataControlService) {
        this.accountService = accountService;
        this.activityService = activityService;
        this.categoryService = categoryService;
        this.statisticsService = statisticsService;
        this.trackingService = trackingService;
        this.userService = userService;
        this.providerService = providerService;
        this.versionService = versionService;
        this.propertyService = propertyService;
        this.authenticationService = authenticationService;
        this.followService = followService;
        this.deviceService = deviceService;
        this.transferService = transferService;
        this.consentService = consentService;
        this.userDataControlService = userDataControlService;
    }

    @Override
    public AccountService getAccountService() {
        return accountService;
    }

    @Override
    public ActivityService getActivityService() {
        return activityService;
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    @Override
    public CalendarService getCalendarService() {
        return calendarService;
    }

    @Override
    public CategoryService getCategoryService() {
        return categoryService;
    }

    @Override
    public CredentialsService getCredentialsService() {
        return credentialsService;
    }

    @Override
    public DeviceService getDeviceService() {
        return deviceService;
    }

    @Override
    public DocumentService getDocumentService() {
        return documentService;
    }

    @Override
    public FollowService getFollowService() {
        return followService;
    }

    @Override
    public FraudService getFraudService() {
        return fraudService;
    }

    @Override
    public MerchantService getMerchantService() {
        return merchantService;
    }
    
    @Override
    public MonitoringService getMonitoringService() {
        return monitoringService;
    }
    
    @Override
    public NotificationService getNotificationService() {
        return notificationService;
    }

    @Override
    public SearchService getSearchService() {
        return searchService;
    }

    @Override
    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }
    
    @Override
    public TrackingService getTrackingService() {
        return trackingService;
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public TransferService getTransferService() {
        return transferService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public VersionService getVersionService() {
        return versionService;
    }

    @Override
    public OAuth2Service getOAuth2Service() {
        return oauth2Service;
    }

    @Override
    public PropertyService getPropertyService() {
        return propertyService;
    }

    @Override
    public AbnAmroService getAbnAmroService() {
        return abnAmroService;
    }

    @Override
    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Override
    public ProviderService getProviderService() {
        return providerService;
    }

    @Override
    public UserDataControlService getUserDataControlService() {
        return userDataControlService;
    }

    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public void setCredentialsService(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    public void setFraudService(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    public void setMonitoringService(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }
    
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setLoanService(LoanService loanService) {
        this.loanService = loanService;
    }

    public LoanService getLoanService() {
        return loanService;
    }

    public void setAbnAmroService(AbnAmroService abnAmroService) {
        this.abnAmroService = abnAmroService;
    }

    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void setOAuth2Service(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public ConsentService getConsentService() {
        return consentService;
    }
}
