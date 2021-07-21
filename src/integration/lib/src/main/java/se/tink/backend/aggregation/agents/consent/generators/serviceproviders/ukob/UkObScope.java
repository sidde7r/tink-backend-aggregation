package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum UkObScope implements Scope {
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

    UkObScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
