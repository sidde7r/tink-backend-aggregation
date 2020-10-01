package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.SebTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebAccountPaymentCapabilityUtil;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public class SebAccountPaymentCapabilityUtilTest {

    private static final String ACCOUNT_DATA =
            "{\n"
                    + "  \"accounts\": [{\n"
                    + "    \"resourceId\": \"d21f0798-11c1-446f-9fc2-43d30dd4f43d\",\n"
                    + "    \"iban\": \"SE1111111111111111111111\",\n"
                    + "    \"bban\": \"11111111111\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Privatkonto\",\n"
                    + "    \"name\": \"Gemensamt räkningskonto\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"2000.5\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"2000.5\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/d21f0798-11c1-446f-9fc2-43d30dd4f43d/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"resourceId\": \"09e8f041-648b-4cd3-9583-4dfba69c087f\",\n"
                    + "    \"iban\": \"SE2222222222222222222222\",\n"
                    + "    \"bban\": \"22222222222\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Personallönekonto\",\n"
                    + "    \"name\": \"Lön\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"905.500\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"905.50\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/09e8f041-648b-4cd3-9583-4dfba69c087f/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"resourceId\": \"b27923ef-9b44-47dd-933b-9c3725326ac7\",\n"
                    + "    \"iban\": \"SE3333333333333333333333\",\n"
                    + "    \"bban\": \"33333333333\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Enkla sparkontot\",\n"
                    + "    \"name\": \"Spar\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.000\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.00\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/b27923ef-9b44-47dd-933b-9c3725326ac7/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"resourceId\": \"b27923ef-9b44-47dd-933b-9c3725326ac7\",\n"
                    + "    \"iban\": \"SE4444444444444444444444\",\n"
                    + "    \"bban\": \"44444444444\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "      \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Valutakonto\",\n"
                    + "    \"name\": \"Valutakonto\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.000\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.00\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/b27923ef-9b44-47dd-933b-9c3725326ac7/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"resourceId\": \"b27923ef-9b44-47dd-933b-9c3725326ac7\",\n"
                    + "    \"iban\": \"SE5555555555555555555555\",\n"
                    + "    \"bban\": \"55555555555\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Notariatkonto\",\n"
                    + "    \"name\": \"Notariatkonto\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.000\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.00\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/b27923ef-9b44-47dd-933b-9c3725326ac7/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"resourceId\": \"b27923ef-9b44-47dd-933b-9c3725326ac7\",\n"
                    + "    \"iban\": \"SE6666666666666666666666\",\n"
                    + "    \"bban\": \"66666666666\",\n"
                    + "    \"status\": \"enabled\",\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"ownerName\": \"Name Surname\",\n"
                    + "    \"creditLine\": \"0.000\",\n"
                    + "    \"product\": \"Unknown product\",\n"
                    + "    \"name\": \"Unknown product\",\n"
                    + "    \"balances\": [{\n"
                    + "      \"balanceType\": \"interimAvailable\",\n"
                    + "      \"creditLimitIncluded\": true,\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.000\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }, {\n"
                    + "      \"balanceType\": \"interimBooked\",\n"
                    + "      \"balanceAmount\": {\n"
                    + "        \"amount\": \"0.00\",\n"
                    + "        \"currency\": \"SEK\"\n"
                    + "      }\n"
                    + "    }],\n"
                    + "    \"_links\": {\n"
                    + "      \"transactions\": {\n"
                    + "        \"href\": \"/accounts/b27923ef-9b44-47dd-933b-9c3725326ac7/transactions?bookingStatus=booked\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }]\n"
                    + "}";

    @Test
    public void testAccountToPaymentCapabilityMapping() {
        FetchAccountResponse accountResponse =
                SerializationUtils.deserializeFromString(ACCOUNT_DATA, FetchAccountResponse.class);
        SebTransactionalAccountFetcher fetcher = Mockito.mock(SebTransactionalAccountFetcher.class);
        Mockito.when(fetcher.fetchAccounts()).thenReturn(accountResponse.toTinkAccounts());
        List<Account> accounts =
                fetcher.fetchAccounts().stream()
                        .map(account -> account.toSystemAccount(createUser(), createProvider()))
                        .collect(Collectors.toList());
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();
        accounts.forEach(
                account ->
                        transferDestinations.put(
                                account,
                                SebAccountPaymentCapabilityUtil
                                        .inferTransferDestinationFromAccountProductType(
                                                account, createStorage())));
        FetchTransferDestinationsResponse response =
                new FetchTransferDestinationsResponse(transferDestinations);
        assertThat(response.getTransferDestinations().size()).isEqualTo(6);

        List<TransferDestinationPattern> checkingAccountPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "11111111111"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 11111111111"));
        assertThat(checkingAccountPatterns.size()).isEqualTo(4);

        List<TransferDestinationPattern> salaryAccountPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "22222222222"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 22222222222"));
        assertThat(salaryAccountPatterns.size()).isEqualTo(4);

        List<TransferDestinationPattern> savingsAccountPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "33333333333"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 33333333333"));
        assertThat(savingsAccountPatterns.size()).isEqualTo(2);

        List<TransferDestinationPattern> currencyAccountPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "44444444444"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 44444444444"));
        assertThat(currencyAccountPatterns.size()).isEqualTo(2);

        List<TransferDestinationPattern> notariatAccountPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "55555555555"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 55555555555"));
        assertThat(notariatAccountPatterns.size()).isEqualTo(0);

        List<TransferDestinationPattern> unknownProductPatterns =
                response.getTransferDestinations().entrySet().stream()
                        .filter(
                                entry ->
                                        Objects.equals(
                                                entry.getKey().getAccountNumber(), "66666666666"))
                        .map(Entry::getValue)
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing expected account with account number 66666666666"));
        assertThat(unknownProductPatterns.size()).isEqualTo(0);
    }

    private static User createUser() {
        User user = new User();
        user.setId("123");
        user.setProfile(new UserProfile());
        user.setFlags(Collections.emptyList());

        return user;
    }

    private Provider createProvider() {
        Provider p = new Provider();
        p.setClassName("someClassName");
        p.setName("PROVIDER_NAME");
        p.setMarket("SE");
        return p;
    }

    private Storage createStorage() {
        Storage storage = new Storage();
        Map<String, String> accountProductMap = new HashMap<>();
        accountProductMap.put("11111111111", "Privatkonto");
        accountProductMap.put("22222222222", "Privatkonto");
        accountProductMap.put("33333333333", "Enkla sparkontot");
        accountProductMap.put("44444444444", "Valutakonto");
        accountProductMap.put("55555555555", "Notariatkonto");
        accountProductMap.put("66666666666", "Unknown product");

        storage.put(SebConstants.Storage.ACCOUNT_PRODUCT_MAP, accountProductMap);
        return storage;
    }
}
