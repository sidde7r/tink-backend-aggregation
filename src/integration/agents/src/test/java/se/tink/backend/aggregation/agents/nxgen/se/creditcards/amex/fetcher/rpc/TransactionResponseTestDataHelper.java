package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;

import java.io.IOException;

@Ignore
public class TransactionResponseTestDataHelper {
    private static final String END_OF_MESSAGE = "} }";
    public static String PROPER_MESSAGE_CONSTANTS =
            " {\n"
                    + "  \"transactionDetails\": {\n"
                    + "    \"status\": 0,\n"
                    + "    \"sectionHeaders\": {\n"
                    + "      \"currentStatements\": \"Nuvarande fakturor\",\n"
                    + "      \"previousStatements\": \"Tidigare fakturor\"\n"
                    + "    },\n"
                    + "    \"billingInfo\": {\n"
                    + "      \"message\": \"Besök vår webbplats för att se fler än 6 fakturaperioder\",\n"
                    + "      \"billingInfoDetails\": [\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"0\",\n"
                    + "          \"title\": \"Nyligen gjorda köp\",\n"
                    + "          \"label\": \"15 maj - Visa\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"1\",\n"
                    + "          \"title\": \"Nuvarande faktura\",\n"
                    + "          \"label\": \"15 apr - 14 maj\",\n"
                    + "          \"statementDate\": \"14/5/2018\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"2\",\n"
                    + "          \"title\": \"Föregående faktura\",\n"
                    + "          \"label\": \"15 mar - 14 apr\",\n"
                    + "          \"statementDate\": \"14/4/2018\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"3\",\n"
                    + "          \"title\": \"Föregående faktura\",\n"
                    + "          \"label\": \"15 feb - 14 mar\",\n"
                    + "          \"statementDate\": \"14/3/2018\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"4\",\n"
                    + "          \"title\": \"Föregående faktura\",\n"
                    + "          \"label\": \"15 jan - 14 feb\",\n"
                    + "          \"statementDate\": \"14/2/2018\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"5\",\n"
                    + "          \"title\": \"Föregående faktura\",\n"
                    + "          \"label\": \"15 dec - 14 jan\",\n"
                    + "          \"statementDate\": \"14/1/2018\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"billingIndex\": \"6\",\n"
                    + "          \"title\": \"Föregående faktura\",\n"
                    + "          \"label\": \"15 nov - 14 dec\",\n"
                    + "          \"statementDate\": \"14/12/2017\"\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    },\n"
                    + "    \"filterOptions\": {\n"
                    + "      \"title\": \"Filtrera transaktioner\",\n"
                    + "      \"transactionTypes\": {\n"
                    + "        \"header\": \"Transaktionstyp\",\n"
                    + "        \"values\": [\n"
                    + "          {\n"
                    + "            \"type\": \"ALL\",\n"
                    + "            \"label\": \"Alla transaktioner\",\n"
                    + "            \"defaultOption\": true\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"CREDIT\",\n"
                    + "            \"label\": \"Krediteringar/inbetalningar\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"DEBIT\",\n"
                    + "            \"label\": \"Debiteringar\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"FEE_AND_ADJUSTMENT\",\n"
                    + "            \"label\": \"Endast avgifter och justeringar\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"FOREIGN\",\n"
                    + "            \"label\": \"Utlandstransaktioner\"\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      },\n"
                    + "      \"sort\": {\n"
                    + "        \"header\": \"Sortera\",\n"
                    + "        \"values\": [\n"
                    + "          {\n"
                    + "            \"type\": \"MOST_RECENT_FIRST\",\n"
                    + "            \"label\": \"De senaste först\",\n"
                    + "            \"defaultOption\": true\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"OLDEST_FIRST\",\n"
                    + "            \"label\": \"De äldsta först\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"AMOUNT_ASCENDING\",\n"
                    + "            \"label\": \"Belopp stigande\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"AMOUNT_DESCENDING\",\n"
                    + "            \"label\": \"Belopp fallande\"\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      },\n"
                    + "      \"cardmembers\": {\n"
                    + "        \"header\": \"Kortmedlemmar\",\n"
                    + "        \"values\": [\n"
                    + "          {\n"
                    + "            \"type\": \"ALL\",\n"
                    + "            \"label\": \"Alla kortmedlemmar\",\n"
                    + "            \"defaultOption\": true\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"00\",\n"
                    + "            \"label\": \"XXXXXXX XXXXXXXX\"\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"type\": \"01\",\n"
                    + "            \"label\": \"XXXXXXX XXXXXXXX  XX\"\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      }\n"
                    + "    },";
    private static String NO_TRANSACTIONS_FOR_PERIOD_STRING =
            "    \"activityList\": [\n"
                    + "      {\n"
                    + "        \"billingIndex\": \"0\",\n"
                    + "        \"message\": \"Inga transaktioner nyligen\",\n"
                    + "        \"messageType\": \"INFORMATION\",\n"
                    + "        \"flexEnrolled\": false\n"
                    + "      }\n"
                    + "    ]";
    private static String PROPER_TRANSACTION_LIST_STRING =
            "\"activityList\": [{\n"
                    + "\"transactionList\": [{\n"
                    + "\"transactionId\": \"xXXXXXXXXXXXXXXXXXXXXXX---XXXX---20150412\",\n"
                    + "\"transactionReference\": \"XXXXXXXXXXXXXXXXXXXXXXX\",\n"
                    + "\"billingCycleIndex\": 0,\n"
                    + "\"suppIndex\": \"01\",\n"
                    + "\"displaySuppIcon\": false,\n"
                    + "\"type\": \"FOREIGN\",\n"
                    + "\"chargeDate\": {\n"
                    + "\"formattedDate\": \"12 apr 2015\",\n"
                    + "\"rawValue\": 20150412\n"
                    + "},\n"
                    + "\"amount\": {\n"
                    + "\"formattedAmount\": \"111,11 kr\",\n"
                    + "\"rawValue\": 111.11,\n"
                    + "\"stringRawValue\": \"111.11\"\n"
                    + "},\n"
                    + "\"description\": [\"Atlassian San Francisco\"],\n"
                    + "\"extendedTransactionDetails\": {\n"
                    + "\"merchantName\": \"ATLASSIAN\",\n"
                    + "\"address\": [\"94103-4521\", \"UNITED STATES OF AMERICA (THE)\"],\n"
                    + "\"processDate\": {\n"
                    + "\"formattedDate\": \"12 apr 2015\",\n"
                    + "\"rawValue\": 20150412\n"
                    + "}\n"
                    + "},\n"
                    + "\"foreignTransactionDetails\": {\n"
                    + "\"exchangeRate\": \"0.1182\",\n"
                    + "\"exchangeRateLabel\": \"Valutakurs:\",\n"
                    + "\"commission\": \"6,66 kr\",\n"
                    + "\"commissionLabel\": \"Avgift:\",\n"
                    + "\"foreignAmount\": \"33.33 USD\",\n"
                    + "\"foreignAmountLabel\": \"Spenderad utländsk valuta:\"\n"
                    + "},\n"
                    + "\"formattedAmount\": \"111,11 kr\"\n"
                    + "}],\n"
                    + "\"billingIndex\": \"0\",\n"
                    + "\"flexEnrolled\": false\n"
                    + "}]}";
    private static String ERROR_MESSAGE_STRING =
            "{\n"
                    + "  \"transactionDetails\": {\n"
                    + "    \"status\": -1,\n"
                    + "    \"message\": \"Ett fel inträffade tyvärr vid laddning av innehållet - försök igen senare\",\n"
                    + "    \"messageType\": \"ERROR\",\n"
                    + "    \"statusCode\": \"error\"\n"
                    + "  }\n"
                    + "}";
    private static ObjectMapper mapper = new ObjectMapper();

    public static TransactionResponse buildResponse(ResponseType type) throws IOException {
        TransactionResponse transactionResponse =
                mapper.readValue(type.getMessage(), TransactionResponse.class);
        return transactionResponse;
    }

    public enum ResponseType {
        NO_TRANSACTIONS_FOR_PERIOD(
                PROPER_MESSAGE_CONSTANTS + NO_TRANSACTIONS_FOR_PERIOD_STRING + END_OF_MESSAGE),
        PROPER_TRANSACTION_LIST(
                PROPER_MESSAGE_CONSTANTS + PROPER_TRANSACTION_LIST_STRING + END_OF_MESSAGE),
        ERROR_LIST(ERROR_MESSAGE_STRING);

        private final String message;

        ResponseType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
