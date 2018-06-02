package se.tink.backend.core;

/**
 * Types that can be opted out from.
 * 
 * @note Removing/renaming one of these, you should probably clean up the database, too.
 * @note For every added subscription type, a translation must be added in
 *       SubscriptionServiceResource#getLocalizedTypeDescription.
 */
public enum SubscriptionType {
    /**
     * Special subscription group that applies to all.
     */
    ROOT(null, true, true),

    MONTHLY_SUMMARY_EMAIL(SubscriptionType.ROOT, true),
    PRODUCT_UPDATES(SubscriptionType.ROOT, true),
    FAILING_CREDENTIALS_EMAIL(SubscriptionType.ROOT, true);
    

    private SubscriptionType parent;
    private boolean subscribedByDefault;
    private boolean invertedSelection = false;

    SubscriptionType(SubscriptionType parent, boolean subscribedByDefault) {
        this(parent, subscribedByDefault, false);
    }
    
    SubscriptionType(SubscriptionType parent, boolean subscribedByDefault, boolean invertedSelection) {
        this.parent = parent;
        this.subscribedByDefault = subscribedByDefault;
        this.invertedSelection = invertedSelection;
    }

    public SubscriptionType getParent() {
        return parent;
    }

    public Boolean isSubscribedByDefault() {
        return subscribedByDefault;
    }
    
    public boolean hasAncestor(SubscriptionType possibleParent) {
        int count = 0;
        
        SubscriptionType s = this;
        while (s.getParent() != null) {
            if (s.getParent().equals(possibleParent)) {
                return true;
            }
            s = s.getParent();
            
            count++;
            if (count > 1000) {
                // Avoid infinite loop.
                throw new RuntimeException("Possible recursive reference.");
            }
        }
        return false;
    }
    
    public boolean getInvertedSelection() {
        return invertedSelection;
    }

}
