package se.tink.libraries.uuid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class UUIDUtilsTest {
    @Test
    public void testFromTinkUUID() {
        String string = UUIDUtils.generateUUID();
        UUID uuid = UUIDUtils.fromTinkUUID(string);

        Assert.assertEquals(string, uuid.toString().replace("-", ""));
    }

    @Test
    public void testToTinkUUID() {
        UUID uuid = UUID.randomUUID();
        String string = UUIDUtils.toTinkUUID(uuid);

        Assert.assertEquals(uuid.toString().replace("-", ""), string);
    }

    @Test
    public void testFromStringWhenNull() {
        assertThat(UUIDUtils.fromString(null)).isEqualTo(null);
    }

    @Test
    public void testFromStringWhenEmpty() {
        assertThat(UUIDUtils.fromString("")).isEqualTo(null);
    }

    @Test
    public void testFromStringTinkUUID() {
        UUID uuid = UUID.fromString("3ab85394-a847-48da-aa50-eb38488cc1d3");

        assertThat(UUIDUtils.fromString("3ab85394a84748daaa50eb38488cc1d3")).isEqualTo(uuid);
    }

    @Test
    public void testFromStringUUIDv4() {
        UUID uuid = UUID.fromString("3ab85394-a847-48da-aa50-eb38488cc1d3");

        assertThat(UUIDUtils.fromString("3ab85394-a847-48da-aa50-eb38488cc1d3")).isEqualTo(uuid);
    }
}
