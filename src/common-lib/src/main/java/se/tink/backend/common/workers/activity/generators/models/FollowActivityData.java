package se.tink.backend.common.workers.activity.generators.models;

import se.tink.backend.core.follow.FollowItem;

public class FollowActivityData {
    private FollowItem followItem;

    public FollowItem getFollowItem() {
        return followItem;
    }

    public void setFollowItem(FollowItem followItem) {
        this.followItem = followItem;
    }
}
