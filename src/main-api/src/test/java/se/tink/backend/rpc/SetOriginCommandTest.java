package se.tink.backend.rpc;

import org.junit.Test;
import se.tink.backend.core.DeviceOrigin;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SetOriginCommandTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullDeviceId(){
        SetOriginCommand command = new SetOriginCommand(null, new DeviceOrigin());
    }

    @Test
    public void testDeviceId(){
        UUID deviceId = UUID.randomUUID();
        SetOriginCommand command = new SetOriginCommand(deviceId.toString(), new DeviceOrigin());
        assertThat(Objects.equals(command.getDeviceId(), deviceId));
    }

    @Test
    public void testDeviceIdWithDashes() {
        String deviceIdStr = "820c72bd-1cae-497a-a659-5f7ba286ac0e";
        UUID deviceId = UUID.fromString("820c72bd-1cae-497a-a659-5f7ba286ac0e");
        SetOriginCommand command = new SetOriginCommand(deviceIdStr, new DeviceOrigin());
        assertThat(Objects.equals(command.getDeviceId(), deviceId));
    }

}
