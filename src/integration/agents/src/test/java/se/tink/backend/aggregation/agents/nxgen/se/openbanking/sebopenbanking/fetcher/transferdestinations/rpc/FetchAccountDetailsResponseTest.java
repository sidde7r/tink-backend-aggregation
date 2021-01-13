package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.rpc;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FetchAccountDetailsResponseTest {

    private static final String FETCH_ACCOUNT_DETAILS_RESPONSE =
            "{\n"
                    + "  \"resourceId\": \"RESOURCE_ID\",\n"
                    + "  \"iban\": \"IBAN_1\",\n"
                    + "  \"bban\": \"BBAN_1\",\n"
                    + "  \"bic\": \"ESSESESS\",\n"
                    + "  \"status\": \"enabled\",\n"
                    + "  \"currency\": \"SEK\",\n"
                    + "  \"ownerName\": \"OWNER\",\n"
                    + "  \"creditLine\": \"0.000\",\n"
                    + "  \"product\": \"Privatkonto\",\n"
                    + "  \"name\": \"Checking account\",\n"
                    + "  \"bicAddress\": \"SEB, 106 40 Stockholm\",\n"
                    + "  \"cardLinkedToAccount\": true,\n"
                    + "  \"paymentService\": true,\n"
                    + "  \"accountInterest\": \"0.0\",\n"
                    + "  \"accountOwners\": [\n"
                    + "    \"OWNER_NAME\"\n"
                    + "  ],\n"
                    + "  \"_links\": {\n"
                    + "    \"transactions\": {\n"
                    + "      \"href\": \"/accounts/RESOURCE_ID/transactions?bookingStatus=booked\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";

    @Test
    public void testIsPaymentServiceEnabled() {
        FetchAccountDetailsResponse fetchAccountDetailsResponse =
                SerializationUtils.deserializeFromString(
                        FETCH_ACCOUNT_DETAILS_RESPONSE, FetchAccountDetailsResponse.class);
        Assert.assertTrue(fetchAccountDetailsResponse.isPaymentService());
    }
}
