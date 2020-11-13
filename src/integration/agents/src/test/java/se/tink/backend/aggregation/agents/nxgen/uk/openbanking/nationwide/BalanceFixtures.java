package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

class BalanceFixtures {

    private static final String BALANCE_CLOSING_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ClosingAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_PREVIOUSLY_CLOSED_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"PreviouslyClosedBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_FORWARD_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ForwardAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    static AccountBalanceEntity closingAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_CLOSING_AVAILABLE, AccountBalanceEntity.class);
    }

    static AccountBalanceEntity previouslyClosedBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_PREVIOUSLY_CLOSED_BOOKED, AccountBalanceEntity.class);
    }

    static AccountBalanceEntity forwardAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_FORWARD_AVAILABLE, AccountBalanceEntity.class);
    }
}
