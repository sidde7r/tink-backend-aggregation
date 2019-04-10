package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AgreementListEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.NemidAuthenticateUserEntity;

public class AuthenticateUserRequestResponseTest {
    @Test
    public void createRequest() throws Exception {
        NemIdAuthenticateUserRequestBody request =
                new NemIdAuthenticateUserRequestBody()
                        .setNemIdAuthenticateUserRequest(
                                new NemidAuthenticateUserEntity()
                                        .setLoginType("MNEMID-LOGON")
                                        .setNemIdSessionId("DK-2017-12-13T12:37:33.192Z-9574a")
                                        .setNemIdToken("TOKEN"));

        ObjectMapper mapper = new ObjectMapper();
        String requestAsString = mapper.writeValueAsString(request);
        assertTrue(requestAsString.startsWith("{"));
    }

    @Test
    public void parseResponse() throws Exception {
        NemidAuthenticateUserResponse response = AuthenticateUserResponseTestData.getTestData();
        assertNotNull(response.getAuthenticateUserResponse());
        List<AgreementListEntity> agreements =
                response.getAuthenticateUserResponse().getAgreements();
        assertNotNull(agreements);
        assertEquals(1, agreements.size());
        AgreementEntity agreement = agreements.get(0).getAgreement();
        assertEquals(
                "p%2FTF6u5QtxYDS7cqKmGfgDh%2Bz9lbL1cxJ%2FVO55Tarau1ZqZQrvqbULlb8%3D",
                agreement.getId());

        assertEquals(
                "NEMID",
                response.getAuthenticateUserResponse().getAuthenticationToken().getAuthMethod());
        assertEquals(
                "1",
                response.getAuthenticateUserResponse().getAuthenticationToken().getAuthLevel());
        assertEquals(
                "600",
                response.getAuthenticateUserResponse().getAuthenticationToken().getTokenMaxAge());
    }
}
