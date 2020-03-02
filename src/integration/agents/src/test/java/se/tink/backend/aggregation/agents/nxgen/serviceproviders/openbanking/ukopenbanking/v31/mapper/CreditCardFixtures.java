package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFixtures {

    private static final String BALANCE_INTERIM_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_CLOSING_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_PREVIOUSLY_CLOSED_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"456.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"PreviouslyClosedBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    private static final String CREDIT_CARD_ACCOUNT =
            "{\"Account\":[{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-IBAN\",\"SchemeName\":\"UK.OBIE.IBAN\"},{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-JELEN\",\"SchemeName\":\"UK.OBIE.PAN\"}],\"AccountId\":\"10000000000000691111\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CreditCard\"}";

    private static final String PAN_IDENTIFIER =
            "{ \"Identification\": \"************5004\", \"Name\": \"MR MYSZO-JELEN\", \"SchemeName\": \"UK.OBIE.PAN\" }";

    private static final String TEMPORARY_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"4087.64\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Temporary\"}";
    private static final String AVAILABLE_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"4087.64\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Available\"}";

    static AccountBalanceEntity interimAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_INTERIM_AVAILABLE, AccountBalanceEntity.class);
    }

    static CreditLineEntity temporaryCreditLine() {
        return SerializationUtils.deserializeFromString(
                TEMPORARY_CREDIT_LINE, CreditLineEntity.class);
    }

    static CreditLineEntity availableCreditLine() {
        return SerializationUtils.deserializeFromString(
                AVAILABLE_CREDIT_LINE, CreditLineEntity.class);
    }

    static AccountBalanceEntity closingBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_CLOSING_BOOKED, AccountBalanceEntity.class);
    }

    static AccountBalanceEntity previouslyClosedBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_PREVIOUSLY_CLOSED_BOOKED, AccountBalanceEntity.class);
    }

    static AccountEntity creditCardAccount() {
        return SerializationUtils.deserializeFromString(CREDIT_CARD_ACCOUNT, AccountEntity.class);
    }

    static AccountIdentifierEntity panIdentifier() {
        return SerializationUtils.deserializeFromString(
                PAN_IDENTIFIER, AccountIdentifierEntity.class);
    }
}
