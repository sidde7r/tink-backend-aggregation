package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.Activity;

public class ActivityQueryResponse {
    @Tag(1)
    @ApiModelProperty(name = "activities", value = "The filtered list of activities matching the query", required = true)
    private List<Activity> activities;
    @Tag(2)
    @ApiModelProperty(name = "count", value = "The total number of activities matching the query", required = true, example = "134")
    private int count;

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
