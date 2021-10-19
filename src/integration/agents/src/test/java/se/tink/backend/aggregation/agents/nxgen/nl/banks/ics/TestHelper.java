package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import java.math.BigDecimal;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ErrorBody;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.BalanceDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestHelper {

    public static final String ACCOUNT_ID = "b25f3b89-76a5-4ff7-81cd-35d5075353f6";
    public static final String CARD_NUMBER = "73297630011";
    public static final String TEST_MESSAGE = "testMessage";
    public static final String STATE = "00000000-0000-4000-0000-000000000000";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    public static CreditTransactionsResponse getCreditTransactionResponseWithEmptyData() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {},\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/accounts/b25f3b89-76a5-4ff7-81cd-35d5075353f6/transactions\"\n"
                        + "    },\n"
                        + "    \"Meta\": {}\n"
                        + "}",
                CreditTransactionsResponse.class);
    }

    public static CreditTransactionsResponse getCreditTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "        \"Transaction\": [\n"
                        + "            {\n"
                        + "                \"AccountId\": \"b25f3b89-76a5-4ff7-81cd-35d5075353f6\",\n"
                        + "                \"TransactionId\": \"46186526272\",\n"
                        + "                \"CountryCode\": \"   \",\n"
                        + "                \"TransactionDate\": \"1992-01-31\",\n"
                        + "                \"BillingAmount\": \"-204.64\",\n"
                        + "                \"BillingCurrency\": \"EUR\",\n"
                        + "                \"SourceAmount\": \"-204.64\",\n"
                        + "                \"SourceCurrency\": \"EUR\",\n"
                        + "                \"EmbossingName\": \"P.C.A. ROSIER\",\n"
                        + "                \"CreditDebitIndicator\": \"Credit\",\n"
                        + "                \"Status\": \"Booked\",\n"
                        + "                \"TransactionInformation\": \"IDEAL BETALING, DANK U\"\n"
                        + "            },\n"
                        + "            {\n"
                        + "                \"AccountId\": \"b25f3b89-76a5-4ff7-81cd-35d5075353f6\",\n"
                        + "                \"TransactionId\": \"46156926595\",\n"
                        + "                \"LastFourDigits\": \"7459\",\n"
                        + "                \"IndicatorExtraCard\": \"Main Card\",\n"
                        + "                \"CountryCode\": \"LUX\",\n"
                        + "                \"TransactionDate\": \"1992-01-21\",\n"
                        + "                \"BillingAmount\": \"1.36\",\n"
                        + "                \"BillingCurrency\": \"EUR\",\n"
                        + "                \"SourceAmount\": \"1.36\",\n"
                        + "                \"SourceCurrency\": \"EUR\",\n"
                        + "                \"EmbossingName\": \"P.C.A. ROSIER\",\n"
                        + "                \"TypeOfPurchase\": \"ONLINE\",\n"
                        + "                \"ProcessingTime\": \"09:15:27\",\n"
                        + "                \"CreditDebitIndicator\": \"Debit\",\n"
                        + "                \"Status\": \"Booked\",\n"
                        + "                \"TransactionInformation\": \"Januszpol retail stor\",\n"
                        + "                \"MerchantDetails\": {\n"
                        + "                    \"MerchantName\": \"Januszpol retail stor\",\n"
                        + "                    \"MerchantCategoryCodeDescription\": \"Miscellaneous Retail Stor\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        ]\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/accounts/b25f3b89-76a5-4ff7-81cd-35d5075353f6/transactions\"\n"
                        + "    },\n"
                        + "    \"Meta\": {}\n"
                        + "}",
                CreditTransactionsResponse.class);
    }

    public static CreditAccountsResponse getEmptyCreditAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "    }\n"
                        + "}",
                CreditAccountsResponse.class);
    }

    public static CreditTransactionsResponse getEmptyCreditTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "    }\n"
                        + "}",
                CreditTransactionsResponse.class);
    }

    public static CreditAccountsResponse getCreditAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "        \"Account\": [\n"
                        + "            {\n"
                        + "                \"AccountId\": \"b25f3b89-76a5-4ff7-81cd-35d5075353f6\",\n"
                        + "                \"Currency\": \"EUR\",\n"
                        + "                \"CreditCardAccountInfo\": {\n"
                        + "                    \"AccountType\": \"CreditCard\",\n"
                        + "                    \"CustomerNumber\": 73297630011,\n"
                        + "                    \"Active\": true\n"
                        + "                },\n"
                        + "                \"ProductInfo\": {\n"
                        + "                    \"ProductName\": \"Knab Creditcard\",\n"
                        + "                    \"ProductImage\": \"data:image/\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        ]\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/accounts\"\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "        \"TotalPages\": 1\n"
                        + "    }\n"
                        + "}\n",
                CreditAccountsResponse.class);
    }

    public static CreditBalanceResponse getCreditBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "        \"Balance\": [\n"
                        + "            {\n"
                        + "                \"AccountId\": \"b25f3b89-76a5-4ff7-81cd-35d5075353f6\",\n"
                        + "                \"CreditCardBalance\": {\n"
                        + "                    \"Amount\": \"54.77\",\n"
                        + "                    \"Currency\": \"EUR\",\n"
                        + "                    \"AvailableLimit\": \"10\",\n"
                        + "                    \"AuthorizedBalance\": \"0.0\",\n"
                        + "                    \"CreditLimit\": \"1000.0\"\n"
                        + "                },\n"
                        + "                \"DateTime\": \"2021-04-10T21:17:27+02:00\",\n"
                        + "                \"Active\": true\n"
                        + "            }\n"
                        + "        ]\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/accounts/b25f3b89-76a5-4ff7-81cd-35d5075353f6/balances\"\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "        \"TotalPages\": 1\n"
                        + "    }\n"
                        + "}\n",
                CreditBalanceResponse.class);
    }

    public static ICSConfiguration getIcsConfiguration() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"clientId\": \"dummyId\",\n"
                        + "    \"clientSecret\": \"dummySecret\""
                        + "}",
                ICSConfiguration.class);
    }

    public static ICSConfiguration getIcsConfigurationWithoutClientId() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "    \"clientId\": null,\n" + "    \"clientSecret\": \"dummySecret\"" + "}",
                ICSConfiguration.class);
    }

    public static ICSConfiguration getIcsConfigurationWithoutClientSecret() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "    \"clientId\": \"dummyId\",\n" + "    \"clientSecret\": null" + "}",
                ICSConfiguration.class);
    }

    public static AccountSetupResponse getAccountSetupResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "        \"AccountRequestId\": \"aea79c15-8f98-4e9b-97bf-858b47e8f9ff\",\n"
                        + "        \"Status\": \"AwaitingAuthorisation\",\n"
                        + "        \"CreationDateTime\": \"2021-04-10T19:16:46+00:00\",\n"
                        + "        \"Permissions\": [\n"
                        + "            \"ReadAccountsBasic\",\n"
                        + "            \"ReadAccountsDetail\",\n"
                        + "            \"ReadBalances\",\n"
                        + "            \"ReadTransactionsBasic\",\n"
                        + "            \"ReadTransactionsCredits\",\n"
                        + "            \"ReadTransactionsDebits\",\n"
                        + "            \"ReadTransactionsDetail\"\n"
                        + "        ],\n"
                        + "        \"ExpirationDate\": \"1993-07-08\",\n"
                        + "        \"TransactionFromDate\": \"1990-04-10\",\n"
                        + "        \"TransactionToDate\": \"1993-07-08\"\n"
                        + "    },\n"
                        + "    \"Risk\": {},\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/account-requests/aea79c15-8f98-4e9b-97bf-858b47e8f9ff\"\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "        \"TotalPages\": 1\n"
                        + "    }\n"
                        + "}\n",
                AccountSetupResponse.class);
    }

    public static ClientCredentialTokenResponse getClientCredentialTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"access_token\": \"accessToken\",\n"
                        + "    \"expires_in\": 299,\n"
                        + "    \"tppInformation\": {\n"
                        + "        \"tppLegalEntityName\": \"Grip\",\n"
                        + "        \"tppRegisteredId\": \"ABN AMRO\",\n"
                        + "        \"tppRoles\": [\n"
                        + "            \"AISP\"\n"
                        + "        ]\n"
                        + "    },\n"
                        + "    \"jti\": \"9699bf55-7940-45ec-8150-b378d00990e0\"\n"
                        + "}\n",
                ClientCredentialTokenResponse.class);
    }

    public static ErrorBody getError(String message) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"error\": \""
                        + message
                        + "\",\n"
                        + "    \"error_description\": \"invalid_token\"\n"
                        + "}\n",
                ErrorBody.class);
    }

    public static BalanceDataEntity getBalanceDataEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Balance\": [\n"
                        + "        {\n"
                        + "            \"AccountId\": \"b25f3b89-76a5-4ff7-81cd-35d5075353f6\",\n"
                        + "            \"CreditCardBalance\": {\n"
                        + "                \"Amount\": \"54.77\",\n"
                        + "                \"Currency\": \"EUR\",\n"
                        + "                \"AvailableLimit\": \"10\",\n"
                        + "                \"AuthorizedBalance\": \"0.0\",\n"
                        + "                \"CreditLimit\": \"1000.0\"\n"
                        + "            },\n"
                        + "            \"DateTime\": \"2021-04-10T21:17:27+02:00\",\n"
                        + "            \"Active\": true\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}\n",
                BalanceDataEntity.class);
    }

    public static CreditBalanceResponse getEmptyCreditBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Data\": {\n"
                        + "        \"Balance\": [\n"
                        + "            {\n"
                        + "                \"AccountId\": \"dummyId\",\n"
                        + "                \"CreditCardBalance\": {\n"
                        + "                    \"Amount\": \"54.77\",\n"
                        + "                    \"Currency\": \"EUR\",\n"
                        + "                    \"AvailableLimit\": \"10\",\n"
                        + "                    \"AuthorizedBalance\": \"0.0\",\n"
                        + "                    \"CreditLimit\": \"1000.0\"\n"
                        + "                },\n"
                        + "                \"DateTime\": \"2021-04-10T21:17:27+02:00\",\n"
                        + "                \"Active\": true\n"
                        + "            }\n"
                        + "        ]\n"
                        + "    },\n"
                        + "    \"Links\": {\n"
                        + "        \"Self\": \"/accounts/b25f3b89-76a5-4ff7-81cd-35d5075353f6/balances\"\n"
                        + "    },\n"
                        + "    \"Meta\": {\n"
                        + "        \"TotalPages\": 1\n"
                        + "    }\n"
                        + "}\n",
                CreditBalanceResponse.class);
    }

    public static CreditCardAccount getCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(CARD_NUMBER)
                                .withBalance(createBalance())
                                .withAvailableCredit(createAvailableCredit())
                                .withCardAlias("Knab Creditcard")
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(CARD_NUMBER)
                                .withAccountNumber(CARD_NUMBER)
                                .withAccountName("Knab Creditcard")
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.OTHER, CARD_NUMBER))
                                .build())
                .setBankIdentifier(ACCOUNT_ID)
                .setApiIdentifier(ACCOUNT_ID)
                .addHolderName("")
                .build();
    }

    private static ExactCurrencyAmount createAvailableCredit() {
        return new ExactCurrencyAmount(new BigDecimal("10.0"), "EUR");
    }

    private static ExactCurrencyAmount createBalance() {
        return new ExactCurrencyAmount(new BigDecimal("-990.0"), "EUR");
    }
}
