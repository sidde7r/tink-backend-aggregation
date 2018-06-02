package se.tink.backend.rpc;

import io.protostuff.Tag;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

import se.tink.backend.core.follow.FollowItem;

public class FollowItemListResponse {
    @Tag(1)
    @ApiModelProperty(name = "followItems", value="A list of follow items.")
    private List<FollowItem> followItems;

    public List<FollowItem> getFollowItems() {
        return followItems;
    }

    public void setFollowItems(List<FollowItem> followItems) {
        this.followItems = followItems;
    }
}
