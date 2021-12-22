package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entity;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

// TODO: To be adjusted when feature enabled - currently checks if it is a dry run (no effect)
@RunWith(JUnitParamsRunner.class)
public class AccountBalanceEntityTest {

    private static final String INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_INCLUDED =
            "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_CREDIT_AND_TEMPORARY_INCLUDED =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"2.00\",\"Currency\":\"GBP\"},\"Type\":\"Temporary\"},{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_SMALLER_THAN_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";

    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_EQUAL_TO_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_BIGGER_THAN_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"20.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";

    private static final String INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED =
            "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":false,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED_WITH_EMPTY_AMOUNT =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Credit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":false,\"Amount\":{},\"Type\":\"Credit\"}]}";

    @Test
    @Parameters(method = "balancesForSubtraction")
    public void shouldSubtractCreditLine(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine = balance.getAmountWithoutCreditLine();
        ExactCurrencyAmount balanceAmount = balance.getAmount();

        // Dry run
        //
        // assertThat(balanceAmountWithoutCreditLine).isEqualTo(ExactCurrencyAmount.of("5.00",
        // "GBP"));
        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balanceAmount);
    }

    @Test
    @Parameters(method = "balancesForReturningZero")
    public void shouldReturnZero(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine = balance.getAmountWithoutCreditLine();
        ExactCurrencyAmount balanceAmount = balance.getAmount();

        // Dry run
        //
        // assertThat(balanceAmountWithoutCreditLine).isEqualTo(ExactCurrencyAmount.zero("GBP"));
        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balanceAmount);
    }

    @Test
    @Parameters(method = "balancesNotForSubtraction")
    public void shouldNotSubtractCreditLine(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine = balance.getAmountWithoutCreditLine();
        ExactCurrencyAmount balanceAmount = balance.getAmount();

        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balanceAmount);
    }

    @SuppressWarnings("unused")
    private Object[] balancesForSubtraction() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_INCLUDED,
                        AccountBalanceEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_CREDIT_AND_TEMPORARY_INCLUDED,
                        AccountBalanceEntity.class),
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_SMALLER_THAN_BALANCE,
                        AccountBalanceEntity.class)
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] balancesForReturningZero() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_EQUAL_TO_BALANCE,
                        AccountBalanceEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_BIGGER_THAN_BALANCE,
                        AccountBalanceEntity.class)
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] balancesNotForSubtraction() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED,
                        AccountBalanceEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED_WITH_EMPTY_AMOUNT,
                        AccountBalanceEntity.class)
            }
        };
    }
}
