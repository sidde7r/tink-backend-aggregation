package se.tink.analytics.entites;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CategorizationTransaction implements Serializable {

    private final String userId;
    private final String description;
    private final String categoryCode;
    private final String market;
    
    public CategorizationTransaction(String description, String categoryCode, String market, String userId) {

        this.description = description;
        this.categoryCode = (categoryCode != null) ? categoryCode.toLowerCase() : null;
        this.market = (market != null) ? market.toLowerCase() : null;
        this.userId = userId;
    }
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public String getCategoryType() {
        return categoryCode.substring(0, categoryCode.indexOf(':'));
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getMarket() {
        return market;
    }
    
    public String getUserId() {
        return userId;
    }
}
