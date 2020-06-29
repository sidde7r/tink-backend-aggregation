package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.natwest;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BalanceFixtures {

    private static final String BALANCE_EXPECTED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"Expected\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_PREVIOUSLY_CLOSED_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"PreviouslyClosedBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_FORWARD_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ForwardAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    static AccountBalanceEntity expectedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_EXPECTED, AccountBalanceEntity.class);
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
