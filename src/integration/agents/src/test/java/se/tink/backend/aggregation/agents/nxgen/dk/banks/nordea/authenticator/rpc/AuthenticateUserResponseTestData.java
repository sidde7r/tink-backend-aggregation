package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticateUserResponseTestData {

    public static NemidAuthenticateUserResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NemidAuthenticateUserResponse response =
                mapper.readValue(TEST_DATA, NemidAuthenticateUserResponse.class);

        return response;
    }

    private static final String TEST_DATA =
            "{"
                    + "\"authenticateUserResponse\": {"
                    + "\"authenticationToken\": {"
                    + "\"token\": {"
                    + "\"$\": \"9HDSlsfFHXa9VxhSRvaxbeNpAw%3D%3D\""
                    + "},"
                    + "\"authLevel\": {"
                    + "\"$\": \"1\""
                    + "},"
                    + "\"rememberUserId\": {"
                    + "\"$\": \"y7MlPKWROjRURd1QfHZYMUX0=\""
                    + "},"
                    + "\"authMethod\": {"
                    + "\"$\": \"NEMID\""
                    + "},"
                    + "\"loginTime\": {"
                    + "\"$\": \"2017-12-13T12:38:09.746Z\""
                    + "},"
                    + "\"notAfter\": {"
                    + "\"$\": \"2017-12-13T12:48:09Z\""
                    + "},"
                    + "\"sessionMaxLength\": {"
                    + "\"$\": 1800"
                    + "},"
                    + "\"tokenMaxAge\": {"
                    + "\"$\": 600"
                    + "},"
                    + "\"swippExists\": {"
                    + "\"$\": \"N\""
                    + "}"
                    + "},"
                    + "\"agreements\": {"
                    + "\"agreement\": {"
                    + "\"@id\": {"
                    + "\"$\": \"p%2FTF6u5QtxYDS7cqKmGfgDh%2Bz9lbL1cxJ%2FVO55Tarau1ZqZQrvqbULlb8%3D\""
                    + "},"
                    + "\"agreementNumber\": {"
                    + "\"$\": \"0011182450\""
                    + "},"
                    + "\"agreementNickName\": {},"
                    + "\"swippAccess\": {"
                    + "\"$\": \"N\""
                    + "}"
                    + "}"
                    + "}"
                    + "}"
                    + "}";
}
