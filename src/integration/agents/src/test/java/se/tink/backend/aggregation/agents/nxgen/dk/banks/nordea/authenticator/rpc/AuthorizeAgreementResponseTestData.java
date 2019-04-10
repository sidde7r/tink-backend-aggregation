package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthorizeAgreementResponseTestData {

    public static AuthorizeAgreementResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AuthorizeAgreementResponse response =
                mapper.readValue(TEST_DATA, AuthorizeAgreementResponse.class);

        return response;
    }

    private static final String TEST_DATA =
            "{"
                    + "\"authorizeAgreementResponse\": {"
                    + "\"authenticationToken\": {"
                    + "\"token\": {"
                    + "\"$\": \"wsT3gzuCiPmlvZcP7u84e40eMgriEawnh4xlxljZN%2F\""
                    + "},"
                    + "\"authLevel\": {"
                    + "\"$\": \"1\""
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
                    + "\"profileId\": {"
                    + "\"$\": \"6uNPFE48Mqk3zJwPUotqVQ=\""
                    + "}"
                    + "},"
                    + "\"secureMailAccess\": {"
                    + "\"$\": \"Y\""
                    + "},"
                    + "\"nativeMessagingAccess\": {"
                    + "\"$\": \"N\""
                    + "}"
                    + "}"
                    + "}";
}
