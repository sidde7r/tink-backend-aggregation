package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

public enum ConsentPermission {
    READ_ACCOUNTS_DETAIL("ReadAccountsDetail"),
    READ_BALANCES("ReadBalances"),
    READ_TRANSACTIONS_CREDITS("ReadTransactionsCredits"),
    READ_TRANSACTIONS_DEBITS("ReadTransactionsDebits"),
    READ_TRANSACTIONS_DETAIL("ReadTransactionsDetail"),
    READ_PARTY("ReadParty"),
    READ_PARTY_PSU("ReadPartyPSU"),
    READ_BENEFICIARIES_DETAIL("ReadBeneficiariesDetail"),
    READ_SCHEDULED_PAYMENTS_DETAIL("ReadScheduledPaymentsDetail");

    private final String value;

    ConsentPermission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
