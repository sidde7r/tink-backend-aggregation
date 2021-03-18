package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

public class FabricAccountFetcherTest {

    @Test
    public void shouldConvertToTransferDestinationProperly() {
        String sourceBalance =
                "{\n"
                        + "  \"account\": {\n"
                        + "    \"iban\": \"IT60X0542811101000000123456\",\n"
                        + "    \"currency\": \"EUR\"\n"
                        + "  },\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"11.0\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"interimBooked\",\n"
                        + "      \"creditLimitIncluded\": false,\n"
                        + "      \"lastChangeDateTime\": null,\n"
                        + "      \"referenceDate\": \"2020-06-15\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"11.0\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"interimAvailable\",\n"
                        + "      \"creditLimitIncluded\": false,\n"
                        + "      \"lastChangeDateTime\": null,\n"
                        + "      \"referenceDate\": \"2020-06-15\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}";
        String sourceAccount =
                "{\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"iban\": \"IT60X0542811101000000123456\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"name\": \"Joe Doe\",\n"
                        + "      \"_links\": {\n"
                        + "        \"account\": {\n"
                        + "          \"href\": \"/v1/accounts/15259\"\n"
                        + "        },\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"/v1/accounts/15259/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"/v1/accounts/15259/transactions\"\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}";
        String accountDetialSource =
                "{\n"
                        + "  \"account\": {\n"
                        + "    \"resourceId\": \"15259\",\n"
                        + "    \"iban\": \"IT60X0542811101000000123456\",\n"
                        + "    \"currency\": \"EUR\",\n"
                        + "    \"name\": \"Joe Doe\",\n"
                        + "    \"usage\": \"ORGA\",\n"
                        + "    \"_links\": {\n"
                        + "      \"account\": {\n"
                        + "        \"href\": \"/v1/accounts/15259\"\n"
                        + "      },\n"
                        + "      \"balances\": {\n"
                        + "        \"href\": \"/v1/accounts/15259/balances\"\n"
                        + "      },\n"
                        + "      \"transactions\": {\n"
                        + "        \"href\": \"/v1/accounts/15259/transactions\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}";

        AccountResponse accountResponse =
                SerializationUtils.deserializeFromString(sourceAccount, AccountResponse.class);
        AccountDetailsResponse accountDetailsResponse =
                SerializationUtils.deserializeFromString(
                        accountDetialSource, AccountDetailsResponse.class);
        BalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(sourceBalance, BalanceResponse.class);
        FabricApiClient apiClient = mock(FabricApiClient.class);
        when(apiClient.fetchAccounts()).thenReturn(accountResponse);
        when(apiClient.getAccountDetails("/v1/accounts/15259")).thenReturn(accountDetailsResponse);
        when(apiClient.getBalances("/v1/accounts/15259/balances")).thenReturn(balanceResponse);
        FabricAccountFetcher fabricAccountFetcher = new FabricAccountFetcher(apiClient);

        FetchTransferDestinationsResponse dt =
                InferredTransferDestinations.forPaymentAccounts(
                        fabricAccountFetcher.fetchAccounts().stream()
                                .map(a -> a.toSystemAccount(new User(), new Provider()))
                                .collect(Collectors.toList()),
                        AccountIdentifierType.IBAN);
        Assert.assertEquals(1, dt.getTransferDestinations().size());
    }
}
