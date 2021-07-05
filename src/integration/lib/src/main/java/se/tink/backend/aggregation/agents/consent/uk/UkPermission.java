package se.tink.backend.aggregation.agents.consent.uk;

import se.tink.backend.aggregation.agents.consent.Permission;

public enum UkPermission implements Permission {
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

    UkPermission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
