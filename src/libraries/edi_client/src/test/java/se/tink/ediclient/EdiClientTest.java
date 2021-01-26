package se.tink.ediclient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class EdiClientTest {
    @Test
    public void certificateAboutToExpireTest() {

        ZonedDateTime now = ZonedDateTime.of(2020, 12, 1, 12, 0, 0, 0, ZoneId.systemDefault());

        Assert.assertFalse(
                EdiClient.certificateAboutToExpire(Date.from(now.plusHours(5).toInstant()), now));
        Assert.assertTrue(
                EdiClient.certificateAboutToExpire(Date.from(now.plusHours(3).toInstant()), now));
        Assert.assertTrue(
                EdiClient.certificateAboutToExpire(Date.from(now.minusHours(3).toInstant()), now));
        Assert.assertTrue(
                EdiClient.certificateAboutToExpire(Date.from(now.minusHours(5).toInstant()), now));
    }
}
