package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BpceTestFixtures {

    public static final String SERVER_URL = "http://server-url";
    public static final String EXCHANGE_CODE = "exchange_code";
    public static final String ACCESS_TOKEN = "1234";
    public static final String REFRESH_TOKEN = "2345";
    public static final String SIGNATURE = "beef";
    public static final String RESOURCE_ID = "009988";
    public static final String CLIENT_ID = "cId";
    public static final String REDIRECT_URL = "http://redirect-url";
    private static final long TOKEN_EXPIRES_IN = 3600L;

    public static TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"access_token\":\""
                        + ACCESS_TOKEN
                        + "\",\n"
                        + "\"token_type\":\"Bearer\",\n"
                        + "\"expires_in\":"
                        + TOKEN_EXPIRES_IN
                        + ",\n"
                        + "\"refresh_token\":\""
                        + REFRESH_TOKEN
                        + "\",\n"
                        + "\"scope\":\"xx\",\n"
                        + "\"state\":\"abc\"\n"
                        + "}",
                TokenResponse.class);
    }

    public static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"accounts\": [\n"
                        + "    {\n"
                        + "      \"cashAccountType\": \"CACC\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"iban\": \"FR7613807008043001965409135\"\n"
                        + "      },\n"
                        + "      \"resourceId\": \""
                        + RESOURCE_ID
                        + "\",\n"
                        + "      \"product\": \"COMPTE COURANT\",\n"
                        + "      \"_links\": {},\n"
                        + "      \"usage\": \"ORGA\",\n"
                        + "      \"psuStatus\": \"Account Holder\",\n"
                        + "      \"name\": \"Account\",\n"
                        + "      \"bicFi\": \"CCBPFRPPNAN\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"details\": \"det\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    public static BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceType\": \"VALU\",\n"
                        + "      \"name\": \"Bal1\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4321.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-16\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"CLBD\",\n"
                        + "      \"name\": \"Bal2\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4179.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-15\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"OTHR\",\n"
                        + "      \"name\": \"Bal3\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4348.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                BalancesResponse.class);
    }

    public static TransactionsResponse getTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "\"transactions\":["
                        + "   {"
                        + "       \"transactionAmount\":{"
                        + "           \"currency\":\"EUR\","
                        + "           \"amount\":\"2113.31\""
                        + "       },"
                        + "       \"creditDebitIndicator\":\"DBIT\","
                        + "       \"status\":\"BOOK\","
                        + "       \"bookingDate\":\"2020-06-20\","
                        + "       \"remittanceInformation\":[\"DESCRIPTION_1\"]"
                        + "   },"
                        + "   {"
                        + "       \"transactionAmount\":{"
                        + "           \"currency\":\"EUR\","
                        + "           \"amount\":\"8.9\""
                        + "       },"
                        + "       \"creditDebitIndicator\":\"CRDT\","
                        + "       \"status\":\"BOOK\","
                        + "       \"bookingDate\":\"2020-06-19\","
                        + "       \"remittanceInformation\":[\"DESCRIPTION_2\"]"
                        + "   },"
                        + "   {"
                        + "       \"transactionAmount\":{"
                        + "           \"currency\":\"EUR\","
                        + "           \"amount\":\"3.49\""
                        + "       },"
                        + "       \"creditDebitIndicator\":\"DBIT\","
                        + "       \"status\":\"PDNG\","
                        + "       \"bookingDate\":\"2020-07-04\","
                        + "       \"transactionDate\":\"2020-06-20\","
                        + "       \"remittanceInformation\":[\"DESCRIPTION_3\"]"
                        + "   }"
                        + "],"
                        + "\"_links\":{"
                        + "   \"self\":{"
                        + "       \"href\":\"/stet/psd2/v1/accounts/009988/transactions\","
                        + "       \"templated\":true"
                        + "   },"
                        + "   \"balances\":{"
                        + "       \"href\":\"/stet/psd2/v1/accounts/009988/balances\","
                        + "       \"templated\":false"
                        + "   },"
                        + "   \"parent-list\":{"
                        + "       \"href\":\"/stet/psd2/v1/accounts\","
                        + "       \"templated\":false"
                        + "   }"
                        + "}}",
                TransactionsResponse.class);
    }

    public static TransactionalAccount getTransactionalAccount() {
        final String accountNo = "7613807008043001965409135";
        final String iban = "FR" + accountNo;
        final ExactCurrencyAmount exactCurrencyAmount =
                new ExactCurrencyAmount(BigDecimal.valueOf(10.0), "EUR");

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(exactCurrencyAmount))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(accountNo)
                                .withAccountName("account")
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(RESOURCE_ID)
                .build()
                .orElse(null);
    }
}
