package se.tink.backend.rpc;

import java.util.List;
import se.tink.backend.core.Activity;

public class ListActivityHtmlCommand {
    private List<Activity> activityList;
    private String userAgent;
    private int offset;
    private int limit;
    private int screenWidthl;
    private int screenPpi;

    public List<Activity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<Activity> activityList) {
        this.activityList = activityList;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    public int getScreenWidthl() {
        return screenWidthl;
    }

    public void setScreenWidthl(int screenWidthl) {
        this.screenWidthl = screenWidthl;
    }

    public int getScreenPpi() {
        return screenPpi;
    }

    public void setScreenPpi(int screenPpi) {
        this.screenPpi = screenPpi;
    }
}
