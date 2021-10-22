package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import static org.junit.Assert.assertEquals;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.BankIdResponseStatus;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class BankIdCollectResponseTest {

    @Test
    @Parameters(method = "getParameters")
    public void shouldReturnBankIdStatusFromGivenState(
            BankIdResponseStatus state, BankIdStatus expectedBankIdStatus) {
        assertEquals(expectedBankIdStatus, getBankIdCollectResponse(state).getBankIdStatus());
    }

    private Object[] getParameters() {
        return new Object[] {
            new Object[] {BankIdResponseStatus.COMPLETE, BankIdStatus.DONE},
            new Object[] {BankIdResponseStatus.ALREADY_IN_PROGRESS, BankIdStatus.WAITING},
            new Object[] {BankIdResponseStatus.USER_SIGN, BankIdStatus.WAITING},
            new Object[] {BankIdResponseStatus.STARTED, BankIdStatus.WAITING},
            new Object[] {BankIdResponseStatus.NO_CLIENT, BankIdStatus.NO_CLIENT},
            new Object[] {BankIdResponseStatus.CANCELLED, BankIdStatus.CANCELLED},
            new Object[] {BankIdResponseStatus.TIMEOUT, BankIdStatus.TIMEOUT},
            new Object[] {BankIdResponseStatus.UNKNOWN, BankIdStatus.FAILED_UNKNOWN},
        };
    }

    private static BankIdCollectResponse getBankIdCollectResponse(BankIdResponseStatus state) {
        return SerializationUtils.deserializeFromString(
                "{\"name\":\"firstName\",\"logins\":[{\"customerId\":\"2000004\",\"username\":"
                        + "\"userName\",\"accounts\":[{\"accountName\":\"1000000\",\"accountType\":"
                        + "\"Investeringssparkonto\"},{\"accountName\":\"1000052\",\"accountType\":"
                        + "\"Aktie- & fondkonto\"}],\"loginPath\":"
                        + "\"/_api/authentication/sessions/bankid/transactionId/2000004\"}],"
                        + "\"transactionId\":\"transactionId\",\"recommendedTargetCustomers\":"
                        + "[],\"state\":\""
                        + state.getStatusCode()
                        + "\"}",
                BankIdCollectResponse.class);
    }
}
