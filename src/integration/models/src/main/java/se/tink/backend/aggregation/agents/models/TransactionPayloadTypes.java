package se.tink.backend.aggregation.agents.models;

/**
 * Type of information that can be added to a transaction if there is information available from the
 * agent.
 */
public enum TransactionPayloadTypes {
    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.GIRO} */
    GIRO,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_TWIN} */
    TRANSFER_TWIN,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_ACCOUNT} */
    TRANSFER_ACCOUNT,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL}
     */
    TRANSFER_ACCOUNT_EXTERNAL,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL}
     */
    TRANSFER_ACCOUNT_NAME_EXTERNAL,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.EXTERNAL_ID} */
    EXTERNAL_ID,

    /**
     * {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.VERIFICATION_NUMBER}
     */
    @Deprecated
    VERIFICATION_NUMBER,

    /**
     * {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.CHARGEBACK_OR_RETURN}
     */
    CHARGEBACK_OR_RETURN,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.UNSETTLED_AMOUNT} */
    UNSETTLED_AMOUNT,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.DOCUMENT} */
    DOCUMENT,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.PAYMENT_GATEWAY} */
    PAYMENT_GATEWAY,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.AGENT_CATEGORY} */
    @Deprecated
    AGENT_CATEGORY,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.AGENT_ORIGINAL_CATEGORY}
     */
    @Deprecated
    AGENT_ORIGINAL_CATEGORY,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.INTUIT_CATEGORY} */
    INTUIT_CATEGORY,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.MCC} */
    MCC,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.YODLEE_CATEGORY} */
    @Deprecated
    YODLEE_CATEGORY,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.AMEX_CATEGORY} */
    AMEX_CATEGORY,

    /**
     * {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.GOOGLE_PLACES_TYPES}
     */
    GOOGLE_PLACES_TYPES,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.SUB_ACCOUNT} */
    SUB_ACCOUNT,

    /**
     * {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.TRANSFER_PROVIDER}
     */
    TRANSFER_PROVIDER,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.FRAUD_STATUS} */
    FRAUD_STATUS,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.MESSAGE} */
    MESSAGE,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.DETAILS} */
    DETAILS,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID}
     */
    EDITABLE_TRANSACTION_TRANSFER_ID,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER}
     */
    EDITABLE_TRANSACTION_TRANSFER,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.EXCHANGE_RATE} */
    EXCHANGE_RATE,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.LOCAL_CURRENCY} */
    LOCAL_CURRENCY,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY}
     */
    AMOUNT_IN_LOCAL_CURRENCY,

    /** {@see se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.LOCAL_REGION} */
    LOCAL_REGION,

    /**
     * {@see
     * se.tink.backend.aggregation.agents.models.TransactionPayloadTypes.LINK_COUNTERPART_PROMPT_ANSWE}
     */
    LINK_COUNTERPART_PROMPT_ANSWER,

    /** {@see se.tink.backend.core.TransactionPayloadTypes.CREDIT_CARD_NUMBER} */
    CREDIT_CARD_NUMBER,

    /** {@see se.tink.backend.core.TransactionPayloadTypes.CREDIT_CARD_HOLDER} */
    CREDIT_CARD_HOLDER,

    /** {@see se.tink.backend.core.TransactionPayloadTypes.CREDIT_CARD_HOLDER} */
    FIELD_MAPPER_MIGRATIONS
}
