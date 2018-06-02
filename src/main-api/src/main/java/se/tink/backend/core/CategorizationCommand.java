package se.tink.backend.core;
/**
 * Transaction categorization (defined by probability vectors) can be performed by the commands defined in this enum 
 */
public enum CategorizationCommand {
    /**
     * Global rules (pattern matching)
     */
    GLOBAL_RULES,
    
    /**
     * American Express
     */
    AMEX,
    
    /**
     * Merchant Category Code
     */
    MCC,
    
    /**
     * Google Places
     */
    GOOGLE_PLACES,
    
    /**
     * User learning
     */
    USER_LEARNING,
    
    /**
     * General model for expenses
     */
    GENERAL_EXPENSES,
    
    /**
     * General model for income
     */
    GENERAL_INCOME,
    
    /**
     * Random uncategorization
     */
    RANDOM_UNCATEGORIZATION,
    
    /**
     * Special rules for ABN AMRO
     */
    ABN_AMRO,

    /**
     * 1-to-1 map between merchants and categories
     */
    MERCHANT_MAP,
}
