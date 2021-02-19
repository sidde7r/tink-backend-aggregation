package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc.FiTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class ResponseTestData {
    static final AccountsResponse ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\": [\n"
                            + "        {\n"
                            + "            \"_links\": {\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/ee02d6d8-6225-467d-bc69-a0dc03894642/transactions\"\n"
                            + "                }\n"
                            + "            },\n"
                            + "            \"accountId\": \"ee02d6d8-6225-467d-bc69-a0dc03894642\",\n"
                            + "            \"bic\": \"HANDFIHH\",\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"iban\": \"FI1234123412341234\",\n"
                            + "            \"name\": \"DUMMY USER\"\n"
                            + "        }\n"
                            + "    ]\n"
                            + "}",
                    AccountsResponse.class);

    static final BalanceAccountResponse BALANCE_ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\": {\n"
                            + "        \"transactions\": {\n"
                            + "            \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/ee02d6d8-6225-467d-bc69-a0dc03894642/transactions\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"accountId\": \"ee02d6d8-6225-467d-bc69-a0dc03894642\",\n"
                            + "    \"balances\": [\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 6964.34,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"balanceType\": \"AVAILABLE_AMOUNT\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"bic\": \"HANDFIHH\",\n"
                            + "    \"currency\": \"EUR\",\n"
                            + "    \"iban\": \"FI1234123412341234\",\n"
                            + "    \"ownerName\": \"DUMMY USER\"\n"
                            + "}",
                    BalanceAccountResponse.class);

    static final BalanceAccountResponse EMPTY_BALANCE_ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\": {\n"
                            + "        \"transactions\": {\n"
                            + "            \"href\": \"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/ee02d6d8-6225-467d-bc69-a0dc03894642/transactions\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"accountId\": \"ee02d6d8-6225-467d-bc69-a0dc03894642\",\n"
                            + "    \"balances\": [\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 6964.34,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"balanceType\": \"NOT_EXISTING_BALANCE\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"bic\": \"HANDFIHH\",\n"
                            + "    \"currency\": \"EUR\",\n"
                            + "    \"iban\": \"FI1234123412341234\",\n"
                            + "    \"ownerName\": \"DUMMY USER\"\n"
                            + "}",
                    BalanceAccountResponse.class);

    static final FiTransactionResponse TRANSACTION_DEBITED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"transactions\": [\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2021-01-25\",\n"
                            + "            \"creditDebit\": \"DEBITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-31.12.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2020-12-23\",\n"
                            + "            \"creditDebit\": \"DEBITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-30.11.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2020-11-23\",\n"
                            + "            \"creditDebit\": \"DEBITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-31.10.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        }\n"
                            + "    ]\n"
                            + "}\n",
                    FiTransactionResponse.class);

    static final FiTransactionResponse TRANSACTION_CREDITED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"transactions\": [\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2021-01-25\",\n"
                            + "            \"creditDebit\": \"CREDITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-31.12.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2020-12-23\",\n"
                            + "            \"creditDebit\": \"CREDITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-30.11.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"amount\": {\n"
                            + "                \"content\": 3,\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"bookingDate\": \"2020-11-23\",\n"
                            + "            \"creditDebit\": \"CREDITED\",\n"
                            + "            \"creditorName\": \"DUMMY NAME\",\n"
                            + "            \"remittanceInformation\": \"DUMMY NAME 1.-31.10.2020\",\n"
                            + "            \"status\": \"BOOKED\"\n"
                            + "        }\n"
                            + "    ]\n"
                            + "}\n",
                    FiTransactionResponse.class);
}
