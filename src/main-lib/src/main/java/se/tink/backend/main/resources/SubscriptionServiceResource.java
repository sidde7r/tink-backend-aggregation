package se.tink.backend.main.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.SubscriptionService;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.OutputSubscription;
import se.tink.backend.core.Subscription;
import se.tink.backend.core.SubscriptionListResponse;
import se.tink.backend.core.SubscriptionToken;
import se.tink.backend.core.SubscriptionType;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

@Path("/api/v1/subscription")
public class SubscriptionServiceResource implements SubscriptionService {

    @Context
    private HttpHeaders headers;

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTokenRepository subscriptionTokenRepository;
    private final UserRepository userRepository;

    private final AnalyticsController analyticsController;
    private final SubscriptionHelper subscriptionHelper;
    private static final LogUtils log = new LogUtils(SubscriptionServiceResource.class);

    public SubscriptionServiceResource(ServiceContext serviceContext) {
        this.subscriptionRepository = serviceContext.getRepository(SubscriptionRepository.class);
        this.subscriptionTokenRepository = serviceContext.getRepository(SubscriptionTokenRepository.class);
        this.userRepository = serviceContext.getRepository(UserRepository.class);

        this.analyticsController = new AnalyticsController(serviceContext.getEventTracker());
        subscriptionHelper = new SubscriptionHelper(serviceContext.getRepository(SubscriptionRepository.class),
                serviceContext.getRepository(SubscriptionTokenRepository.class));
    }

    private String getLocalizedTypeDescription(Catalog catalog, SubscriptionType type, String locale) {
        switch (type) {
        case ROOT:
            return catalog.getString("No thanks, I'd prefer to not get any e-mails from Tink.");
        case MONTHLY_SUMMARY_EMAIL:
            return catalog.getString("Monthly e-mail summarizing the last month's activity on my account.");
        case FAILING_CREDENTIALS_EMAIL:
            return catalog.getString("When there are issues with my bank or credit card connection.");
        case PRODUCT_UPDATES:
            return catalog.getString("When Tink has new and exciting product updates.");
        default:
            log.warn(String.format("Could not resolve a translation for '%s' to '%s'.", type.toString(), locale));
            return type.toString();
        }
    }

    @Override
    public SubscriptionListResponse list(String inputToken, String inputLocale) {
        final String locale = StringUtils.trim(Optional.ofNullable(inputLocale).orElse("")).length() > 0 ?
                inputLocale :
                I18NUtils.DEFAULT_LOCALE;

        if (inputToken == null) {
            // Unnecessary?
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        SubscriptionToken databaseToken = subscriptionTokenRepository.findOne(inputToken);
        if (databaseToken == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        final Map<SubscriptionType, Subscription> userSpecificSubscriptionByType = Maps.uniqueIndex(
                subscriptionRepository.findAllByUserId(databaseToken.getUserId()),
                Subscription::getType);

        // Instantiate catalog with fallback.

        final Catalog finalCatalog;
        {
            Catalog catalog = Catalog.getCatalog(locale);
            if (catalog == null) {
                log.warn(String.format("Unable to find the locale '%s'. Falling back to default '%s'.", locale,
                        I18NUtils.DEFAULT_LOCALE));
                catalog = Preconditions.checkNotNull(Catalog.getCatalog(I18NUtils.DEFAULT_LOCALE));
            }
            finalCatalog = catalog;
        }

        // Build the subscriptions.

        List<OutputSubscription> types = Lists.transform(Arrays.asList(SubscriptionType.values()),
                input -> {
                    // Transforming to be able to translate to the right locale. Catalog is not accessible in
                    // se.tink.backend.core.
                    OutputSubscription instance = new OutputSubscription();
                    instance.setId(input.toString());
                    instance.setParentId(input.getParent() != null ? input.getParent().toString() : null);
                    instance.setDescription(getLocalizedTypeDescription(finalCatalog, input, locale));
                    instance.setSubscribed(Optional.ofNullable(userSpecificSubscriptionByType.get(input))
                            .map(Subscription::isSubscribed).orElse(input.isSubscribedByDefault()));
                    instance.setInvertedSelection(input.getInvertedSelection());
                    return instance;
                });

        SubscriptionListResponse response = new SubscriptionListResponse();
        response.setTitle(finalCatalog.getString("Follow the money"));
        response.setSubtitle(finalCatalog.getString("Manage your Tink subscriptions"));
        response.setSaveButton(finalCatalog.getString("Save"));
        response.setSettingsSaved(finalCatalog.getString("Settings saved."));
        response.setAdditionalSaveInfo(finalCatalog.getString("We are sad to see you go but maybe you are not the email type of person? Follow us on <a href=\"https://twitter.com/tink\" target=\"_blank\">Twitter</a> and <a href=\"https://www.facebook.com/tink.se\" target=\"_blank\">Facebook</a> instead."));
        response.setSubscriptions(types);
        return response;
    }

    @Override
    public void unsubscribe(String token, String subscriptionTypeId) {

        if (token == null || subscriptionTypeId == null) {
            // Unnecessary?
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        SubscriptionToken databaseToken = subscriptionTokenRepository.findOne(token);
        if (databaseToken == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        SubscriptionType subscriptionType = null;
        try {
            subscriptionType = SubscriptionType.valueOf(subscriptionTypeId);
        } catch (IllegalArgumentException e) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        saveSubscriptionRecursively(databaseToken.getUserId(), Preconditions.checkNotNull(subscriptionType), false);
        
        pushToAnalytics(databaseToken.getUserId(), "subscription.unsubscribe", subscriptionType);
    }
    
    private void saveSubscriptionRecursively(String userId, SubscriptionType subscriptionType, boolean subscribed) {
        ArrayList<Subscription> toSave = Lists.newArrayList(constructSubscription(userId, Preconditions.checkNotNull(subscriptionType), subscribed));
        
        // Set each child description, too (if there are any).
        
        for (SubscriptionType type : SubscriptionType.values()) {
            if (type.hasAncestor(subscriptionType)) {
                toSave.add(constructSubscription(userId, Preconditions.checkNotNull(type), subscribed));
            }
        }
        
        // Save to database.

        subscriptionRepository.save(toSave);
    }
    
    private Subscription constructSubscription(String userId, SubscriptionType type, boolean subscribed) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setType(type);
        subscription.setSubscribed(subscribed);
        return subscription;
    }
    
    private void pushToAnalytics(final String userId, String eventType, SubscriptionType subscriptionType) {
        User user = userRepository.findOne(userId);

        Map<String, Object> eventProperties = Maps.newHashMap();
        if (subscriptionType != null) {
            eventProperties.put("Subscription type", subscriptionType.name());
        }
        analyticsController.trackUserEvent(user, eventType, eventProperties, RequestHeaderUtils.getRemoteIp(headers));
    }

    @Override
    public void subscribe(String token, String subscriptionTypeId) {

        if (token == null || subscriptionTypeId == null) {
            // Unnecessary?
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        SubscriptionToken databaseToken = subscriptionTokenRepository.findOne(token);
        if (databaseToken == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        SubscriptionType subscriptionType = null;
        try {
            subscriptionType = SubscriptionType.valueOf(subscriptionTypeId);
        } catch (IllegalArgumentException e) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        saveSubscriptionRecursively(databaseToken.getUserId(), Preconditions.checkNotNull(subscriptionType), true);

        pushToAnalytics(databaseToken.getUserId(), "subscription.subscribe", subscriptionType);
    }

}
