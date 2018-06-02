package se.tink.backend.common.workers.activity;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Activity;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationsGenerationTest {

    private static final String USER_ID = "44d7bfe269154dfc98f8d4dd31f5b777";
    private final Activity activity = new Activity();
    private ActivityGenerator generator;

    @Before
    public void setUp() {
        activity.setKey("activityKey");
        activity.setType(Activity.Types.BALANCE);
        activity.setUserId(USER_ID);
        activity.setDate(new Date());
        activity.setTitle("Title");

        generator = new ActivityGenerator(ActivityGenerator.class, 70, new DeepLinkBuilderFactory("")) {
            @Override
            public void generateActivity(ActivityGeneratorContext context) {
            }

            @Override
            public boolean isNotifiable() {
                return true;
            }
        };
    }

    @Test
    public void generateNotificationsWithoutUserProfile() {
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        User user = new User();
        user.setId(USER_ID);
        context.setUser(user);

        assertEquals(1, generator.generateNotifications(activity, context).size());
    }

    @Test
    public void generateNotificationsWithoutSettings() {
        User user = new User();
        user.setId(USER_ID);
        user.setProfile(new UserProfile());
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        context.setUser(user);

        assertEquals(1, generator.generateNotifications(activity, context).size());
    }

    @Test
    public void generateNotificationsWithoutAnythingEnabled() {
        NotificationSettings notificationSettings = new NotificationSettings();
        assertTrue(notificationSettings.getBalance());

        UserProfile profile = new UserProfile();
        profile.setNotificationSettings(notificationSettings);
        User user = new User();
        user.setId(USER_ID);
        user.setProfile(profile);
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        context.setUser(user);

        assertEquals(1, generator.generateNotifications(activity, context).size());
    }

    @Test
    public void generateNotificationsWithoutKey() {
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setBalance(true);
        UserProfile profile = new UserProfile();
        profile.setNotificationSettings(notificationSettings);
        User user = new User();
        user.setId(USER_ID);
        user.setProfile(profile);
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        context.setUser(user);

        activity.setKey(null);

        assertTrue(generator.generateNotifications(activity, context).isEmpty());
    }

    @Test
    public void generateEnabledNotifications() {
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setBalance(true);
        UserProfile profile = new UserProfile();
        profile.setNotificationSettings(notificationSettings);
        User user = new User();
        user.setId(USER_ID);
        user.setProfile(profile);
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        context.setUser(user);

        assertEquals(1, generator.generateNotifications(activity, context).size());
    }

    @Test
    public void testNotNotifiableActivities() {
        ActivityGeneratorContext context = new ActivityGeneratorContext();
        User user = new User();
        user.setId(USER_ID);
        context.setUser(user);

        ActivityGenerator generator = new ActivityGenerator(ActivityGenerator.class, 70, new DeepLinkBuilderFactory("")) {
            @Override
            public void generateActivity(ActivityGeneratorContext context) {
            }

            @Override
            public boolean isNotifiable() {
                return false;
            }
        };

        assertThat(generator.generateNotifications(activity, context)).isEmpty();
    }
}
