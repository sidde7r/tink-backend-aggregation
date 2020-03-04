package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BalanceFixtures {

    private static final String BALANCE_CREDIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_DEBIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.051234\",\"Currency\":\"EUR\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    private static final String BALANCE_INTERIM_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_CLOSING_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_PREVIOUSLY_CLOSED_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"PreviouslyClosedBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    public static AccountBalanceEntity balanceDebit() {
        return SerializationUtils.deserializeFromString(BALANCE_DEBIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceCredit() {
        return SerializationUtils.deserializeFromString(BALANCE_CREDIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity interimAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_INTERIM_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity closingBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_CLOSING_BOOKED, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity previouslyClosedBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_PREVIOUSLY_CLOSED_BOOKED, AccountBalanceEntity.class);
    }
}
