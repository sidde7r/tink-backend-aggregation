package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SerializationTest {

    @Test
    public void testSerializeAccounts() {
        String json =
                "{\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"resourceId\": \"EX09999999\",\n"
                        + "      \"iban\": \"AT611904300234573201\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"name\": \"Main Account\",\n"
                        + "      \"product\": \"Main Product\",\n"
                        + "      \"cashAccountType\": \"CurrentAccount\",\n"
                        + "      \"bic\": \"DABAIE2D\",\n"
                        + "      \"balances\": [\n"
                        + "        {\n"
                        + "          \"balanceType\": \"interimBooked\",\n"
                        + "          \"balanceAmount\": {\n"
                        + "            \"amount\": 67.01592826,\n"
                        + "            \"currency\": \"EUR\"\n"
                        + "          },\n"
                        + "          \"referenceDate\": \"2017-10-25T00:00:00.000Z\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"_links\": {\n"
                        + "        \"balances\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances\",\n"
                        + "        \"transactions\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}";

        AccountsResponse response =
                SerializationUtils.deserializeForLogging(json, AccountsResponse.class)
                        .orElseThrow(() -> new IllegalStateException("Serialization error"));
        Collection<TransactionalAccount> accounts = response.toTransactionalAccounts();
    }

    @Test
    public void testSerializeTransactions() {
        String json =
                "                                     {\n"
                        + "  \"transactions\": {\n"
                        + "    \"booked\": [\n"
                        + "      {\n"
                        + "        \"transactionId\": \"123000000\",\n"
                        + "        \"endToEndId\": \"430780838838272\",\n"
                        + "        \"mandateId\": \"2/19/2086\",\n"
                        + "        \"bookingDate\": \"2018-01-01T00:00:00.000Z\",\n"
                        + "        \"valueDate\": \"2018-01-02T00:00:00.000Z\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": 71.71680331,\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"exchangeRate\": [\n"
                        + "          {\n"
                        + "            \"currencyFrom\": \"RON\",\n"
                        + "            \"rateFrom\": 0.301,\n"
                        + "            \"currencyTo\": \"EUR\",\n"
                        + "            \"rateTo\": 0.241,\n"
                        + "            \"rateDate\": \"2018-01-01T00:00:00.000Z\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"creditorName\": \"36684632133426\",\n"
                        + "        \"creditorAccount\": {\n"
                        + "          \"accountNumber\": \"99999/9999\",\n"
                        + "          \"iban\": \"AT611904300234573201\"\n"
                        + "        },\n"
                        + "        \"ultimateCreditor\": \"6334406611374771\",\n"
                        + "        \"debtorName\": \"May Cortez\",\n"
                        + "        \"debtorAccount\": {\n"
                        + "          \"accountNumber\": \"99999/9999\",\n"
                        + "          \"iban\": \"AT611904300234573201\"\n"
                        + "        },\n"
                        + "        \"ultimateDebtor\": \"vigej\",\n"
                        + "        \"remittanceInformationUnstructured\": \"ficnefrewiudpowembetiezresileormojweheusilerenagobbupuivigavoshepusadfetpoglavzalzemaezojmudesuvadulisobuvomibmumordinawetehkuvalo\",\n"
                        + "        \"remittanceInformationStructured\": \"lowtepculopowuhiridapcidefaehodalawocandefelotavdojumijodujfaluwtamjujtutarisuzcetatitu\",\n"
                        + "        \"purposeCode\": \"rofwet\",\n"
                        + "        \"bankTransactionCode\": \"tiec\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"pending\": [\n"
                        + "      {\n"
                        + "        \"transactionId\": \"123000000\",\n"
                        + "        \"endToEndId\": \"275887285075968\",\n"
                        + "        \"mandateId\": \"5/27/2029\",\n"
                        + "        \"bookingDate\": \"2018-01-01T00:00:00.000Z\",\n"
                        + "        \"valueDate\": \"2018-01-02T00:00:00.000Z\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": 32.53961215,\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"exchangeRate\": [\n"
                        + "          {\n"
                        + "            \"currencyFrom\": \"RON\",\n"
                        + "            \"rateFrom\": 0.301,\n"
                        + "            \"currencyTo\": \"EUR\",\n"
                        + "            \"rateTo\": 0.241,\n"
                        + "            \"rateDate\": \"2018-01-01T00:00:00.000Z\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"creditorName\": \"3528030606988453\",\n"
                        + "        \"creditorAccount\": {\n"
                        + "          \"accountNumber\": \"99999/9999\",\n"
                        + "          \"iban\": \"AT611904300234573201\"\n"
                        + "        },\n"
                        + "        \"ultimateCreditor\": \"5174658523047789\",\n"
                        + "        \"debtorName\": \"Garrett Holloway\",\n"
                        + "        \"debtorAccount\": {\n"
                        + "          \"accountNumber\": \"99999/9999\",\n"
                        + "          \"iban\": \"AT611904300234573201\"\n"
                        + "        },\n"
                        + "        \"ultimateDebtor\": \"wutjacma\",\n"
                        + "        \"remittanceInformationUnstructured\": \"sihuhhefetejergolegotorwifmipizmovudaibolajivjolunazbuwamjutilfihentehhuutjehijcavudo\",\n"
                        + "        \"remittanceInformationStructured\": \"vinmoddujalulokzutsigoclohiebafecavmawmeiszakurizacobulericidzonpihfiti\",\n"
                        + "        \"purposeCode\": \"kikehe\",\n"
                        + "        \"bankTransactionCode\": \"bapemn\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"_links\": [\n"
                        + "      {\n"
                        + "        \"viewAccount\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f\",\n"
                        + "        \"firstPage\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions?page=0\",\n"
                        + "        \"secondPage\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions?page=1\",\n"
                        + "        \"currentPage\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions?page=3\",\n"
                        + "        \"nextPage\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions?page=4\",\n"
                        + "        \"lastPage\": \"/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions?page=10\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}";

        TransactionsResponse response =
                SerializationUtils.deserializeForLogging(json, TransactionsResponse.class)
                        .orElseThrow(() -> new IllegalStateException("Serialization error"));
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
    }
}
