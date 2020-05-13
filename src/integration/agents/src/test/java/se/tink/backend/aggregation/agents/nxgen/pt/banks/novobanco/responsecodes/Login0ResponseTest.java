package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData;

public class Login0ResponseTest {

    @Test
    public void shouldReportSuccessIfFailedLoginResponse() {
        Login0Response response =
                Login0TestData.getResponse(Login0TestData.FAILED_REQUEST_REJECTED);
        assertFalse(response.isValidCredentials());
    }

    @Test
    public void shouldReportSuccessIfSuccessfulLoginResponse() {
        Login0Response response = Login0TestData.getResponse(Login0TestData.SUCCESSFUL_LOGIN);
        assertTrue(response.isValidCredentials());
    }
}
