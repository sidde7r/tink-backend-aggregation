package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.initialContext.InitialContextResponse;

public class InitialContextResponseTestData {

    public static InitialContextResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InitialContextResponse response = mapper.readValue(TEST_DATA, InitialContextResponse.class);

        return response;
    }

    private static final String TEST_DATA =
            "{"
                    + "\"getInitialContextOut\": {"
                    + "\"product\": {"
                    + "\"productType\": {"
                    + "\"$\": \"Account\""
                    + "},"
                    + "\"cardGroup\": {},"
                    + "\"productId\": {"
                    + "\"@id\": {"
                    + "\"$\": \"FaQxFvRe9l1swuGAP%2Blqb876gNrva3zrVA%3D%3D\""
                    + "},"
                    + "\"@view\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"@pay\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"@deposit\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"@ownTransferFrom\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"@ownTransferTo\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"@thirdParty\": {"
                    + "\"$\": true"
                    + "},"
                    + "\"$\": \"0001652140\""
                    + "},"
                    + "\"productNumber\": {"
                    + "\"$\": \"0381100143\""
                    + "},"
                    + "\"accountType\": {"
                    + "\"$\": \"100\""
                    + "},"
                    + "\"productTypeExtension\": {"
                    + "\"$\": \"DK1174\""
                    + "},"
                    + "\"currency\": {"
                    + "\"$\": \"DKK\""
                    + "},"
                    + "\"nickName\": {},"
                    + "\"productCode\": {"
                    + "\"$\": \"DK_ACT_DK1174\""
                    + "},"
                    + "\"productName\": {"
                    + "\"$\": \"Grundkonto\""
                    + "},"
                    + "\"balance\": {"
                    + "\"$\": 150.00"
                    + "},"
                    + "\"fundsAvailable\": {"
                    + "\"$\": 0.00"
                    + "},"
                    + "\"branchId\": {"
                    + "\"$\": \"1101\""
                    + "},"
                    + "\"productRole\": {"
                    + "\"$\": \"5\""
                    + "},"
                    + "\"mtgLoanName\": {},"
                    + "\"nextPayment\": {"
                    + "\"$\": 0.00"
                    + "},"
                    + "\"nextPaymentDate\": {},"
                    + "\"loanId\": {}"
                    + "}"
                    + "}"
                    + "}";
}
