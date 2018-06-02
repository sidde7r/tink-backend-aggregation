package se.tink.backend.abnamro.workers.activity.renderers.models;

import se.tink.backend.common.workers.activity.renderers.models.Icon;

public class TransactionSummaryData {
    
    private String amount;
    private String deeplink;
    private String description;
    private Icon icon;
    private String title;
    
    public String getAmount() {
        return amount;
    }
    
    public String getDeeplink() {
        return deeplink;
    }
    
    public String getDeeplinkMethodName() {
        return deeplink.replaceAll(":|/|\\?|\\=|\\s|\\-|\\&", "");
    }
    
    public String getDescription() {
        return description;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
