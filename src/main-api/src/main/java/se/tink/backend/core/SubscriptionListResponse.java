package se.tink.backend.core;

import java.util.List;

public class SubscriptionListResponse {

    private List<OutputSubscription> subscriptions;
    private String title;
    private String subtitle;
    private String settingsSaved;
    private String saveButton;
    private String additionalSaveInfo;

    public List<OutputSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<OutputSubscription> types) {
        this.subscriptions = types;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getSubtitle() {
        return subtitle;
    }

    public void setSettingsSaved(String settingsSaved) {
        this.settingsSaved = settingsSaved;
    }

    public void setSaveButton(String saveButton) {
        this.saveButton = saveButton;
    }
    
    public String getSaveButton() {
        return saveButton;
    }
    
    public String getSettingsSaved() {
        return settingsSaved;
    }

    public void setAdditionalSaveInfo(String additionalSaveInfo) {
        this.additionalSaveInfo = additionalSaveInfo;
    }
    
    public String getAdditionalSaveInfo() {
        return additionalSaveInfo;
    }
}
