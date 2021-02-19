package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class SearchTransactionsResponseTestData {

    static SearchTransactionsResponse getTestData() {
        return SerializationUtils.deserializeFromString(
                TEST_DATA, SearchTransactionsResponse.class);
    }

    static SearchTransactionsResponse getTestDataWithReservations() {
        return SerializationUtils.deserializeFromString(
                TEST_DATA_WITH_RESERVATIONS, SearchTransactionsResponse.class);
    }

    static SearchTransactionsResponse getTestEmptyData() {
        return SerializationUtils.deserializeFromString(
                EMPTY_TEST_DATA, SearchTransactionsResponse.class);
    }

    private static final String EMPTY_TEST_DATA =
            "{" + "  \"transactions\" : [ ]," + "  \"reservations\" : [ ]" + "}";

    private static final String TEST_DATA =
            "{"
                    + "\"transactions\": [{"
                    + "\"paymentDate\": \"2017-07-03\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-07-03\","
                    + "\"hostTms\": \"2017-07-03-21.21.44.358783\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-07-03\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-4711\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-47,11\","
                    + "\"localizedValueWithCurrency\": \"-47,11 SEK\","
                    + "\"value\": -4711,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-47,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-47 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-47 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-07-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-06-30\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-0,11\","
                    + "\"localizedValueWithCurrency\": \"-0,11 SEK\","
                    + "\"value\": -11,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-0,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-0 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-0 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-11\","
                    + "\"paymentDate\": \"2017-06-30\","
                    + "\"hostTms\": \"2017-06-30-23.34.19.679123\","
                    + "\"pstgWsId\": \"YIDS0149\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-06-30\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-811\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-8,11\","
                    + "\"localizedValueWithCurrency\": \"-8,11 SEK\","
                    + "\"value\": -811,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-8,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-8 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-8 SEK\""
                    + "},"
                    + "\"label\": \"Ränta på övertrassering\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-07-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-06-01\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-06-01\","
                    + "\"hostTms\": \"2017-06-01-20.58.39.026126\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-06-01\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-800\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-8,00\","
                    + "\"localizedValueWithCurrency\": \"-8,00 SEK\","
                    + "\"value\": -800,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-8,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-8 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-8 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-06-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-05-03\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"50,00\","
                    + "\"localizedValueWithCurrency\": \"50,00 SEK\","
                    + "\"value\": 5000,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"50,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"50 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"50 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"5000\","
                    + "\"paymentDate\": \"2017-05-03\","
                    + "\"hostTms\": \"2017-05-03-15.52.51.923065\","
                    + "\"pstgWsId\": \"ZIDS4123\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-05-03\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"3100\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"31,00\","
                    + "\"localizedValueWithCurrency\": \"31,00 SEK\","
                    + "\"value\": 3100,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"31,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"31 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"31 SEK\""
                    + "},"
                    + "\"label\": \"Insättning från annan bank ANDREAS HÅ\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-05-04\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-05-02\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-05-02\","
                    + "\"hostTms\": \"2017-05-02-20.56.22.170439\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-05-02\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-1900\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-19,00\","
                    + "\"localizedValueWithCurrency\": \"-19,00 SEK\","
                    + "\"value\": -1900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-19,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-19 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-19 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-05-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}],"
                    + "\"reservations\": null,"
                    + "\"totalReservationsAmount\": null"
                    + "}";

    private static final String TEST_DATA_WITH_RESERVATIONS =
            "{"
                    + "\"transactions\": [{"
                    + "\"paymentDate\": \"2017-07-03\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-07-03\","
                    + "\"hostTms\": \"2017-07-03-21.21.44.358783\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-07-03\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-4711\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-47,11\","
                    + "\"localizedValueWithCurrency\": \"-47,11 SEK\","
                    + "\"value\": -4711,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-47,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-47 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-47 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-07-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-06-30\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-0,11\","
                    + "\"localizedValueWithCurrency\": \"-0,11 SEK\","
                    + "\"value\": -11,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-0,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-0 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-0 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-11\","
                    + "\"paymentDate\": \"2017-06-30\","
                    + "\"hostTms\": \"2017-06-30-23.34.19.679123\","
                    + "\"pstgWsId\": \"YIDS0149\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-06-30\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-811\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-8,11\","
                    + "\"localizedValueWithCurrency\": \"-8,11 SEK\","
                    + "\"value\": -811,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-8,11 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-8 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-8 SEK\""
                    + "},"
                    + "\"label\": \"Ränta på övertrassering\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-07-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-06-01\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-06-01\","
                    + "\"hostTms\": \"2017-06-01-20.58.39.026126\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-06-01\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-800\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-8,00\","
                    + "\"localizedValueWithCurrency\": \"-8,00 SEK\","
                    + "\"value\": -800,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-8,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-8 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-8 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-06-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-05-03\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"50,00\","
                    + "\"localizedValueWithCurrency\": \"50,00 SEK\","
                    + "\"value\": 5000,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"50,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"50 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"50 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"5000\","
                    + "\"paymentDate\": \"2017-05-03\","
                    + "\"hostTms\": \"2017-05-03-15.52.51.923065\","
                    + "\"pstgWsId\": \"ZIDS4123\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-05-03\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"3100\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"31,00\","
                    + "\"localizedValueWithCurrency\": \"31,00 SEK\","
                    + "\"value\": 3100,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"31,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"31 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"31 SEK\""
                    + "},"
                    + "\"label\": \"Insättning från annan bank ANDREAS HÅ\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-05-04\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}, {"
                    + "\"paymentDate\": \"2017-05-02\","
                    + "\"amount\": {"
                    + "\"localizedValue\": \"-39,00\","
                    + "\"localizedValueWithCurrency\": \"-39,00 SEK\","
                    + "\"value\": -3900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-39,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-39 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-39 SEK\""
                    + "},"
                    + "\"entityKey\": {"
                    + "\"accountId\": \"9511.1231013123\","
                    + "\"agreementId\": \"123400696169123\","
                    + "\"refNumber\": \"1205361128\","
                    + "\"amount\": \"-3900\","
                    + "\"paymentDate\": \"2017-05-02\","
                    + "\"hostTms\": \"2017-05-02-20.56.22.170439\","
                    + "\"pstgWsId\": \"YGDS0169\","
                    + "\"pciHostTimeStamp\": \"0000-00-00-00.00.00.000000\","
                    + "\"dtCreate\": \"2017-05-02\","
                    + "\"ownText\": \"\","
                    + "\"balance\": \"-1900\","
                    + "\"ldbId\": \"09840\","
                    + "\"afrIdfr\": \"957066696169723\","
                    + "\"pccgHostTs\": \"\""
                    + "},"
                    + "\"balance\": {"
                    + "\"localizedValue\": \"-19,00\","
                    + "\"localizedValueWithCurrency\": \"-19,00 SEK\","
                    + "\"value\": -1900,"
                    + "\"scale\": 2,"
                    + "\"currency\": \"SEK\","
                    + "\"localizedValueWithCurrencyAtEnd\": \"-19,00 SEK\","
                    + "\"roundedAmountWithIsoCurrency\": \"-19 kr\","
                    + "\"roundedAmountWithCurrencySymbol\": \"-19 SEK\""
                    + "},"
                    + "\"label\": \"Avgift\","
                    + "\"originalText\": \"\","
                    + "\"clearingChoice\": \"Unknown\","
                    + "\"icon\": null,"
                    + "\"categoryLabel\": null,"
                    + "\"subCategoryLabel\": null,"
                    + "\"dueDate\": \"2017-05-01\","
                    + "\"crrfValue\": null,"
                    + "\"eerfValue\": null"
                    + "}],"
                    + "\"reservations\" : [ {"
                    + "      \"id\": \"1122411431159137333346\","
                    + "      \"type\": \"VPES\","
                    + "      \"amount\": {"
                    + "        \"localizedValue\": \"-33.00\","
                    + "        \"localizedValueWithCurrency\": \"-33.00 NOK\","
                    + "        \"value\": -3300,"
                    + "        \"scale\": 2,"
                    + "        \"currency\": \"NOK\","
                    + "        \"localizedValueWithCurrencyAtEnd\": \"-33.00 NOK\","
                    + "        \"roundedAmountWithIsoCurrency\": \"-NOK33\","
                    + "        \"roundedAmountWithCurrencySymbol\": \"-33 NOK\""
                    + "      },"
                    + "      \"calculatedAmount\": {"
                    + "        \"localizedValue\": \"9,405.82\","
                    + "        \"localizedValueWithCurrency\": \"9,405.82\","
                    + "        \"value\": 940582,"
                    + "        \"scale\": 2,"
                    + "        \"currency\": null,"
                    + "        \"localizedValueWithCurrencyAtEnd\": null,"
                    + "        \"roundedAmountWithIsoCurrency\": null,"
                    + "        \"roundedAmountWithCurrencySymbol\": null"
                    + "      },"
                    + "      \"expirationDate\": \"2017-12-05\","
                    + "      \"description\": \"Vipps by DnB/OSLO/NO\","
                    + "      \"hostId\": \"112358767144769\","
                    + "      \"createDate\": \"2017-11-30\","
                    + "      \"createTimestamp\": \"2017-11-30T09:03:46\","
                    + "      \"status\": \"ACTIVE\","
                    + "      \"validation\": \"UNKN\","
                    + "      \"labelValuePair\": ["
                    + "        {"
                    + "          \"label\": \"Amount\","
                    + "          \"value\": \"-33.00\""
                    + "        },"
                    + "        {"
                    + "          \"label\": \"Reservation number\","
                    + "          \"value\": \"1122411431159137333346\""
                    + "        },"
                    + "        {"
                    + "          \"label\": \"Created on\","
                    + "          \"value\": \"2017-11-30\""
                    + "        },"
                    + "        {"
                    + "          \"label\": \"Expiration date\","
                    + "          \"value\": \"2017-12-05\""
                    + "        },"
                    + "        {"
                    + "          \"label\": \"Status\","
                    + "          \"value\": \"aktiv\""
                    + "        }"
                    + "      ]"
                    + "} ],"
                    + "\"totalReservationsAmount\": null"
                    + "}";
}
