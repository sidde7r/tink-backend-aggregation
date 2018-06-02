package se.tink.backend.common.workers.activity.generators.models;

import java.util.List;

import se.tink.backend.core.follow.FollowItem;

public class FollowActivityFeedbackData {
    private String description;
    private List<FollowItem> followItems;
    private String title;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public List<FollowItem> getFollowItems() {
        return followItems;
    }

    public void setFeedbackTitle(String feedbackTitle) {
        this.title = feedbackTitle;
    }

    public void setFollowItems(List<FollowItem> followItems) {
        this.followItems = followItems;
    }
}
