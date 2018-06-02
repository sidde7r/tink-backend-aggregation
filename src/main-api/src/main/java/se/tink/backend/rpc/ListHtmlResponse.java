package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

public class ListHtmlResponse {
    @Tag(1)
    private String htmlPage;
    @Tag(2)
    private int nextPageOffset;
    @Tag(3)
    private List<String> activityKeys;
    @Tag(4)
    private List<String> feedActivityIdentifiersList;

    public String getHtmlPage() {
        return htmlPage;
    }

    public void setHtmlPage(String htmlPage) {
        this.htmlPage = htmlPage;
    }

    public int getNextPageOffset() {
        return nextPageOffset;
    }

    public void setNextPageOffset(int nextPageOffset) {
        this.nextPageOffset = nextPageOffset;
    }

    public List<String> getActivityKeys() {
        return activityKeys;
    }

    public void setActivityKeys(List<String> activityKeys) {
        this.activityKeys = activityKeys;
    }

    public List<String> getFeedActivityIdentifiersList() {
        return feedActivityIdentifiersList;
    }

    public void setFeedActivityIdentifiersList(List<String> feedActivityIdentifiersList) {
        this.feedActivityIdentifiersList = feedActivityIdentifiersList;
    }
}
