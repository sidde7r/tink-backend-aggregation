package se.tink.backend.system.rpc;


/**
 * Type of information that can be added to a transaction if there is
 * information available from the agent.
 */
public enum TransactionPayloadTypes {
    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.GIRO}
     */
    GIRO,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.TRANSFER_TWIN}
     */
    TRANSFER_TWIN,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.TRANSFER_ACCOUNT}
     */
    TRANSFER_ACCOUNT,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL}
     */
    TRANSFER_ACCOUNT_EXTERNAL,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL}
     */
    TRANSFER_ACCOUNT_NAME_EXTERNAL,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.EXTERNAL_ID}
     */
    EXTERNAL_ID,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.VERIFICATION_NUMBER}
     */
    @Deprecated
    VERIFICATION_NUMBER,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.CHARGEBACK_OR_RETURN}
     */
    CHARGEBACK_OR_RETURN,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.UNSETTLED_AMOUNT}
     */
    UNSETTLED_AMOUNT,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.DOCUMENT}
     */
    DOCUMENT,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.PAYMENT_GATEWAY}
     */
    PAYMENT_GATEWAY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.AGENT_CATEGORY}
     */
    @Deprecated
    AGENT_CATEGORY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.AGENT_ORIGINAL_CATEGORY}
     */
    @Deprecated
    AGENT_ORIGINAL_CATEGORY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.INTUIT_CATEGORY}
     */
    INTUIT_CATEGORY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.MCC}
     */
    MCC,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.YODLEE_CATEGORY}
     */
    @Deprecated
    YODLEE_CATEGORY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.AMEX_CATEGORY}
     */
    AMEX_CATEGORY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.GOOGLE_PLACES_TYPES}
     */
    GOOGLE_PLACES_TYPES,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.SUB_ACCOUNT}
     */
    SUB_ACCOUNT,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.TRANSFER_PROVIDER}
     */
    TRANSFER_PROVIDER,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.FRAUD_STATUS}
     */
    FRAUD_STATUS,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.MESSAGE}
     */
    MESSAGE,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.DETAILS}
     */
    DETAILS,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID}
     */
    EDITABLE_TRANSACTION_TRANSFER_ID,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER}
     */
    EDITABLE_TRANSACTION_TRANSFER,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.EXCHANGE_RATE}
     */
    EXCHANGE_RATE,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.LOCAL_CURRENCY}
     */
    LOCAL_CURRENCY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY}
     */
    AMOUNT_IN_LOCAL_CURRENCY,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.LOCAL_REGION}
     */
    LOCAL_REGION,

    /**
     * {@see se.tink.backend.core.TransactionPayloadTypes.LINK_COUNTERPART_PROMPT_ANSWE}
     */
    LINK_COUNTERPART_PROMPT_ANSWER
}
