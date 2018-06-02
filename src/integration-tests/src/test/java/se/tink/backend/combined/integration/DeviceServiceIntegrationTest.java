package se.tink.backend.combined.integration;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.api.DeviceService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.core.Device;
import se.tink.backend.rpc.DeviceListResponse;

/**
 * TODO this is a unit test
 */
public class DeviceServiceIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testCreateDevice() throws Exception {
        AuthenticatedUser authenticatedUser = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceService deviceService = serviceFactory.getDeviceService();

        DeviceListResponse devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 0);

        String deviceToken = UUID.randomUUID().toString();

        Device device = new Device();
        device.setAppId("se.tink.mobile.google");
        device.setNotificationToken("LRJ2bFzHA1jUIkwayDqxteNsWY3udejkEe9UwRMt12E_R5i");
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)");

        deviceService.updateDevice(authenticatedUser, deviceToken, device);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 1);

        deleteUser(authenticatedUser.getUser());
    }

    @Test
    public void testDeleteDevice() throws Exception {
        AuthenticatedUser authenticatedUser = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceService deviceService = serviceFactory.getDeviceService();

        DeviceListResponse devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 0);

        String deviceToken = UUID.randomUUID().toString();

        Device device = new Device();
        device.setAppId("se.tink.mobile.google");
        device.setNotificationToken("LRJ2bFzHA1jUIkwayDqxteNsWY3udejkEe9UwRMt12E_R5i");
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)");

        deviceService.updateDevice(authenticatedUser, deviceToken, device);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 1);

        deviceService.deleteDevice(authenticatedUser, deviceToken);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 0);

        deleteUser(authenticatedUser.getUser());
    }

    @Test
    public void testTakeOverDevice() throws Exception {
        DeviceService deviceService = serviceFactory.getDeviceService();

        String deviceToken = UUID.randomUUID().toString();

        Device device = new Device();
        device.setAppId("se.tink.mobile.google");
        device.setNotificationToken("LRJ2bFzHA1jUIkwayDqxteNsWY3udejkEe9UwRMt12E_R5i");
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)");

        // Register the device with user 1

        AuthenticatedUser authenticatedUser1 = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceListResponse devices1 = deviceService.listDevices(authenticatedUser1);

        deviceService.updateDevice(authenticatedUser1, deviceToken, device);

        devices1 = deviceService.listDevices(authenticatedUser1);
        Assert.assertTrue(devices1.getDevices().size() == 1);

        // Register the device with user 2

        AuthenticatedUser authenticatedUser2 = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceListResponse devices2 = deviceService.listDevices(authenticatedUser2);

        deviceService.updateDevice(authenticatedUser2, deviceToken, device);

        devices2 = deviceService.listDevices(authenticatedUser2);
        Assert.assertTrue(devices2.getDevices().size() == 1);

        // Assert the takeover

        devices1 = deviceService.listDevices(authenticatedUser1);
        Assert.assertTrue(devices1.getDevices().size() == 0);

        // Cleanup

        deleteUser(authenticatedUser1.getUser());
        deleteUser(authenticatedUser2.getUser());
    }

    @Test
    public void testUpdateDevice() throws Exception {
        AuthenticatedUser authenticatedUser = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceService deviceService = serviceFactory.getDeviceService();

        DeviceListResponse devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 0);

        String deviceToken = UUID.randomUUID().toString();

        Device device = new Device();
        device.setAppId("se.tink.mobile.google-1");
        device.setNotificationToken("first-notification-token");
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)");

        deviceService.updateDevice(authenticatedUser, deviceToken, device);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 1);

        device = new Device();
        device.setAppId("se.tink.mobile.google-2");
        device.setNotificationToken("second-notification-token");
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.8.0 (Android; 4.4.2, LGE Nexus 4)");
        deviceService.updateDevice(authenticatedUser, deviceToken, device);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 1);

        Device updatedDevice = devices.getDevices().get(0);
        Assert.assertTrue(updatedDevice.getUpdated() != null);

        deleteUser(authenticatedUser.getUser());
    }


    @Test
    public void testMigratingDevice() throws Exception {
        AuthenticatedUser authenticatedUser = authenticated(registerTestUserWithDemoCredentialsAndData());

        DeviceService deviceService = serviceFactory.getDeviceService();

        DeviceListResponse devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 0);

        // Register the device using the hash of the notification token as a way to simulate
        // the migration of old push tokens to new device structure.

        String notificationToken = "1159dc01-f506-486b-ad87-7dc6dd12c72f";

        Device device = new Device();
        device.setAppId("se.tink.mobile.google-1");
        device.setNotificationToken(notificationToken);
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.7.8 (Android; 4.4.2, LGE Nexus 4)");

        deviceService.updateDevice(authenticatedUser, notificationToken, device);

        // Register the device again, with a real device ID.

        String deviceToken = "4e53d139-e626-40b6-ab2b-7c8807208f25";

        device = new Device();
        device.setAppId("se.tink.mobile.google-2");
        device.setNotificationToken(notificationToken);
        device.setType("android");
        device.setUserAgent("Tink Mobile/1.8.0 (Android; 4.4.2, LGE Nexus 4)");
        deviceService.updateDevice(authenticatedUser, deviceToken, device);

        devices = deviceService.listDevices(authenticatedUser);
        Assert.assertTrue(devices.getDevices().size() == 1);

        Device updatedDevice = devices.getDevices().get(0);
        Assert.assertTrue(updatedDevice.getUpdated() != null);

        deleteUser(authenticatedUser.getUser());
    }
}
