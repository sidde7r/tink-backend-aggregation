package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.authenticator;

import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaTestData;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;

@Ignore
public class BankAustriaAuthenticatorTest {
    @Test
    public void shouldDetectWrongCredentials() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OtmlResponse otml =
                objectMapper.readValue(
                        BankAustriaTestData.OTML_ERROR_LOGIN_WRONG_CREDENTIALS, OtmlResponse.class);
        OtmlResponseConverter otmlResponseConverter = new OtmlResponseConverter();
        BankAustriaAuthenticator bankAustriaAuthenticator1 =
                new BankAustriaAuthenticator(null, null, null, null, otmlResponseConverter);

        assertTrue(bankAustriaAuthenticator1.wrongCredentials(otml));
    }
}
