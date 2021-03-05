package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.DEBTOR_ACCOUNT_NOT_FOUND;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.ILLEGAL_PAYMENT_TYPE_FOR_DEBTOR_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.TestUtils.getAccountNumbersResponse;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountNumbersResponse;
import se.tink.libraries.account.AccountIdentifier.Type;

public class AccountNumbersResponseTest {

    @Test
    public void testSuccess() throws DebtorValidationException {
        AccountNumbersResponse accountNumbersResponse = getAccountNumbersResponse("1234", "4321");
        accountNumbersResponse.checkIfTransactionTypeIsAllowed("1234", Type.SE);
    }

    @Test
    public void testAccountNotFoundException() {
        AccountNumbersResponse accountNumbersResponse = getAccountNumbersResponse("1234", "4321");

        Throwable thrown =
                catchThrowable(
                        () ->
                                accountNumbersResponse.checkIfTransactionTypeIsAllowed(
                                        "12345", Type.SE));
        Assertions.assertThat(thrown).isInstanceOf(DebtorValidationException.class);
        Assertions.assertThat(thrown).hasMessage(DEBTOR_ACCOUNT_NOT_FOUND);
    }

    @Test
    public void testIllegalPaymentTypeForDebtorAccountException() {
        AccountNumbersResponse accountNumbersResponse = getAccountNumbersResponse("1234", "4321");

        Throwable thrown =
                catchThrowable(
                        () ->
                                accountNumbersResponse.checkIfTransactionTypeIsAllowed(
                                        "4321", Type.SE));
        Assertions.assertThat(thrown).isInstanceOf(DebtorValidationException.class);
        Assertions.assertThat(thrown).hasMessage(ILLEGAL_PAYMENT_TYPE_FOR_DEBTOR_ACCOUNT);
    }

    @Test
    public void testIllegalPaymentTypeForBGDebtorAccountException() {
        AccountNumbersResponse accountNumbersResponse = getAccountNumbersResponse("1234", "4321");

        Throwable thrown =
                catchThrowable(
                        () ->
                                accountNumbersResponse.checkIfTransactionTypeIsAllowed(
                                        "4321", Type.SE_BG));
        Assertions.assertThat(thrown).isInstanceOf(DebtorValidationException.class);
        Assertions.assertThat(thrown).hasMessage(ILLEGAL_PAYMENT_TYPE_FOR_DEBTOR_ACCOUNT);
    }
}
