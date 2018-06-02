package se.tink.backend.system.notifications;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSender;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationEvent;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.ListUtils.union;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationGatewayTest {
    @Mock NotificationsConfiguration notificationsConfiguration;
    @Mock NotificationDao notificationDao;
    @InjectMocks NotificationGateway gateway;
    @Captor ArgumentCaptor<List<Notification>> notificationListCaptor;
    @Mock
    MobileNotificationSender sender;

    @Before
    public void setUp() throws Exception {
        gateway = new NotificationGateway(sender, notificationDao, notificationsConfiguration, new MetricRegistry());
        gateway.start();
    }

    @After
    public void tearDown() throws Exception {
        gateway.stop();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendNotifications() throws ExecutionException, InterruptedException, TimeoutException {
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setFraud(true);
        notificationSettings.setBalance(true);
        notificationSettings.setSummaryMonthly(true);

        Notification fraudNotification = newNotification("fraudTitle");
        fraudNotification.setType(Activity.Types.FRAUD);
        List<Notification> user1Notifications = singletonList(fraudNotification);

        Notification highBalanceNotification = newNotification("highBalanceTitle");
        highBalanceNotification.setType(Activity.Types.BALANCE_HIGH);
        Notification monthlySummaryNotification = newNotification("monthlySummaryTitle");
        monthlySummaryNotification.setType(Activity.Types.MONTHLY_SUMMARY);
        List<Notification> user2Notifications = asList(highBalanceNotification, monthlySummaryNotification);

        List<Notification> allNotifications = union(user1Notifications, user2Notifications);

        User user1 = newUser(notificationSettings);
        User user2 = newUser(notificationSettings);

        when(notificationsConfiguration.shouldSendNotifications(user1)).thenReturn(true);
        when(notificationsConfiguration.shouldSendNotifications(user2)).thenReturn(true);

        boolean encrypted = true;
        SendNotificationsRequest request = new SendNotificationsRequest();
        request.addUserNotifications(user1, user1Notifications, encrypted);
        request.addUserNotifications(user2, user2Notifications, encrypted);
        List<ListenableFuture<NotificationGateway.FutureNotifications>> futures = gateway.sendNotifications(request);

        // Block until everything is done
        Uninterruptibles.getUninterruptibly(Futures.successfulAsList(futures), 10, TimeUnit.SECONDS);

        verify(notificationDao).save(notificationListCaptor.capture(), Mockito.eq(NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL));
        assertTrue(notificationListCaptor.getValue().containsAll(allNotifications));
        verify(notificationDao).markAsSent(user1Notifications, encrypted);
        verify(notificationDao).markAsSent(user2Notifications, encrypted);
        boolean track = true;

        Assert.assertEquals(2, futures.size());
        Assert.assertEquals(new NotificationGateway.FutureNotifications(user1, encrypted, track, user1Notifications),
                futures.get(0).get());
        Assert.assertEquals(new NotificationGateway.FutureNotifications(user2, encrypted, track, user2Notifications),
                futures.get(1).get());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void saveMarkAsReadAndContinueIfNotificationSendingFailed()
            throws ExecutionException, InterruptedException, TimeoutException {
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setFraud(true);
        notificationSettings.setBalance(true);
        notificationSettings.setSummaryMonthly(true);

        Notification fraudNotification = newNotification("fraudTitle");
        fraudNotification.setType(Activity.Types.FRAUD);
        List<Notification> user1Notifications = singletonList(fraudNotification);

        Notification highBalanceNotification = newNotification("highBalanceTitle");
        highBalanceNotification.setType(Activity.Types.BALANCE_HIGH);
        List<Notification> user2Notifications = singletonList(highBalanceNotification);

        List<Notification> allNotifications = union(user1Notifications, user2Notifications);

        User user1 = newUser(notificationSettings);
        User user2 = newUser(notificationSettings);

        when(notificationsConfiguration.shouldSendNotifications(user1)).thenReturn(true);
        when(notificationsConfiguration.shouldSendNotifications(user2)).thenReturn(true);
        boolean encrypted = true;
        boolean track = true;

        SendNotificationsRequest request = new SendNotificationsRequest();
        request.addUserNotifications(user1, user1Notifications, encrypted);
        request.addUserNotifications(user2, user2Notifications, encrypted);
        MobileNotificationSender currentMobileChannel = gateway
                .getNotificationSender();
        try {
            gateway.setNotificationSender(new MobileNotificationSender() {
                @Override
                public void start() throws Exception {
                    // Deliberately left empty.
                }

                @Override
                public void stop() throws Exception {
                    // Deliberately left empty.
                }

                @Override
                public void sendNotifications(User user, List<Notification> notifications, boolean shouldBeTracked,
                        boolean encrypted) {
                    throw new RuntimeException("From test. Expected.");
                }
            });
            List<ListenableFuture<NotificationGateway.FutureNotifications>> futures = gateway
                    .sendNotifications(request);

            // Block until everything is done
            Uninterruptibles.getUninterruptibly(Futures.successfulAsList(futures), 10, TimeUnit.SECONDS);

            verify(notificationDao).save(notificationListCaptor.capture(), Mockito.eq(NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL));
            assertTrue(notificationListCaptor.getValue().containsAll(allNotifications));
            verify(notificationDao).markAsSent(user1Notifications, encrypted);
            verify(notificationDao).markAsSent(user2Notifications, encrypted);

            Assert.assertEquals(2, futures.size());
            assertFailure(futures.get(0));
            assertFailure(futures.get(1));
        } finally {
            // Needed to properly stop() it in tearDown.
            gateway.setNotificationSender(currentMobileChannel);
        }

    }

    private void assertFailure(ListenableFuture<?> future) {
        try {
            future.get();
            Assert.fail("Expected failure.");
        } catch (InterruptedException e) {
            // Deliberately left empty.
        } catch (ExecutionException e) {
            // Deliberately left empty.
        }
    }

    @Test
    public void doNotSendInvalidNotifications() throws ExecutionException, InterruptedException {
        Notification notification = new Notification("userId");
        assertFalse(notification.isValid());
        User user = newUser(new NotificationSettings());
        boolean encrypted = true;
        //noinspection ConstantConditions
        SendNotificationsRequest request = new SendNotificationsRequest(user, singletonList(notification), encrypted);
        List<ListenableFuture<NotificationGateway.FutureNotifications>> futures = gateway.sendNotifications(request);
        Assert.assertEquals(0, futures.size());

        verify(notificationDao).save(Lists.<Notification>newArrayList(), NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL);

        verifyNoMoreInteractions(notificationDao);
    }

    @Test
    public void doNotSendDisabledNotifications() throws ExecutionException, InterruptedException {
        Notification notification = newNotification("title");
        notification.setType(Activity.Types.INCOME);
        UserProfile profile = new UserProfile();
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setIncome(false);
        profile.setNotificationSettings(notificationSettings);
        User user = new User();
        user.setProfile(profile);

        boolean encrypted = true;
        //noinspection ConstantConditions
        SendNotificationsRequest request = new SendNotificationsRequest(user, singletonList(notification), encrypted);
        List<ListenableFuture<NotificationGateway.FutureNotifications>> futures = gateway.sendNotifications(request);
        Assert.assertEquals(0, futures.size());

        verify(notificationDao).save(Lists.<Notification>newArrayList(), NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL);

        verifyNoMoreInteractions(notificationDao);
    }

    @Test
    public void doNotSendButSaveNotificationsIfProhibitedByConfiguration()
            throws ExecutionException, InterruptedException {
        Notification notification = newNotification("title");
        notification.setType(Activity.Types.INCOME);

        User user = newUser(new NotificationSettings());
        when(notificationsConfiguration.shouldSendNotifications(user)).thenReturn(false);

        boolean encrypted = true;
        //noinspection ConstantConditions
        SendNotificationsRequest request = new SendNotificationsRequest(user, singletonList(notification), encrypted);
        List<ListenableFuture<NotificationGateway.FutureNotifications>> futures = gateway.sendNotifications(request);
        Assert.assertEquals(0, futures.size());

        verify(notificationDao).save(singletonList(notification), NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL);
    }

    Notification newNotification(String title) {
        return new Notification.Builder()
                .userId("userId")
                .date(new Date())
                .title(title)
                .url("url")
                .type("type")
                .build();
    }

    User newUser(NotificationSettings notificationSettings) {
        UserProfile profile = new UserProfile();
        profile.setNotificationSettings(notificationSettings);
        User user = new User();
        user.setProfile(profile);
        return user;
    }

}
