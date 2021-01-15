package se.tink.backend.aggregation.client.provider_configuration.rpc;

/**
 * Used on providers to indicate different tasks it can handle in terms of agents, since it's not
 * possible now in main to know if an agent implements an interface e.g. TransferExecutor.
 */
public enum Capability {
    TRANSFERS, // backwards compatibility, deprecated in favour of PAYMENTS
    EINVOICES,
    @Deprecated
    MORTGAGE_AGGREGATION, // backwards compatibility, deprecated in favour of LOANS
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    LOANS,
    INVESTMENTS,
    PAYMENTS, // backwards compatibility, deprecated in favour of granular PIS capabilities
    IDENTITY_DATA,
    LIST_BENEFICIARIES,
    CREATE_BENEFICIARIES,
    CREATE_BENEFICIARIES_IN_PAYMENT
}
