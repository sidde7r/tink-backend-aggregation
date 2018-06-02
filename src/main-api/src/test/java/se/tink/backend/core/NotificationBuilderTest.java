package se.tink.backend.core;

import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NotificationBuilderTest {
    @Test
    public void build() {
        Notification notification = new Notification("userId");
        notification.setKey("key");
        Date date = new Date();
        notification.setDate(date);
        Date generated = new Date();
        notification.setGenerated(generated);
        notification.setTitle("title");
        notification.setMessage("message");
        notification.setUrl("url");
        notification.setType("type");
        notification.setGroupable(true);
        notification.setSensitiveTitle("sensitive-title");
        notification.setSensitiveMessage("sensitive-message");

        assertEquals(
                notification,
                new Notification.Builder()
                        .userId("userId")
                        .key("key")
                        .date(date)
                        .generated(generated)
                        .title("title")
                        .message("message")
                        .url("url")
                        .type("type")
                        .groupable(true)
                        .sensitiveTitle("sensitive-title")
                        .sensitiveMessage("sensitive-message")
                        .build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildWithoutRequiredFields() {
        new Notification.Builder().build();
    }

}
