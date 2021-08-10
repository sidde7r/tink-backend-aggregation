package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum UkObScope implements Scope {
    READ_ACCOUNTS_BASIC("ReadAccountsBasic"),
    READ_ACCOUNTS_DETAIL("ReadAccountsDetail"),
    READ_BALANCES("ReadBalances"),
    READ_BENEFICIARIES_DETAIL("ReadBeneficiariesDetail"),
    READ_DIRECT_DEBITS("ReadDirectDebits"),
    READ_OFFERS("ReadOffers"),
    READ_PARTY("ReadParty"),
    READ_PARTY_PSU("ReadPartyPSU"),
    READ_SCHEDULED_PAYMENTS_DETAIL("ReadScheduledPaymentsDetail"),
    READ_STANDING_ORDERS_DETAIL("ReadStandingOrdersDetail"),
    READ_STATEMENTS_BASIC("ReadStatementsBasic"),
    READ_STATEMENTS_DETAIL("ReadStatementsDetail"),
    READ_TRANSACTIONS_BASIC("ReadTransactionsBasic"),
    READ_TRANSACTIONS_CREDITS("ReadTransactionsCredits"),
    READ_TRANSACTIONS_DEBITS("ReadTransactionsDebits"),
    READ_TRANSACTIONS_DETAIL("ReadTransactionsDetail");

    private final String value;

    UkObScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
