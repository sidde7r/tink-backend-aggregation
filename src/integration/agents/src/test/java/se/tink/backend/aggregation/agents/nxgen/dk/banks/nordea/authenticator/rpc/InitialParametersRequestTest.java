package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.InitialParametersRequestEntity;
import static org.junit.Assert.assertTrue;

public class InitialParametersRequestTest {
    // this is just to check that nothing breaks
    @Test
    public void createRequest() throws Exception {
        String authLevel = "1";
        String remeberUserId = "";
        InitialParametersRequestEntity nordeaInitialParametersRequest = new InitialParametersRequestEntity()
                .setAuthLevel(authLevel)
                .setRemeberUserId(remeberUserId);
        InitialParametersRequestBody request = new InitialParametersRequestBody()
                .setInitialParametersRequest(nordeaInitialParametersRequest);

        ObjectMapper mapper = new ObjectMapper();
        String requestAsString = mapper.writeValueAsString(request);
        assertTrue(requestAsString.startsWith("{"));
    }
}
