package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BredBanquePopulaireTestFixtures {
    public static final String OAUTH_URL =
            "http://server-url/bred-auth-client/oauth/authorize?state=%s";
    public static final String REDIRECT_URL = "https://127.0.0.1:7357/api/v1/thirdparty/callback";
    public static final String RESOURCE_ID = "00000000000000EUR000000000";
    public static final String IBAN = "FR7613807008043001965409135";
    public static final String STATE = "DUMMY_STATE";
    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String OCP_APIM_KEY = "DUMMY_OCP_APIM_KEY";
    public static final String EXCHANGE_CODE = "DUMMY_EXCHANGE_CODE";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    public static final String TOKEN_TYPE = "Bearer";
    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String DIGEST = "SHA-256=63lNmcHVI2d0suMyvE/oSUPZQR1rXmidOdy3NZMmSzQ=";
    public static final String FULL_NAME = "SAMPLE NAME";
    public static final String KEY_ID = "key-id";
    public static final String SCHEME_NAME = "BRED";
    public static final String ISSUER = "BREDFRPPXXX";
    public static final String CURRENCY = "EUR";
    public static final long TOKEN_EXPIRES_IN = 3600L;

    public static AccountsResponse getAccountResponseWithoutConsent() {
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
                        + "      \"_links\": {},\n"
                        + "      \"usage\": \"PRIV\",\n"
                        + "      \"name\": \"SAMPLE NAME\",\n"
                        + "      \"bicFi\": \"BREDFRPPXXX\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    public static AccountsResponse getAccountResponseWithConsent() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"accounts\": [\n"
                        + "        {\n"
                        + "            \"resourceId\": \"00000000000000EUR000000000\",\n"
                        + "            \"bicFi\": \"BREDFRPPXXX\",\n"
                        + "            \"accountId\": {\n"
                        + "                \"iban\": \"FR7613807008043001965409135\"\n"
                        + "            },\n"
                        + "            \"name\": \"NAME\",\n"
                        + "            \"usage\": \"PRIV\",\n"
                        + "            \"cashAccountType\": \"CACC\",\n"
                        + "            \"currency\": \"EUR\",\n"
                        + "            \"balances\": [\n"
                        + "                {\n"
                        + "                    \"name\": \"Solde comptable au 21/01/2022\",\n"
                        + "                    \"balanceAmount\": {\n"
                        + "                        \"currency\": \"EUR\",\n"
                        + "                        \"amount\": \"-13.23\"\n"
                        + "                    },\n"
                        + "                    \"balanceType\": \"CLBD\"\n"
                        + "                }\n"
                        + "            ],\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"v1/accounts/00000000000000EUR000000000/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"v1/accounts/00000000000000EUR000000000/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"_links\": {\n"
                        + "        \"self\": {\n"
                        + "            \"href\": \"v1/accounts\"\n"
                        + "        }\n"
                        + "    }\n"
                        + "}",
                AccountsResponse.class);
    }

    public static BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"balances\": [\n"
                        + "        {\n"
                        + "            \"name\": \"Solde comptable au 20/01/2022\",\n"
                        + "            \"balanceAmount\": {\n"
                        + "                \"currency\": \"EUR\",\n"
                        + "                \"amount\": \"500.52\"\n"
                        + "            },\n"
                        + "            \"balanceType\": \"CLBD\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"_links\": {\n"
                        + "        \"self\": {\n"
                        + "            \"href\": \"v1/accounts/00000000000000EUR000000000/balances\"\n"
                        + "        }\n"
                        + "    }\n"
                        + "}",
                BalancesResponse.class);
    }

    public static TransactionResponse getTransactionsResponse(boolean isFirstRequest) {
        return isFirstRequest
                ? SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"transactions\": [\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000002021-12-14-03.09.43.626449\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"8.89\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"CRDT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"DEXT FRANCE 0000000\",\n"
                                + "                \"Virement SEPA reçu 0000000\",\n"
                                + "                \"Dext Expenses\",\n"
                                + "                null,\n"
                                + "                \"Dext Expenses\"\n"
                                + "            ]\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000012021-12-14-03.09.43.626449\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"-2.5\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"DBIT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"Frais tenue de compte 0000000\"\n"
                                + "            ]\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000022021-12-08-03.00.08.347524\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"-49.99\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"DBIT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"DEXT FRANCE 0000000\",\n"
                                + "                \"Virement SEPA reçu 0000000\",\n"
                                + "                \"Dext Expenses\",\n"
                                + "                null,\n"
                                + "                \"Dext Expenses\"            ]\n"
                                + "        }\n"
                                + "    ],\n"
                                + "    \"_links\": {\n"
                                + "        \"self\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=0\"\n"
                                + "        },\n"
                                + "        \"parent-list\": {\n"
                                + "            \"href\": \"v1/accounts\"\n"
                                + "        },\n"
                                + "        \"balances\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/balances\"\n"
                                + "        },\n"
                                + "        \"first\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=0\"\n"
                                + "        },\n"
                                + "        \"last\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=1\"\n"
                                + "        },\n"
                                + "        \"next\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=1\"\n"
                                + "        }\n"
                                + "    }\n"
                                + "}",
                        TransactionResponse.class)
                : SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"transactions\": [\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000032021-12-14-03.09.43.626449\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"8.89\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"CRDT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"DEXT FRANCE 0000000\",\n"
                                + "                \"Virement SEPA reçu 0000000\",\n"
                                + "                \"Dext Expenses\",\n"
                                + "                null,\n"
                                + "                \"Dext Expenses\"\n"
                                + "            ]\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000042021-12-14-03.09.43.626449\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"-2.5\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"DBIT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-12T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"Frais tenue de compte 0000000\"\n"
                                + "            ]\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"entryReference\": \"0000000052021-12-08-03.00.08.347524\",\n"
                                + "            \"transactionAmount\": {\n"
                                + "                \"currency\": \"EUR\",\n"
                                + "                \"amount\": \"-49.99\"\n"
                                + "            },\n"
                                + "            \"creditDebitIndicator\": \"DBIT\",\n"
                                + "            \"status\": \"BOOK\",\n"
                                + "            \"bookingDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"valueDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"transactionDate\": \"2021-12-06T23:00:00.000+0000\",\n"
                                + "            \"remittanceInformation\": [\n"
                                + "                \"DEXT FRANCE 0000000\",\n"
                                + "                \"Virement SEPA reçu 0000000\",\n"
                                + "                \"Dext Expenses\",\n"
                                + "                null,\n"
                                + "                \"Dext Expenses\"            ]\n"
                                + "        }\n"
                                + "    ],\n"
                                + "    \"_links\": {\n"
                                + "        \"self\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=1\"\n"
                                + "        },\n"
                                + "        \"parent-list\": {\n"
                                + "            \"href\": \"v1/accounts\"\n"
                                + "        },\n"
                                + "        \"balances\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/balances\"\n"
                                + "        },\n"
                                + "        \"first\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=0\"\n"
                                + "        },\n"
                                + "        \"last\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=1\"\n"
                                + "        },\n"
                                + "        \"prev\": {\n"
                                + "            \"href\": \"v1/accounts/00000000000000EUR000000000/transactions?page=0\"\n"
                                + "        }\n"
                                + "    }\n"
                                + "}",
                        TransactionResponse.class);
    }

    public static EndUserIdentityResponse getEndUserIdentityResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "\"connectedPsu\":\"" + FULL_NAME + "\"\n" + "}",
                EndUserIdentityResponse.class);
    }

    public static TrustedBeneficiariesResponseDto getEmptyTrustedBeneficiariesResponse() {
        return SerializationUtils.deserializeFromString(
                "{}", TrustedBeneficiariesResponseDto.class);
    }

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

    public static List<ConsentDataEntity> getConsentDataEntities() {
        final ConsentDataEntity consentDataEntity = new ConsentDataEntity(IBAN, RESOURCE_ID);
        return ImmutableList.of(consentDataEntity);
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
