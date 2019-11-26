package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData;

public class Login0ResponseTest {

    @Test
    public void testFailedLogin() {
        Login0Response response = Login0TestData.getResponse(Login0TestData.FAILED_LOGIN);
        assertFalse(response.isValidCredentials());
    }

    @Test
    public void testSuccessfulLogin() {
        Login0Response response = Login0TestData.getResponse(Login0TestData.SUCCESSFUL_LOGIN);
        assertTrue(response.isValidCredentials());
    }
}
