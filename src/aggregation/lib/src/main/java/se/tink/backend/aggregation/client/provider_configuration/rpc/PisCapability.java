package se.tink.backend.aggregation.client.provider_configuration.rpc;

public enum PisCapability {
    PIS_SE_BG,
    PIS_SE_PG,
    PIS_SE_BANK_TRANSFERS,
    PIS_FUTURE_DATE,
    PIS_SEPA_RECURRING_PAYMENTS,
    PIS_SEPA_ICT_RECURRING_PAYMENTS,
    /**
     * SEPA Credit Transfer, aka SCT
     * https://www.europeanpaymentscouncil.eu/what-we-do/sepa-credit-transfer
     */
    SEPA_CREDIT_TRANSFER,
    /**
     * SEPA Instant Credit Transfer, aka SCT Inst
     * https://www.europeanpaymentscouncil.eu/what-we-do/sepa-instant-credit-transfer
     */
    SEPA_INSTANT_CREDIT_TRANSFER,
    /** Faster Payments Service in UK, aka FPS https://www.fasterpayments.org.uk/ */
    FASTER_PAYMENTS
}
