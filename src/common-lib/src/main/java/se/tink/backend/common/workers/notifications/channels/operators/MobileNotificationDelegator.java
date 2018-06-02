package se.tink.backend.common.workers.notifications.channels.operators;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import java.util.List;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationChannel;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class MobileNotificationDelegator implements MobileNotificationOperator {
    private static final LogUtils log = new LogUtils(MobileNotificationDelegator.class);
    private final ImmutableList<MobileNotificationChannel> channels;
    private final MobileNotificationOperator nextOperator;

    public MobileNotificationDelegator(MobileNotificationOperator nextOperator, List<MobileNotificationChannel> channels) {
        this.channels = ImmutableList.copyOf(channels);
        this.nextOperator = Preconditions.checkNotNull(nextOperator);
    }

    @Override
    public void process(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        ImmutableListMultimap<MobileNotificationChannel, Device> notificationsByChannel = devicesByChannel(user,
                devices);

        boolean sent = false;
        for (MobileNotificationChannel channel : notificationsByChannel.keySet()) {
            ImmutableList<Device> channelDevices = notificationsByChannel.get(channel);

            if (channelDevices.size() > 0) {
                sent |= channel.send(notification, channelDevices, user, encrypted, unreadNotifications);
            }
        }

        if (sent) {
            nextOperator.process(notification, devices, user, encrypted, unreadNotifications);
        }
    }

    private ImmutableListMultimap<MobileNotificationChannel, Device> devicesByChannel(User user, List<Device> devices) {

        ImmutableListMultimap.Builder<MobileNotificationChannel, Device> notificationsByChannelBuilder = ImmutableListMultimap
                .builder();
        for (Device device : devices) {
            boolean foundAtLeastOneMatch = false;
            for (MobileNotificationChannel channel : channels) {
                if (channel.getPredicate().apply(device)) {
                    notificationsByChannelBuilder.put(channel, device);
                    foundAtLeastOneMatch = true;
                }
            }
            if (!foundAtLeastOneMatch) {
                log.warn(user.getId(), String.format("Unmatched push device: %s", device));
            }
        }

        return notificationsByChannelBuilder.build();
    }
}
