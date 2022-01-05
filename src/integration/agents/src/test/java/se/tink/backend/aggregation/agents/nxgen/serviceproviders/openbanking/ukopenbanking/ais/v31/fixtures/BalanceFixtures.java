package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BalanceFixtures {

    private static final String UPPERCASE_BALANCE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"CREDIT\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String LOWERCASE_BALANCE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"222.0512\",\"Currency\":\"EUR\"},\"CreditDebitIndicator\":\"debit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_CREDIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_EMPTY =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_DEBIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"222.051234\",\"Currency\":\"EUR\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String EMPTY_TYPE_BALANCE_CREDIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"CREDIT\",\"Type\":\"\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String WITHOUT_TYPE_BALANCE_CREDIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"CREDIT\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String NULL_TYPE_BALANCE_CREDIT =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"CREDIT\",\"Type\":null,\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    private static final String BALANCE_TYPE_INTERIM_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"333.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_TYPE_CLOSING_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"444.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ClosingBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_TYPE_PREVIOUSLY_CLOSED_BOOKED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"555.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"PreviouslyClosedBooked\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_TYPE_EXPECTED =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"123.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"Expected\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String BALANCE_TYPE_FORWARD_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"ForwardAvailable\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String EMPTY_BALANCE_TYPE_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"333.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String WITHOUT_BALANCE_TYPE_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"333.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";
    private static final String NULL_BALANCE_TYPE_AVAILABLE =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"333.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"null\",\"DateTime\":\"2019-11-20T14:24:56Z\"}";

    private static final String NULL_DATE_TIME =
            "{\"AccountId\":\"4cc\",\"Amount\":{\"Amount\":\"111.05\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"CREDIT\",\"Type\":\"InterimAvailable\",\"DateTime\":null}";

    private static final String TEMPORARY_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"111.11\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Temporary\"}";
    private static final String AVAILABLE_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"2222.22\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Available\"}";
    private static final String CREDIT_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"333.33\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Credit\"}";
    private static final String EMERGENCY_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"444.33\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Emergency\"}";
    private static final String PRE_AGREED_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"444.33\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"PreAgreed\"}";
    private static final String EMPTY_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"2222.22\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"\"}";
    private static final String WITHOUT_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"2222.22\",\"Currency\":\"GBP\"},\"Included\":false}";
    private static final String NULL_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"2222.22\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":null}";

    public static AccountBalanceEntity expectedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_TYPE_EXPECTED, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity forwardAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_TYPE_FORWARD_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceDebit() {
        return SerializationUtils.deserializeFromString(BALANCE_DEBIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceUppercaseCredit() {
        return SerializationUtils.deserializeFromString(
                UPPERCASE_BALANCE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceLowercaseDebit() {
        return SerializationUtils.deserializeFromString(
                LOWERCASE_BALANCE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceCredit() {
        return SerializationUtils.deserializeFromString(BALANCE_CREDIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceEmpty() {
        return SerializationUtils.deserializeFromString(BALANCE_EMPTY, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceWithEmptyType() {
        return SerializationUtils.deserializeFromString(
                EMPTY_TYPE_BALANCE_CREDIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceWithoutType() {
        return SerializationUtils.deserializeFromString(
                WITHOUT_TYPE_BALANCE_CREDIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity balanceWithNullType() {
        return SerializationUtils.deserializeFromString(
                NULL_TYPE_BALANCE_CREDIT, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity interimAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_TYPE_INTERIM_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity closingBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_TYPE_CLOSING_BOOKED, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity previouslyClosedBookedBalance() {
        return SerializationUtils.deserializeFromString(
                BALANCE_TYPE_PREVIOUSLY_CLOSED_BOOKED, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity emptyTypeAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                EMPTY_BALANCE_TYPE_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity withoutTypeAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                WITHOUT_BALANCE_TYPE_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity nullTypeAvailableBalance() {
        return SerializationUtils.deserializeFromString(
                NULL_BALANCE_TYPE_AVAILABLE, AccountBalanceEntity.class);
    }

    public static AccountBalanceEntity nullDateTime() {
        return SerializationUtils.deserializeFromString(NULL_DATE_TIME, AccountBalanceEntity.class);
    }

    public static CreditLineEntity temporaryCreditLine() {
        return SerializationUtils.deserializeFromString(
                TEMPORARY_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity availableCreditLine() {
        return SerializationUtils.deserializeFromString(
                AVAILABLE_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity emergencyCreditLine() {
        return SerializationUtils.deserializeFromString(
                EMERGENCY_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity preAgreedCreditLine() {
        return SerializationUtils.deserializeFromString(
                PRE_AGREED_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity creditCreditLine() {
        return SerializationUtils.deserializeFromString(CREDIT_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity emptyTypeCreditLine() {
        return SerializationUtils.deserializeFromString(EMPTY_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity withoutTypeCreditLine() {
        return SerializationUtils.deserializeFromString(
                WITHOUT_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity nullTypeCreditLine() {
        return SerializationUtils.deserializeFromString(NULL_CREDIT_LINE, CreditLineEntity.class);
    }
}
