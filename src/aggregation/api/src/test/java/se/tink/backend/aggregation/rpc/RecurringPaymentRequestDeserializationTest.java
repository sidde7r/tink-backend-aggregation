package se.tink.backend.aggregation.rpc;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RecurringPaymentRequestDeserializationTest {

    @Test
    public void deserializableFromJsonTest() {
        // Given
        String jsonPayload = getExampleJson();

        // When
        RecurringPaymentRequest recurringPaymentRequest =
                SerializationUtils.deserializeFromString(
                        jsonPayload, RecurringPaymentRequest.class);

        // Then
        Assert.assertNotNull(recurringPaymentRequest);
    }

    private String getExampleJson() {
        return "{\n"
                + "  \"credentials\": {\n"
                + "    \"providerLatency\": 0,\n"
                + "    \"id\": \"f5ea8c349da943d99be6121b57243e51\",\n"
                + "    \"providerName\": \"se-test-password\",\n"
                + "    \"dataVersion\": 1,\n"
                + "    \"status\": \"UPDATED\",\n"
                + "    \"statusPayload\": \"Updated.\",\n"
                + "    \"statusUpdated\": 1616430451074,\n"
                + "    \"type\": \"PASSWORD\",\n"
                + "    \"updated\": 1616430451069,\n"
                + "    \"userId\": \"232fac9f0b654b929364913449cb7c0b\",\n"
                + "    \"sensitiveDataSerialized\": \"{\\\"version\\\":2,\\\"timestamp\\\":1616430451092,\\\"keyId\\\":0,\\\"fields\\\":\\\"{\\\\\\\"version\\\\\\\":2,\\\\\\\"timestamp\\\\\\\":\\\\\\\"2021-03-22T16:27:31.092Z\\\\\\\",\\\\\\\"keyId\\\\\\\":3,\\\\\\\"payload\\\\\\\":\\\\\\\"{\\\\\\\\\\\\\\\"iv\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"M24SsehIsvvda6L/\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"data\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"ljy7oA6rN8xofBOIiXB9lTMqktDON68gUj3VL9+EFPbg+WCkhNva0A\\\\\\\\u003d\\\\\\\\u003d\\\\\\\\\\\\\\\"}\\\\\\\"}\\\",\\\"payload\\\":\\\"{\\\\\\\"version\\\\\\\":2,\\\\\\\"timestamp\\\\\\\":\\\\\\\"2021-03-22T16:27:31.092Z\\\\\\\",\\\\\\\"keyId\\\\\\\":3,\\\\\\\"payload\\\\\\\":\\\\\\\"{\\\\\\\\\\\\\\\"iv\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"L4GYKhhWPm3EH0zo\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"data\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"+wzXwqbWhHzT4DX8zrzMz/O0wVaqL+LwyYygpEfNhuX13nvLROGfntxS2yefa0FLpqUqokdRsll/U8yy5zkAEKYAobxrnQsbMT4oEO/e3m1rRu6D7UV/KirXYvESYAnOzIxsw4r+j7Q45HN3chuErv7K\\\\\\\\\\\\\\\"}\\\\\\\"}\\\"}\",\n"
                + "    \"sensitivePayload\": {}\n"
                + "  },\n"
                + "  \"provider\": {\n"
                + "    \"accessType\": \"OTHER\",\n"
                + "    \"authenticationUserType\": \"PERSONAL\",\n"
                + "    \"capabilitiesSerialized\": \"[\\\"TRANSFERS\\\",\\\"MORTGAGE_AGGREGATION\\\",\\\"CHECKING_ACCOUNTS\\\",\\\"SAVINGS_ACCOUNTS\\\",\\\"CREDIT_CARDS\\\",\\\"LOANS\\\",\\\"INVESTMENTS\\\",\\\"IDENTITY_DATA\\\"]\",\n"
                + "    \"pisCapabilitiesSerialized\": \"[\\\"PIS_SE_BG\\\",\\\"PIS_SE_PG\\\",\\\"PIS_SE_BANK_TRANSFERS\\\"]\",\n"
                + "    \"className\": \"nxgen.demo.banks.password.PasswordDemoAgent\",\n"
                + "    \"credentialsType\": \"PASSWORD\",\n"
                + "    \"currency\": \"SEK\",\n"
                + "    \"displayName\": \"Test Password\",\n"
                + "    \"displayDescription\": \"Password\",\n"
                + "    \"defaultDataVersion\": \"0\",\n"
                + "    \"financialInstitutionId\": \"4d0c65519a5e5a0d80e218a92f9ae1d6\",\n"
                + "    \"financialInstitutionName\": \"Test Password\",\n"
                + "    \"groupDisplayName\": \"Test Password\",\n"
                + "    \"market\": \"SE\",\n"
                + "    \"multiFactor\": true,\n"
                + "    \"name\": \"se-test-password\",\n"
                + "    \"passwordHelpText\": \"Use the same username and password as you would in the bank\\u0027s mobile app.\",\n"
                + "    \"popular\": true,\n"
                + "    \"backgroundRefreshMaxFrequency\": 1,\n"
                + "    \"backgroundRefreshEnabled\": true,\n"
                + "    \"status\": \"ENABLED\",\n"
                + "    \"transactional\": true,\n"
                + "    \"type\": \"TEST\",\n"
                + "    \"releaseStatus\": \"GENERALLY_AVAILABLE\",\n"
                + "    \"financialServices\": [\n"
                + "      {\n"
                + "        \"segment\": \"PERSONAL\",\n"
                + "        \"shortName\": \"Personal Banking\",\n"
                + "        \"financialServiceSegment\": \"PERSONAL\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"agentSources\": [\n"
                + "      \"AGGREGATION_SERVICE\"\n"
                + "    ],\n"
                + "    \"fields\": [\n"
                + "      {\n"
                + "        \"description\": \"Username\",\n"
                + "        \"immutable\": true,\n"
                + "        \"masked\": false,\n"
                + "        \"name\": \"username\",\n"
                + "        \"numeric\": false,\n"
                + "        \"optional\": false,\n"
                + "        \"sensitive\": false,\n"
                + "        \"checkbox\": false\n"
                + "      },\n"
                + "      {\n"
                + "        \"description\": \"Password\",\n"
                + "        \"immutable\": false,\n"
                + "        \"masked\": true,\n"
                + "        \"name\": \"password\",\n"
                + "        \"numeric\": false,\n"
                + "        \"optional\": false,\n"
                + "        \"sensitive\": true,\n"
                + "        \"checkbox\": false\n"
                + "      }\n"
                + "    ],\n"
                + "    \"supplementalFields\": []\n"
                + "  },\n"
                + "  \"user\": {\n"
                + "    \"created\": 1616430408000,\n"
                + "    \"flags\": [\n"
                + "      \"DETECT_NATIONAL_ID\",\n"
                + "      \"ACTIONABLE_INSIGHTS\",\n"
                + "      \"ANONYMOUS\",\n"
                + "      \"APPLICATIONS_INTERCOM_CHAT\",\n"
                + "      \"SPLIT_TRANSACTIONS\",\n"
                + "      \"NO_TINK_USER\",\n"
                + "      \"TEMPORARY\",\n"
                + "      \"RECURRING_TRANSACTIONS\",\n"
                + "      \"BUDGETS_V2\",\n"
                + "      \"TRANSFERS\",\n"
                + "      \"TEST_MANUAL_REFRESH_REMINDER_OFF\",\n"
                + "      \"BUDGETS_MIGRATE\"\n"
                + "    ],\n"
                + "    \"id\": \"232fac9f0b654b929364913449cb7c0b\",\n"
                + "    \"profile\": {\n"
                + "      \"locale\": \"en_US\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"create\": false,\n"
                + "  \"update\": false,\n"
                + "  \"appUriId\": \"ebe8dbf0-95e2-4bde-9582-bbd2b016feed\",\n"
                + "  \"dataFetchingRestrictions\": [],\n"
                + "  \"timePutOnQueue\": 0,\n"
                + "  \"timeLeavingQueue\": 0,\n"
                + "  \"signableOperation\": {\n"
                + "    \"created\": 1616430453482,\n"
                + "    \"id\": \"815814a0-8b2b-11eb-8c88-85fc7107b170\",\n"
                + "    \"status\": \"CREATED\",\n"
                + "    \"type\": \"RECURRING_PAYMENT\",\n"
                + "    \"underlyingId\": \"b81b364a-01e0-40b9-a05d-3e30c42c55f2\",\n"
                + "    \"updated\": 1616430453482,\n"
                + "    \"userId\": \"232fac9f-0b65-4b92-9364-913449cb7c0b\",\n"
                + "    \"credentialsId\": \"f5ea8c34-9da9-43d9-9be6-121b57243e51\"\n"
                + "  },\n"
                + "  \"skipRefresh\": false,\n"
                + "  \"forceAuthenticate\": false\n"
                + "}";
    }
}
