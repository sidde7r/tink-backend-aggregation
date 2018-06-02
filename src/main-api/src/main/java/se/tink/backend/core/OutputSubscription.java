package se.tink.backend.core;

/**
 * An instance of an {@link SubscriptionType} that can be localized.
 */
public class OutputSubscription {
    private String id;
    private String description;
    private String parentId;
    private Boolean subscribed;
    private boolean invertedSelection;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }
    
    public Boolean isSubscribed() {
        return subscribed;
    }

    public void setInvertedSelection(boolean invertedSelection) {
        this.invertedSelection = invertedSelection;
    }
    
    public boolean getInvertedSelection() {
        return invertedSelection;
    }

}
