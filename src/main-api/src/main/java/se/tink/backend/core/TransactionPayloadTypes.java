package se.tink.backend.core;

/**
 * Type of information that can be added to a transaction if there is
 * information available from the agent.
 */
public enum TransactionPayloadTypes {
    /**
     * AG - this is an autogiro transaction Recipient of postgiro and bankgiro
     * payment
     */
    GIRO,

    /**
     * Corresponding transactions that constitutes this transfer
     */
    TRANSFER_TWIN,

    /**
     * ID of the account to where the transfer was made, set by @TransferDetectionCommand
     */
    TRANSFER_ACCOUNT,

    /**
     * Account number of the account to or from where the transfer was made, set by agent.
     */
    TRANSFER_ACCOUNT_EXTERNAL,
    
    /**
     * Account name of the account to or from where the transfer was made, set by agent.
     */
    TRANSFER_ACCOUNT_NAME_EXTERNAL,

    /**
     * Generic external ID fetched from the provider.
     */
    EXTERNAL_ID,

    /**
     * Generic verification number fetched from the provider.
     */
    @Deprecated
    VERIFICATION_NUMBER,

    /**
     * Boolean flag if the transactions is a chargeback or return transaction.
     */
    CHARGEBACK_OR_RETURN,

    /**
     * Amount before the transaction was settled
     */
    UNSETTLED_AMOUNT,

    /**
     * The document id of any linked documents (the primary one).
     */
    DOCUMENT,

    /**
     * The payment gateway that was used (Paypal, Swish, iZettle, Bankgiro,
     * Plusgiro, Autogiro, etc.)
     */
    PAYMENT_GATEWAY,

    /**
     * Mapping an external category to a Tink category should be done
     * as a step in the categorization command chain instead.
     *
     * @deprecated
     */
    @Deprecated
    AGENT_CATEGORY,
    
    /**
     * Replaced with agent specific parameters.
     *
     * @deprecated use {@link YODLEE_CATEGORY} or {@link INTUIT_CATEGORY} instead.  
     */
    @Deprecated
    AGENT_ORIGINAL_CATEGORY,
    
    /**
     * The Intuit category of the transaction.
     */
    INTUIT_CATEGORY,
    
    /**
     * The MCC (Merchant Category Code) of the credit-card transaction.
     */
    MCC,
    
    /**
     * The Yodlee category of the transaction.
     */
    @Deprecated
    YODLEE_CATEGORY,
    
    /**
     * The Amex category of the (credit-card) transaction as we get it for (some) Amex transactions.
     */
    AMEX_CATEGORY,

    /**
     * Google Places types
     * @see https://developers.google.com/places/documentation/supported_types
     */
    GOOGLE_PLACES_TYPES,

    /**
     * The sub account (e.g. the credit card) the transaction belongs to.
     */
    SUB_ACCOUNT,
    
    /**
     * Name of the provider that the transaction _probably_ has as counter part.
     * If @TRANSFER_ACCOUNT is set, the provider is only used for evaluation.
     */
    TRANSFER_PROVIDER,
    
    /**
     * Fraud status @TransactionFraudStatus of this transaction, marked by the user. 
     */
    FRAUD_STATUS,
    
    /**
     * Transactions--especially transfers--might contain an additional message supplementing the description. 
     */
    MESSAGE,

    /**
     * Transaction details, sometimes transactions contains receipt information and the like not suitable for description field.
     */
    DETAILS,

    /**
     * This transactions refers to a payment that can be changed, the id of the payment (TRANSFER) is this payload.
     */
    EDITABLE_TRANSACTION_TRANSFER_ID,

    /**
     * This transactions refers to a payment that can be changed, the transfer object is this payload.
     */
    EDITABLE_TRANSACTION_TRANSFER,

    /**
     * The exchange rate between local and target currencies in the transaction. Values less than one means converting from a less
     * valuable to a more valuable currency. And vice-versa.
     */
    EXCHANGE_RATE,

    /**
     * Local currency the transaction amount was originally made in before being converted to another currency.
     * Currencies are in the 3-letter (SEK, EUR, etc.) ISO 4217 format: https://en.wikipedia.org/wiki/ISO_4217
     */
    LOCAL_CURRENCY,

    /**
     * The actual amount of the transaction in the local currency.
     */
    AMOUNT_IN_LOCAL_CURRENCY,

    /**
     * The geographical region where the transaction was initiated.
     */
    LOCAL_REGION,

    /**
     * If we suspect that a transaction has a counterpart (e.g. if it's a refund for an expense), we prompt the user,
     * asking if that's the case or not. The answer (YES/NO) is persisted in the payload through a custom endpoint.
     */
    LINK_COUNTERPART_PROMPT_ANSWER
}
