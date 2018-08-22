package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthorizeAgreementDetails;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuthorizeAgreementRequestResponseTest {
    @Test
    public void createRequest() throws Exception {
        AuthorizeAgreementRequestBody request = new AuthorizeAgreementRequestBody()
                .setAuthorizeAgreementRequest(new AuthorizeAgreementDetails()
                        .setAgreement("AGREEMENT ID"));
        ObjectMapper mapper = new ObjectMapper();
        String requestAsString = mapper.writeValueAsString(request);
        assertTrue(requestAsString.startsWith("{"));
    }

    @Test
    public void parseResponse() throws Exception {
        AuthorizeAgreementResponse response = AuthorizeAgreementResponseTestData.getTestData();
        assertNotNull(response.getAuthorizeAgreementResponse());
        assertNotNull(response.getAuthorizeAgreementResponse().getAuthenticationToken());
        assertNotNull(response.getAuthorizeAgreementResponse().getAuthenticationToken().getAuthMethod());

        assertEquals("NEMID", response.getAuthorizeAgreementResponse().getAuthenticationToken().getAuthMethod());
        assertEquals("1", response.getAuthorizeAgreementResponse().getAuthenticationToken().getAuthLevel());
        assertEquals("600", response.getAuthorizeAgreementResponse().getAuthenticationToken().getTokenMaxAge());

        assertEquals("Y", response.getAuthorizeAgreementResponse().getSecureMailAccess());
        assertEquals("N", response.getAuthorizeAgreementResponse().getNativeMessagingAccess());
    }
}
