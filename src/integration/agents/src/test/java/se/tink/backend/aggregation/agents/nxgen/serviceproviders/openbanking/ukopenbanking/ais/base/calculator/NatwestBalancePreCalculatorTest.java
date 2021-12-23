package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class NatwestBalancePreCalculatorTest {

    private static final String INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_INCLUDED =
            "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_CREDIT_AND_TEMPORARY_INCLUDED =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"2.00\",\"Currency\":\"GBP\"},\"Type\":\"Temporary\"},{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_SMALLER_THAN_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";

    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_EQUAL_TO_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";
    private static final String
            INTERIM_AVAILABLE_BALANCE_WITH_CREDIT_LINE_AVAILABLE_INCLUDED_BIGGER_THAN_BALANCE =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimAvailable\",\"CreditLine\":[{\"Included\":true,\"Amount\":{\"Amount\":\"20.00\",\"Currency\":\"GBP\"},\"Type\":\"Available\"}]}";

    private static final String INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED =
            "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":false,\"Amount\":{\"Amount\":\"5.00\",\"Currency\":\"GBP\"},\"Type\":\"Credit\"}]}";
    private static final String
            INTERIM_BOOKED_BALANCE_WITH_CREDIT_LINE_CREDIT_NOT_INCLUDED_WITH_EMPTY_AMOUNT =
                    "{\"Amount\":{\"Amount\":\"10.00\",\"Currency\":\"GBP\"},\"CreditDebitIndicator\":\"Debit\",\"Type\":\"InterimBooked\",\"CreditLine\":[{\"Included\":false,\"Amount\":{},\"Type\":\"Credit\"}]}";

    private final BalancePreCalculator balancePreCalculator =
            new NatwestGroupBalancePreCalculator();

    @Test
    @Parameters(method = "balancesForSubtraction")
    public void shouldNegateBalanceAndSubtractCreditLine(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balancePreCalculator.calculateBalanceAmountConsideringCreditLines(
                        balance.getType(), balance.getAmount(), balance.getCreditLine());

        // Dry run
        //
        // assertThat(balanceAmountWithoutCreditLine).isEqualTo(ExactCurrencyAmount.of("5.00",
        // "GBP"));
        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balance.getAmount().negate());
    }

    @Test
    @Parameters(method = "balancesForReturningZero")
    public void shouldReturnZero(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balancePreCalculator.calculateBalanceAmountConsideringCreditLines(
                        balance.getType(), balance.getAmount(), balance.getCreditLine());

        // Dry run
        //
        // assertThat(balanceAmountWithoutCreditLine).isEqualTo(ExactCurrencyAmount.zero("GBP"));
        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balance.getAmount().negate());
    }

    @Test
    @Parameters(method = "balancesNotForSubtraction")
    public void shouldNegateBalanceOnly(AccountBalanceEntity balance) {
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balancePreCalculator.calculateBalanceAmountConsideringCreditLines(
                        balance.getType(), balance.getAmount(), balance.getCreditLine());

        assertThat(balanceAmountWithoutCreditLine).isEqualTo(balance.getAmount().negate());
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
