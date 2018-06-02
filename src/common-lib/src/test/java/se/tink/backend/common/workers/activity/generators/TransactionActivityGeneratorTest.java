package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.ActivitiesConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Notification;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionActivityGeneratorTest {

    private ActivityGeneratorContext context = new ActivityGeneratorContext();

    @Before
    public void setUp() {
        UserProfile profile = new UserProfile();
        profile.setCurrency("EUR");
        profile.setPeriodMode(ResolutionTypes.MONTHLY);
        User user = new User();
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList());
        context.setUser(user);

        context.setActivitiesConfiguration(new ActivitiesConfiguration());

        context.setCurrencies(ImmutableMap.of("EUR", new Currency()));

        Category category = new Category();
        category.setId("categoryId");
        category.setType(CategoryTypes.EXPENSES);
        context.setCategoriesByCodeForLocale(ImmutableMap.of("categoryId", category));
        context.setCategoryConfiguration(new SECategories());

        context.setCatalog(Catalog.getCatalog("en_US"));

        Transaction transaction = new Transaction();
        transaction.setUserId(user.getId());
        transaction.setCategory(category);
        transaction.setDate(new Date());
        transaction.setDescription("McDonalds");
        context.setTransactions(singletonList(transaction));
    }

    /**
     * ABN AMRO user without the detailed push information flag. We expect the message to be generic without the
     * merchant name and the sensitive message to be null.
     */
    @Test
    public void testGenerateNotificationForAbn() {
        context.getActivitiesConfiguration().setShouldGenerateSensitiveMessage(false);

        Notification notification = generateNotification();

        assertThat(notification.getMessage()).isEqualTo("You've made a purchase.");
        assertThat(notification.getSensitiveMessage()).isNull();
    }

    /**
     * ABN AMRO user with the detailed push information flag. We expect the message to be generic but the sensitive
     * message contains the merchant name.
     */
    @Test
    public void testGenerateNotificationForAbnAmroWithDetailedPushInformation() {
        context.getActivitiesConfiguration().setShouldGenerateSensitiveMessage(false);
        context.getUser().getFlags().add(FeatureFlags.ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS);

        Notification notification = generateNotification();

        assertThat(notification.getMessage()).isEqualTo("You've made a purchase.");
        assertThat(notification.getSensitiveMessage()).isEqualTo("You had an expense charged by McDonalds");
    }

    @Test
    public void testGenerateNotificationForNonAbnAmro() {
        context.setCluster(Cluster.TINK);
        context.getUser().getFlags().clear();

        Notification notification = generateNotification();

        assertThat(notification.getMessage()).isEqualTo("You had an expense charged by McDonalds");
        assertThat(notification.getSensitiveMessage()).isNull();
    }

    private Notification generateNotification() {
        TransactionActivityGenerator generator = new TransactionActivityGenerator(new DeepLinkBuilderFactory(""));

        // Generate an activity
        generator.generateActivity(context);

        // We expect only one activity here
        assertThat(context.getActivities().size()).isEqualTo(1);

        Activity activity = context.getActivities().get(0);

        List<Notification> notifications = generator.createNotifications(activity, context);

        // We expect only one notification here
        assertThat(notifications.size()).isEqualTo(1);

        return notifications.get(0);
    }

}
