package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import se.tink.backend.core.NotificationStatus;

public class NotificationQuery {
    @ApiModelProperty(name = "offset", value = "The number of notifications to skip (when paging).", required = false, example = "0")
    private int offset;
    @ApiModelProperty(name = "limit", value = "The maximum number of notifications to return (when paging, 0 indicates no limit).", required = false, example = "10")
    private int limit;
    @ApiModelProperty(name = "statuses", value = "The set of notification statuses to be used as a query filter", allowableValues = "CREATED, SENT, RECEIVED, READ", required = false, example = "[\"READ\", \"SENT\"]")
    private Set<NotificationStatus> statuses;

    public Set<NotificationStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<NotificationStatus> statuses) {
        this.statuses = statuses;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }


}
