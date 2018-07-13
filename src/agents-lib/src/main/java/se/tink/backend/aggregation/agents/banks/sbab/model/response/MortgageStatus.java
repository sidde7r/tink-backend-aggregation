package se.tink.backend.aggregation.agents.banks.sbab.model.response;

/**
 * Statuses of a mortgage application to SBAB.
 */
public enum MortgageStatus {

    ANSOKAN_REGISTRERAD,
    AVSLAGEN,
    AVSLAGEN_UC,
    BEARBETNING_PAGAR,
    UTBETALT,
    MAKULERAD,
    TEKNISKT_FEL,
    KOMPLETTERING_KRAVS,
    LANEHANDLINGAR_KLARA,
    LANEHANDLINGAR_INKOMNA
}
