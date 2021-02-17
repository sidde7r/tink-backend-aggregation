package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TransactionalAccountTestData {

    public static ConsentResponse getCreateConsentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"data\": {\n"
                        + "  \"categoriesOutcome\": [\n"
                        + "    {\n"
                        + "      \"name\": \"ACCOUNT_INFO\",\n"
                        + "      \"outcome\": true\n"
                        + "    }\n"
                        + "    ]\n"
                        + "  },\n"
                        + "    \"result\": {\n"
                        + "       \"requestId\": \"852923860913816585143842\",\n"
                        + "       \"outcome\": \"SUCCESS\",\n"
                        + "       \"flushMessages\": true,\n"
                        + "       \"messages\": []\n"
                        + "  },\n"
                        + "  \"_embedded\": {},\n"
                        + "  \"resources\": {\n"
                        + "    \"resourceId\": \"89237de7-175f-447d-b1ed-47704fe23a72\"\n"
                        + "  }\n"
                        + "}",
                ConsentResponse.class);
    }

    public static ConsentAuthorizationResponse getAuthorizeConsentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"data\": {\n"
                        + "      \"scaRedirectURL\": \"https://developer.chebanca.it/sandbox/sca/simulate?resourceId=89237de7-175f-447d-b1ed-47704fe23a72\",\n"
                        + "      \"chosenScaApproach\": \"REDIRECT\"\n"
                        + "  },\n"
                        + "  \"result\": {\n"
                        + "      \"requestId\": \"908497645230254113240748\",\n"
                        + "      \"outcome\": \"SUCCESS\",\n"
                        + "      \"flushMessages\": true,\n"
                        + "      \"messages\": []\n"
                        + "  },\n"
                        + "  \"resources\": {\n"
                        + "      \"resourceId\": \"89237de7-175f-447d-b1ed-47704fe23a72\"\n"
                        + "  }\n"
                        + "}",
                ConsentAuthorizationResponse.class);
    }

    public static GetAccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"data\": {\n"
                        + "      \"accounts\": [\n"
                        + "          {\n"
                        + "              \"accountId\": \"0001658072\",\n"
                        + "              \"product\": {\n"
                        + "                  \"code\": \"0633\",\n"
                        + "                  \"description\": \"CONTO YELLOW\"\n"
                        + "              },\n"
                        + "              \"currency\": \"EUR\",\n"
                        + "              \"iban\": \"IT04G0305801604100571657883\",\n"
                        + "              \"name\": \"71657883\"\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"accountId\": \"0001712638\",\n"
                        + "              \"product\": {\n"
                        + "                  \"code\": \"0649\",\n"
                        + "                  \"description\": \"CONTO CORRENTE 0649\"\n"
                        + "               },\n"
                        + "              \"currency\": \"EUR\",\n"
                        + "              \"iban\": \"IT87P03058016041005737885636\",\n"
                        + "              \"name\": \"37885636\"\n"
                        + "          }\n"
                        + "      ]\n"
                        + "  },\n"
                        + "  \"result\": {\n"
                        + "      \"requestId\": \"056336878538284929555964\",\n"
                        + "      \"outcome\": \"SUCCESS\",\n"
                        + "      \"flushMessages\": true,\n"
                        + "      \"messages\": []\n"
                        + "  },\n"
                        + "  \"_links\": {\n"
                        + "      \"self\": {\n"
                        + "          \"href\": \" \",\n"
                        + "          \"method\": \"GET\"\n"
                        + "      },\n"
                        + "      \"curies\": [\n"
                        + "          {\n"
                        + "              \"href\": \"https://sandbox-api.chebanca.io/private/customers/4450923/accounts/{rel}\",\n"
                        + "              \"name\": \"accounts\"\n"
                        + "          }\n"
                        + "      ]\n"
                        + "  },\n"
                        + "  \"_embedded\": {}\n"
                        + "}\n",
                GetAccountsResponse.class);
    }

    public static CustomerIdResponse getCustomerId() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"data\": {\n"
                        + "      \"accounts\": [\n"
                        + "          {\n"
                        + "              \"accountId\": \"0001658072\",\n"
                        + "              \"product\": {\n"
                        + "                  \"code\": \"0633\",\n"
                        + "                  \"description\": \"CONTO YELLOW\"\n"
                        + "              },\n"
                        + "              \"currency\": \"EUR\",\n"
                        + "              \"iban\": \"IT04G0305801604100571657883\",\n"
                        + "              \"name\": \"71657883\"\n"
                        + "          },\n"
                        + "          {\n"
                        + "              \"accountId\": \"0001712638\",\n"
                        + "              \"product\": {\n"
                        + "                  \"code\": \"0649\",\n"
                        + "                  \"description\": \"CONTO CORRENTE 0649\"\n"
                        + "              },\n"
                        + "              \"currency\": \"EUR\",\n"
                        + "              \"iban\": \"IT87P03058016041005737885636\",\n"
                        + "              \"name\": \"37885636\"\n"
                        + "          }\n"
                        + "      ]\n"
                        + "  },\n"
                        + "  \"result\": {\n"
                        + "      \"requestId\": \"056336878538284929555964\",\n"
                        + "      \"outcome\": \"SUCCESS\",\n"
                        + "      \"flushMessages\": true,\n"
                        + "      \"messages\": []\n"
                        + "  },\n"
                        + "  \"_links\": {\n"
                        + "      \"self\": {\n"
                        + "          \"href\": \" \",\n"
                        + "          \"method\": \"GET\"\n"
                        + "      },\n"
                        + "      \"curies\": [\n"
                        + "          {\n"
                        + "              \"href\": \"https://sandbox-api.chebanca.io/private/customers/4450923/accounts/{rel}\",\n"
                        + "              \"name\": \"accounts\"\n"
                        + "          }\n"
                        + "      ]\n"
                        + "  },\n"
                        + "  \"_embedded\": {}\n"
                        + "}\n",
                CustomerIdResponse.class);
    }

    public static GetBalancesResponse getBalances() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"data\": {\n"
                        + "    \"accountBalance\": {\n"
                        + "      \"amount\": \"849881.560\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"availableBalance\": {\n"
                        + "      \"amount\": \"814047.160\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"accountAvailability\": {\n"
                        + "      \"amount\": \"814047.160\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"accountAvailableCredit\": {\n"
                        + "      \"amount\": \"0.000\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"date\": \"21/02/2019\",\n"
                        + "    \"hour\": \"15:39\",\n"
                        + "    \"isPocketAccount\": false\n"
                        + "  },\n"
                        + "  \"result\": {\n"
                        + "     \"requestId\": \"128876430262614637647642\",\n"
                        + "     \"outcome\": \"SUCCESS\",\n"
                        + "     \"flushMessages\": false,\n"
                        + "     \"messages\": []\n"
                        + "  },\n"
                        + "  \"_links\": {\n"
                        + "     \"self\": {\n"
                        + "       \"href\": \"retrieve\",\n"
                        + "       \"method\": \"GET\"\n"
                        + "     },\n"
                        + "     \"curies\": [{\n"
                        + "       \"href\": \"https://sandbox-api.chebanca.io/private/customers/4450923/products/0001712638/balance/{rel}\",\n"
                        + "       \"name\": \"balance\"\n"
                        + "     }]\n"
                        + "  },\n"
                        + "  \"_embedded\": {\n"
                        + "    \n"
                        + "  }\n"
                        + "}\n",
                GetBalancesResponse.class);
    }
}
