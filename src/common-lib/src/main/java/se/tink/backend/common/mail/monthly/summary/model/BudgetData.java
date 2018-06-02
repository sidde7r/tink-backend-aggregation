package se.tink.backend.common.mail.monthly.summary.model;

import se.tink.backend.core.follow.FollowTypes;

public class BudgetData
{
    private String name;
    private String icon;
    private int percentCompleted;
    private FollowTypes type;

    public BudgetData(String name, String icon, int percentCompleted, FollowTypes type) {
        this.name = name;
        this.icon = icon;
        this.percentCompleted = percentCompleted;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackgroundIcon() {
        return String.format("budget-%s.png", percentCompleted);
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }
}
