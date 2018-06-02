package se.tink.backend.common.workers.notifications.channels;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Device;
import se.tink.backend.core.Event;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.DateUtils;
import static java.util.Collections.unmodifiableList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MobileNotificationSenderImplTest {
    private static final int NOTIFICATIONS_MAX_DAY_AGES = 2;

    private MobileNotificationSenderImpl mobileNotificationSender;
    private ServiceContext context;
    private DeviceRepository deviceRepository;
    private User user;
    private Counter meter;
    private List<Event> trackedEvents;
    private TestMobileNotificationChannel testChannel;
    private Date today;
    private NotificationsConfiguration notificationsConfiguration;
    private MetricRegistry metricRegistry;
    private Cluster cluster = Cluster.TINK;

    @Before
    public void setUp() {
        user = TestUtils.createUser("Johnny Depp");
        meter = new Counter();
        trackedEvents = Lists.newArrayList();
        today = new Date();

        mockData();

        mobileNotificationSender = spy(
                new MobileNotificationSenderImpl(() -> context, notificationsConfiguration, deviceRepository,
                        metricRegistry, cluster,
                        new DeepLinkBuilderFactory(context.getConfiguration().getNotifications().getDeepLinkPrefix())));
    }

    private void mockData() {
        context = mock(ServiceContext.class);
        deviceRepository = mock(DeviceRepository.class);
        ServiceConfiguration serviceConfiguration = mock(ServiceConfiguration.class);
        notificationsConfiguration = mock(NotificationsConfiguration.class);
        metricRegistry = mock(MetricRegistry.class);
        ListenableThreadPoolExecutor<Runnable> trackingExecutorService = (ListenableThreadPoolExecutor<Runnable>) mock(
                ListenableThreadPoolExecutor.class);
        final EventRepository eventRepository = mock(EventRepository.class);

        when(context.getRepository(DeviceRepository.class)).thenReturn(deviceRepository);
        when(deviceRepository.findByUserId(anyString())).thenReturn(Lists.newArrayList(createTestDevice()));

        when(context.getConfiguration()).thenReturn(serviceConfiguration);
        when(serviceConfiguration.getNotifications()).thenReturn(notificationsConfiguration);
        when(notificationsConfiguration.getMaxAgeDays()).thenReturn(NOTIFICATIONS_MAX_DAY_AGES);
        when(notificationsConfiguration.shouldGroupNotifications()).thenReturn(true);

        when(metricRegistry.meter(any(MetricId.class))).thenReturn(meter);

        when(context.getTrackingExecutorService()).thenReturn(trackingExecutorService);
        doAnswer(invocationOnMock -> {
            ((Runnable) invocationOnMock.getArguments()[0]).run();
            return null;
        }).when(trackingExecutorService).execute(any(Runnable.class));

        when(context.getRepository(EventRepository.class)).thenReturn(eventRepository);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            Event event = (Event) args[0];
            addEvent(event);
            return event;
        });
    }

    // Replace channels list into test's channel
    private void mockNotificationChain(final MobileNotificationChannel channel) {
        final List<MobileNotificationChannel> channels = Lists.newArrayList(channel);

        doAnswer(
                invocationOnMock -> {
                    doCallRealMethod().when(mobileNotificationSender)
                            .getNotificationChain(eq(context), eq(channels), anyBoolean());

                    return mobileNotificationSender.getNotificationChain(context, channels,
                            (Boolean) invocationOnMock.getArguments()[2]);
                }
        ).when(mobileNotificationSender)
                .getNotificationChain(eq(context), Mockito.<MobileNotificationChannel>anyList(), anyBoolean());
    }

    @Test
    public void sendNoMessageForOldNotification() throws Exception {
        Date weekAgo = DateUtils.addWeeks(today, -1);
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(weekAgo, false));
        }

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 0;
        int expMeterCount = 0;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void sendOneMessageForGroupableNotifications() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 1;
        int expMeterCount = 2;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void sendMultipleMessagesWhenGroupNotificationsIsFalse() throws Exception {
        when(notificationsConfiguration.shouldGroupNotifications()).thenReturn(false);

        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 10;
        int expMeterCount = 20;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void sendTwoMessagesForGroupableNotificationsAndOneUngroupable() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 2;
        int expMeterCount = 4;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void sendNoMessageForChannelPredicateReturnFalse() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(false, true);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 0;
        int expMeterCount = 2;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void sendNoMessageForChannelFailedToSend() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(true, false);
        mockNotificationChain(testChannel);

        mobileNotificationSender.sendNotifications(user, notifications, false, false);

        int expTrackedEventsCount = 0;
        int expChannelMsgCount = 2;
        int expMeterCount = 2;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
    }

    @Test
    public void trackSuccessfulSentMessages() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);
        boolean encrypted = false;

        mobileNotificationSender.sendNotifications(user, unmodifiableList(notifications), true, encrypted);

        int expTrackedEventsCount = 2;
        int expChannelMsgCount = 2;
        int expMeterCount = 4;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
        assertTrackedEvent(encrypted);
    }

    @Test
    public void trackSuccessfulSentEncryptedMessages() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(true, true);
        mockNotificationChain(testChannel);
        boolean encrypted = true;

        mobileNotificationSender.sendNotifications(user, notifications, true, encrypted);

        int expTrackedEventsCount = 2;
        int expChannelMsgCount = 2;
        int expMeterCount = 4;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
        assertTrackedEvent(encrypted);
    }

    @Test
    public void trackFailedSentMessagesForChannelPredicateReturnFalse() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(false, true);
        mockNotificationChain(testChannel);
        boolean encrypted = false;

        mobileNotificationSender.sendNotifications(user, notifications, true, encrypted);

        int expTrackedEventsCount = 2;
        int expChannelMsgCount = 0;
        int expMeterCount = 2;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
        assertTrackedEvent(encrypted);
    }

    @Test
    public void trackFailedSentEncryptedMessagesForChannelFailedToSend() throws Exception {
        int nNotifications = 10;

        List<Notification> notifications = Lists.newArrayListWithExpectedSize(nNotifications);

        for (int i = 0; i < nNotifications; i++) {
            notifications.add(createNotification(today, true));
        }
        notifications.add(createNotification(today, false));

        testChannel = new TestMobileNotificationChannel(true, false);
        mockNotificationChain(testChannel);
        boolean encrypted = true;

        mobileNotificationSender.sendNotifications(user, notifications, true, encrypted);

        int expTrackedEventsCount = 2;
        int expChannelMsgCount = 2;
        int expMeterCount = 2;

        assertCount(expTrackedEventsCount, expChannelMsgCount, expMeterCount);
        assertTrackedEvent(encrypted);
    }

    private void assertCount(int expTrackedEventsCount, int expChannelMsgCount, int expMeterCount) {
        Assert.assertEquals("Incorrect count of tracked events", expTrackedEventsCount, trackedEvents.size());
        Assert.assertEquals("Incorrect count of channel's call", expChannelMsgCount, testChannel.getCount());
        Assert.assertEquals("Incorrect count of meter's call", expMeterCount, meter.getValue(), 0.01);
    }

    private void assertTrackedEvent(boolean encrypted) {
        for (Event trackedEvent : trackedEvents) {
            String type = trackedEvent.getType();
            Assert.assertEquals("Event type generated incorrect. It should " + (encrypted ? "" : "not ")
                    + "contain `encrypted` text", type.contains("encrypted"), encrypted);
        }
    }

    private void addEvent(Event event) {
        trackedEvents.add(event);
    }

    private Device createTestDevice() {
        Device device = new Device();

        return device;
    }

    public static Notification createNotification(Date date, boolean isGroupable) {
        Notification notification = new Notification("userId");
        notification.setType("Type");
        notification.setKey(notification.getId());
        notification.setDate(date);
        notification.setGroupable(isGroupable);
        return notification;
    }

    public static class TestMobileNotificationChannel implements MobileNotificationChannel {
        private final Predicate<Device> predicate;
        private final boolean isSent;
        private int count = 0;

        public TestMobileNotificationChannel(boolean predicateValue, boolean isSent) {
            this.isSent = isSent;

            if (predicateValue) {
                predicate = Predicates.alwaysTrue();
            } else {
                predicate = Predicates.alwaysFalse();
            }
        }

        @Override
        public boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
                int unreadNotifications) {
            count++;
            return isSent;
        }

        @Override
        public Predicate<Device> getPredicate() {
            return predicate;
        }

        public int getCount() {
            return count;
        }
    }
}
