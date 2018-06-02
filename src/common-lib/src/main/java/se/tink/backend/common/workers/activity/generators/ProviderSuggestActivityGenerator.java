package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.SuggestProviderSearcher;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.DeepLinkHtmlActivityData;
import se.tink.backend.common.workers.activity.generators.models.HtmlActivityIconData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.StringUtils;

public class ProviderSuggestActivityGenerator extends CustomHtmlActivtyGenerator {
    protected Catalog catalog;
    private Set<String> connectedProviders;
    private SuggestProviderSearcher suggestProviderSearcher;
    private Map<String, Provider> providersByName;
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    public ProviderSuggestActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(ProviderSuggestActivityGenerator.class, 51, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    protected String getActivityHtmlHead() {
        return TemplateUtils.getTemplate("data/templates/default-html/head.html");
    }

    protected String getActivityHtmlBody(Provider provider) {
        String bodyFormat = TemplateUtils.getTemplate("data/templates/default-html/feed.html");
        String body = Catalog.format(bodyFormat, Catalog.format(
                catalog.getString("It seems like you have an account at {0} that you haven't connected to Tink."),
                Catalog.format("<span class=\"bold\">{0}</span>", provider.getDisplayName())));
        return body;
    }

    private Activity createActivity(ActivityGeneratorContext context, Provider provider, Date date) {

        DeepLinkHtmlActivityData data = new DeepLinkHtmlActivityData();

        data.setTrackingName(String.format("ProviderSuggest (add %s)", provider.getName()));
        data.setIcon(new HtmlActivityIconData("accounts", "pink"));
        data.setActivityHtml(createHtml(getActivityHtmlHead(), getActivityHtmlBody(provider)));

        data.setDeepLink(deepLinkBuilderFactory.provider(provider.getName())
                .withSource("tink")
                .withMedium("feed-activity")
                .withCampaign("provider-suggest")
                .build());

        data.setButtonLabel(catalog.getString("Add account"));

        String key = createKey(provider);
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        return createActivity(context.getUser().getId(), date, "html/deep-link", provider.getDisplayName(), null, data,
                key, feedActivityIdentifier);
    }

    private Activity createNativeActivity(ActivityGeneratorContext context, Provider provider, Date date) {
        String key = createHtmlActivityKey(provider);
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        return createActivity(context.getUser().getId(), date, Activity.Types.SUGGEST_PROVIDER,
                provider.getDisplayName(), null, provider, key, feedActivityIdentifier);
    }

    private String createKey(Provider provider) {
        return String.format("provider-suggest.%s", provider.getName());
    }

    private String createHtmlActivityKey(Provider provider) {
        return String.format("suggest-provider.%s", provider.getName());
    }

    private String extractProviderName(String key) {
        if (key.contains(".")) {
            return key.split("\\.")[1];
        }

        return null;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        this.catalog = context.getCatalog();
        this.providersByName = context.getProvidersByName();
        this.suggestProviderSearcher = new SuggestProviderSearcher(context.getProvidersByName());
        this.connectedProviders = Sets.newHashSet(Lists.transform(context.getCredentials(),
                Credentials::getProviderName));

        Map<String, Activity> activities = Maps.newHashMap();
        Map<String, Activity> nativeActivities = Maps.newHashMap();

        for (final Transaction transaction : context.getTransactions()) {
            Optional<String> providerName = suggestProviderSearcher.suggestName(transaction);
            if (!providerName.isPresent()) {
                continue;
            }
            Set<Provider> providerVariants = suggestProviderSearcher.getProviderVariants(providerName.get());

            // Be conservative: No variant is allowed to be connected for a suggestion to be made.
            if (!hasConnectedProviderVariant(providerVariants)) {
                for (Provider provider : providerVariants) {
                    if (provider != null && provider.getStatus() == ProviderStatuses.ENABLED) {
                        // Use the most recent transaction identifying a specific provider, as the base for the
                        // activity.
                        Activity activity = activities.get(provider.getName());
                        if (activity == null || activity.getDate().before(transaction.getDate())) {
                            activities.put(providerName.get(),
                                    createActivity(context, provider, transaction.getDate()));
                        }

                        if (activity == null || activity.getDate().before(transaction.getDate())) {
                            nativeActivities.put(providerName.get(),
                                    createNativeActivity(context, provider, transaction.getDate()));
                        }

                        break;
                    }
                }
            }
        }

        if (activities.size() > 0) {
            context.addActivities(Lists.newArrayList(activities.values()));
        }
        if (nativeActivities.size() > 0) {
            context.addActivities(Lists.newArrayList(nativeActivities.values()));
        }
    }

    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        List<Notification> notifications = Lists.newArrayList();

        String providerName = extractProviderName(activity.getKey());

        if (!Strings.isNullOrEmpty(providerName)) {
            Provider provider = providersByName.get(providerName);

            if (provider != null) {
                Notification.Builder notification = new Notification.Builder()
                        .userId(activity.getUserId())
                        .title(provider.getDisplayName())
                        .date(activity.getDate())
                        .message(context.getCatalog().getString("Connect to Tink and get an even better overview"))
                        .key(activity.getKey())
                        .type("provider-suggest")
                        .url(deepLinkBuilderFactory.provider(provider.getName())
                                .withSource("tink")
                                .withMedium("notification")
                                .withCampaign("provider-suggest")
                                .build())
                        .groupable(false);

                notifications.addAll(buildNotificationsSilentlyFailing(activity.getUserId(), notification));
            }
        }

        return notifications;
    }

    private boolean hasConnectedProviderVariant(Set<Provider> providerVariants) {
        for (Provider provider : providerVariants) {
            if (provider != null && connectedProviders.contains(provider.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Since we are constructing both native (new) and html (old), only generate notification for native.
     */
    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        if (!Objects.equals(activity.getType(), Activity.Types.SUGGEST_PROVIDER)) {
            return Lists.newArrayList();
        }

        return super.generateNotifications(activity, context);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
