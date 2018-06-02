package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.Notification;

public class NotificationQueryResponse {
    @Tag(1)
    @ApiModelProperty(name = "notifications", value = "The filtered list of notifications", required = true)
    private List<Notification> notifications;
    @Tag(2)
    @ApiModelProperty(name = "count", value = "The total number of notifications", required = true, example = "45")
    private int count;

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
