package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class ICSSerializationTest {

    private String accountSetupResponse;
    private String accountsResponse;
    private String balanceResponse;
    private String transactionsResponse;

    @Before
    public void setup(){
        this.accountSetupResponse = "{\n"
                + "  \"Data\": {\n"
                + "    \"AccountRequestId\": \"f2a188d6-5d7f-4b7e-bfef-928b14b0f43e\",\n"
                + "    \"Status\": \"AwaitingAuthorisation\",\n"
                + "    \"CreationDateTime\": \"2017-12-29T09:02:35+05:30\",\n"
                + "    \"Permissions\": [\n"
                + "      \"ReadAccountsBasic\",\n"
                + "      \"ReadAccountsDetail\",\n"
                + "      \"ReadBalances\",\n"
                + "      \"ReadTransactionsBasic\",\n"
                + "      \"ReadTransactionsCredits\",\n"
                + "      \"ReadTransactionsDebits\",\n"
                + "      \"ReadTransactionsDetail\"\n"
                + "    ],\n"
                + "    \"TransactionFromDate\": \"2016-05-03\",\n"
                + "    \"TransactionToDate\": \"2017-12-03\",\n"
                + "    \"ExpirationDate\": \"2019-12-03\"\n"
                + "  },\n"
                + "  \"Risk\": {},\n"
                + "  \"Links\": {\n"
                + "    \"Self\": \"/account-requests/f2a188d6-5d7f-4b7e-bfef-928b14b0f43e\"\n"
                + "  },\n"
                + "  \"Meta\": {\n"
                + "    \"TotalPages\": 1\n"
                + "  }\n"
                + "}\n";

        this.accountsResponse = "{\n"
                + "  \"Data\": {\n"
                + "    \"Account\": [\n"
                + "      {\n"
                + "        \"AccountId\": \"23660b73-460b-499f-ab7c-fc8f7572207c\",\n"
                + "        \"Currency\": \"EUR\",\n"
                + "        \"CreditCardAccountInfo\": {\n"
                + "          \"AccountType\": \"CreditCard\",\n"
                + "          \"CustomerNumber\": 12345670089,\n"
                + "          \"Active\": true\n"
                + "        },\n"
                + "        \"ProductInfo\": {\n"
                + "          \"ProductName\": \"ICS Visa World Card Gold\",\n"
                + "          \"ProductImage\": \"BASE64\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"AccountId\": \"bc662d85-521c-439c-9fc3-8619e783c66f\",\n"
                + "        \"Currency\": \"EUR\",\n"
                + "        \"CreditCardAccountInfo\": {\n"
                + "          \"AccountType\": \"CreditCard\",\n"
                + "          \"CustomerNumber\": 23456780090,\n"
                + "          \"Active\": true\n"
                + "        },\n"
                + "        \"ProductInfo\": {\n"
                + "          \"ProductName\": \"ICS Visa World Card Gold\",\n"
                + "          \"ProductImage\": \"BASE64\"\n"
                + "        }\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"Links\": {\n"
                + "    \"Self\": \"/accounts\"\n"
                + "  },\n"
                + "  \"Meta\": {\n"
                + "    \"TotalPages\": 1\n"
                + "  }\n"
                + "}";

        this.balanceResponse = "{\n"
                + "  \"Data\": {\n"
                + "    \"Balance\": [\n"
                + "      {\n"
                + "        \"AccountId\": \"23660b73-460b-499f-ab7c-fc8f7572207c\",\n"
                + "        \"CreditCardBalance\": {\n"
                + "          \"Amount\": \"1523.5\",\n"
                + "          \"Currency\": \"EUR\",\n"
                + "          \"AvailableLimit\": \"875.54\",\n"
                + "          \"AuthorizedBalance\": \"100.51\",\n"
                + "          \"CreditLimit\": \"2500.0\"\n"
                + "        },\n"
                + "        \"DateTime\": \"2017-11-25T00:00:00+00:00\",\n"
                + "        \"Active\": true\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"Links\": {\n"
                + "    \"Self\": \"/accounts/23660b73-460b-499f-ab7c-fc8f7572207c/balances\"\n"
                + "  },\n"
                + "  \"Meta\": {\n"
                + "    \"TotalPages\": 1\n"
                + "  }\n"
                + "}";

        this.transactionsResponse = "{\n"
                + "  \"Data\": {\n"
                + "    \"Transaction\": [\n"
                + "      {\n"
                + "        \"AccountId\": \"bcf218cd-286f-4ecd-a7fe-7a941c61fa97\",\n"
                + "        \"TransactionId\": \"ZzKJ5\",\n"
                + "        \"LastFourDigits\": \"1234\",\n"
                + "        \"IndicatorExtraCard\": \"Main Card\",\n"
                + "        \"CountryCode\": \"IDN\",\n"
                + "        \"TransactionDate\": \"2017-11-26\",\n"
                + "        \"BillingAmount\": \"177.54\",\n"
                + "        \"BillingCurrency\": \"EUR\",\n"
                + "        \"SourceAmount\": \"2784372.0\",\n"
                + "        \"SourceCurrency\": \"IDR\",\n"
                + "        \"EmbossingName\": \"A. JANSSENS\",\n"
                + "        \"ProcessingTime\": \"12:23:45\",\n"
                + "        \"CreditDebitIndicator\": \"Debit\",\n"
                + "        \"Status\": \"Booked\",\n"
                + "        \"TransactionInformation\": \"GYPSY DEN HO BADUNG ID\",\n"
                + "        \"MerchantDetails\": {\n"
                + "          \"MerchantName\": \"GYPSY DEN HO BADUNG ID\",\n"
                + "          \"MerchantCategoryCodeDescription\": \"Clock Stores\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"AccountId\": \"b2f46c81-3f63-4a76-b08b-1d589a23d9de\",\n"
                + "        \"TransactionId\": \"ZzKJ6\",\n"
                + "        \"LastFourDigits\": \"5678\",\n"
                + "        \"IndicatorExtraCard\": \"Extra Card\",\n"
                + "        \"CountryCode\": \"NLD\",\n"
                + "        \"TransactionDate\": \"2017-11-25\",\n"
                + "        \"BillingAmount\": \"-51.0\",\n"
                + "        \"BillingCurrency\": \"EUR\",\n"
                + "        \"SourceAmount\": \"-51.0\",\n"
                + "        \"SourceCurrency\": \"EUR\",\n"
                + "        \"EmbossingName\": \"M. JANSSENS\",\n"
                + "        \"TypeOfPurchase\": \"ONLINE\",\n"
                + "        \"ProcessingTime\": \"23:45:56\",\n"
                + "        \"CreditDebitIndicator\": \"Credit\",\n"
                + "        \"Status\": \"Booked\",\n"
                + "        \"TransactionInformation\": \"ADOBE CREATIVE CLOUD ADOBE.COM IE\",\n"
                + "        \"MerchantDetails\": {\n"
                + "          \"MerchantName\": \"ADOBE CREATIVE CLOUD ADOBE.COM IE\",\n"
                + "          \"MerchantCategoryCodeDescription\": \"Computer Software Stores\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"AccountId\": \"b2f46c81-3f63-4a76-b08b-1d589a23d9de\",\n"
                + "        \"TransactionId\": \"ZzKJ7\",\n"
                + "        \"LastFourDigits\": \"5678\",\n"
                + "        \"IndicatorExtraCard\": \"Extra Card\",\n"
                + "        \"CountryCode\": \"NL \",\n"
                + "        \"TransactionDate\": \"2017-11-24\",\n"
                + "        \"BillingAmount\": \"11.85\",\n"
                + "        \"BillingCurrency\": \"EUR\",\n"
                + "        \"SourceAmount\": \"11.85\",\n"
                + "        \"SourceCurrency\": \"EUR\",\n"
                + "        \"EmbossingName\": \"M. JANSSENS\",\n"
                + "        \"TypeOfPurchase\": \"ONLINE\",\n"
                + "        \"ProcessingTime\": \"22:41:56\",\n"
                + "        \"CreditDebitIndicator\": \"Debit\",\n"
                + "        \"Status\": \"Reserved\",\n"
                + "        \"TransactionInformation\": \"WIX.COM 54321678345 LU\",\n"
                + "        \"MerchantDetails\": {\n"
                + "          \"MerchantName\": \"WIX.COM 54321678345 LU \",\n"
                + "          \"MerchantCategoryCodeDescription\": \"Continuity/Subscription M\"\n"
                + "        }\n"
                + "      },\n"
                + "      {\n"
                + "        \"AccountId\": \"b2f46c81-3f63-4a76-b08b-1d589a23d9de\",\n"
                + "        \"TransactionId\": \"ZzKJ8\",\n"
                + "        \"TransactionDate\": \"2017-11-22\",\n"
                + "        \"BillingAmount\": \"-100.0\",\n"
                + "        \"BillingCurrency\": \"EUR\",\n"
                + "        \"SourceAmount\": \"-100.0\",\n"
                + "        \"SourceCurrency\": \"EUR\",\n"
                + "        \"CreditDebitIndicator\": \"Credit\",\n"
                + "        \"Status\": \"Booked\",\n"
                + "        \"TransactionInformation\": \"BETALING, DANK U\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"AccountId\": \"b2f46c81-3f63-4a76-b08b-1d589a23d9de\",\n"
                + "        \"TransactionId\": \"ZzKJ9\",\n"
                + "        \"TransactionDate\": \"2017-11-21\",\n"
                + "        \"BillingAmount\": \"-302.52\",\n"
                + "        \"BillingCurrency\": \"EUR\",\n"
                + "        \"SourceAmount\": \"-302.52\",\n"
                + "        \"SourceCurrency\": \"EUR\",\n"
                + "        \"CreditDebitIndicator\": \"Credit\",\n"
                + "        \"Status\": \"Pending\",\n"
                + "        \"TransactionInformation\": \"Incasso jan 2016 betreffende uw creditcard   ICS-klantnummer 12345670089\"\n"
                + "      }\n"
                + "    ]\n"
                + "  },\n"
                + "  \"Links\": {\n"
                + "    \"Self\": \"/accounts/bcf218cd-286f-4ecd-a7fe-7a941c61fa97/transactions\"\n"
                + "  },\n"
                + "  \"Meta\": {}\n"
                + "}";
    }

    @Test
    public void serializeAccountSetupResponse(){
        AccountSetupResponse res = SerializationUtils.deserializeFromString(this.accountSetupResponse, AccountSetupResponse.class);
        Assert.assertNotNull(res);
    }

    @Test
    public void serializeAccountsResponse(){
        CreditAccountsResponse res = SerializationUtils.deserializeFromString(this.accountsResponse, CreditAccountsResponse.class);
        Assert.assertNotNull(res);
    }

    @Test
    public void serializeBalanceResponse(){
        CreditBalanceResponse res = SerializationUtils.deserializeFromString(this.balanceResponse, CreditBalanceResponse.class);
        Assert.assertNotNull(res);
    }

    @Test
    public void serializeTransactionsResponse(){
        CreditTransactionsResponse res = SerializationUtils.deserializeFromString(this.transactionsResponse, CreditTransactionsResponse.class);
        Assert.assertNotNull(res);
    }
}
