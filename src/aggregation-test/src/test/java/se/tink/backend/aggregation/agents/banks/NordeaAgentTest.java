package se.tink.backend.aggregation.agents.banks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgent;
import se.tink.backend.aggregation.agents.banks.nordea.Session;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.CardDetailsEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.InitialContextResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings.CustodyAccount;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.helper.transfer.stubs.TransferStub;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.social.security.TestSSN;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class NordeaAgentTest extends AbstractAgentTest<NordeaAgent> {
    private List<String> featureFlags;
    public NordeaAgentTest() {
        super(NordeaAgent.class);
    }

    @Override
    protected Provider constructProvider() {
        Provider p = new Provider();
        p.setMarket("SE");
        return p;
    }

    @Override
    protected List<String> constructFeatureFlags() {
        return featureFlags;
    }

    @Before
    public void setup() {
        this.featureFlags = Lists.newArrayList();
    }

    @Test
    public void testUser1() throws Exception {
        testAgent("198701080312", "1320", "ENTER_DEVICE_TOKEN_HERE");
        //testAgent("198701080312", "1320", CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testErikPetterssonWithMobilebankId() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        testAgent(TestSSN.EP, "9682", CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testErikPetterssonWithMobilebankIdPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.EP);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testPayment() throws Exception {
        Transfer t = new Transfer();
        t.setType(TransferType.PAYMENT);
        t.setAmount(Amount.inSEK(2.2));
        t.setSource(new SwedishIdentifier(TestAccount.NORDEASSN_JK));
        t.setDestination(new BankGiroIdentifier("7308596"));
        t.setDestinationMessage("37578468440200775");
        t.setSourceMessage("AmEx test2");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.JK, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    /**
     * Requires unsigned eInvoices at Nordea
     */
    @Test
    public void testSignEInvoice() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        List<Transfer> transfers = fetchEInvoices(TestSSN.EP, "Ask EP");
        assertThat(transfers.size()).isGreaterThan(0);

        Transfer originalTransfer = transfers.get(0);

        // Update with minimal change
        Amount originalAmountPlus1SEK = Amount.inSEK(originalTransfer.getAmount().getValue() + 1);
        Date originalDateMinus1Day = DateUtils.getCurrentOrPreviousBusinessDay(
                new DateTime(originalTransfer.getDueDate()).minusDays(1).toDate());

        Transfer transferToSign = TransferStub.eInvoice()
                .createUpdateTransferFromOriginal(originalTransfer)
                .withAmount(originalAmountPlus1SEK)
                .withDueDate(originalDateMinus1Day)
                .build();

        testUpdateTransfer(TestSSN.EP, "Ask EP", CredentialsTypes.MOBILE_BANKID, transferToSign);
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {

        Session session = new Session();
        session.setSecurityToken("my-expired-token");

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, Session.class);
    }

    @Test
    public void testTransferInternalAccount() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(10.0));
        transfer.setSource(new SwedishIdentifier(TestAccount.NORDEASSN_EP));
        transfer.setDestination(new SwedishIdentifier(TestAccount.NORDEA_EP));
        transfer.setDestinationMessage("Tink Test");
        transfer.setSourceMessage("Tink Test");

        testTransfer(TestSSN.EP, "9682", CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalAccount() throws Exception {
        Transfer transfer = TransferStub.bankTransfer()
                .withAmount(Amount.inSEK(1.0))
                .from(TestAccount.IdentifiersWithName.NORDEASSN_EP)
                .to(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                .withDestinationMessage("Tink Test")
                .withSourceMessage("Tink Test").build();

        testTransfer(TestSSN.EP, "9682", CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void positiveCreditLimitShouldReturnCorrectBalance() {
        CardDetailsEntity creditCard = createEmptyCardDetails();
        creditCard.getCreditLimit().put("$", "30000");
        creditCard.getFundsAvailable().put("$", "25000");

        Assert.assertEquals(-5000.0, creditCard.getCurrentBalance().doubleValue(), 0.00001);
    }

    @Test
    public void negativeCreditLimitShouldReturnCorrectBalance() {
        CardDetailsEntity creditCard = createEmptyCardDetails();
        creditCard.getCreditLimit().put("$", "30000");
        creditCard.getFundsAvailable().put("$", "-1000");

        Assert.assertEquals(-31000.0, creditCard.getCurrentBalance().doubleValue(), 0.00001);
    }

    @Test
    public void fallbackToCreditUsedWhenCreditLimitIsNotAvailable() {
        CardDetailsEntity creditCard = new CardDetailsEntity();
        creditCard.setCreditUsed(new HashMap<String, Object>());
        creditCard.getCreditUsed().put("$", "1000");

        Assert.assertEquals(-1000, creditCard.getCurrentBalance().doubleValue(), 0.00001);
    }

    @Test
    public void noDataAvailableShouldReturnNullBalance() {
        CardDetailsEntity creditCard = new CardDetailsEntity();
        Assert.assertNull(creditCard.getCurrentBalance());
    }

    private CardDetailsEntity createEmptyCardDetails() {
        CardDetailsEntity creditCard = new CardDetailsEntity();
        creditCard.setCreditLimit(new HashMap<String, Object>());
        creditCard.setFundsAvailable(new HashMap<String, Object>());
        return creditCard;
    }

    @Test
    public void elevenDigitCustodyAccountNumberShouldBeValid() {

        CustodyAccount account = new CustodyAccount();
        account.setAccountId("FONDA:01409805521");

        Assert.assertTrue(account.hasValidBankId());
    }

    @Test
    public void twelveDigitCustodyAccountNumberShouldBeValid() {


        CustodyAccount account = new CustodyAccount();
        account.setAccountId("FONDA:014481437611");

        Assert.assertTrue(account.hasValidBankId());
    }

    @Test
    public void eightDigitCustodyAccountNumberShouldBeValid() {

        CustodyAccount account = new CustodyAccount();
        account.setAccountId("ASBS:282350.1");

        Assert.assertTrue(account.hasValidBankId());
    }

    @Test
    public void otherAccountNumberShouldNotBeMatched() {
        List<String> invalidFormats = Lists.newArrayList("1212", "ISK:123", "FOND:", "ASBS:112", "ASBS:282350");

        for (String format : invalidFormats) {

            CustodyAccount account = new CustodyAccount();
            account.setAccountId(format);

            Assert.assertFalse(account.hasValidBankId());
        }

    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {

        Session session = new Session();
        session.setSecurityToken("my-token");
        session.addCookie(new BasicClientCookie("name", "value"));

        String serialized = MAPPER.writeValueAsString(session);

        Session result = MAPPER.readValue(serialized, Session.class);

        Assert.assertEquals(session.getSecurityToken(), result.getSecurityToken());
        Assert.assertEquals(1, result.getCookies().size());
        Assert.assertEquals("name", session.getCookies().get(0).getName());
        Assert.assertEquals("value", session.getCookies().get(0).getValue());
    }

    @Test
    public void testPersistentSessionNotExpiredLogic() throws Exception {

        Session session = new Session();
        session.setLastUpdated(new DateTime().minusMinutes(29));
        Assert.assertFalse(session.isExpired());
    }

    @Test
    public void testPersistentSessionExpiredLogic() throws Exception {

        Session session = new Session();
        session.setLastUpdated(new DateTime().minusMinutes(31));
        Assert.assertTrue(session.isExpired());
    }

    @Test
    public void testDeserializeAccountTransactionsWithOneRow() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        TransactionListResponse result = mapper.readValue("{\n"
                + "  \"getAccountTransactionsOut\": {\n"
                + "    \"accountId\": {\n"
                + "      \"$\": \"NDEASESSXXX-SE1-SEK-30222222379\"\n"
                + "    },\n"
                + "    \"continueKey\": {\n"
                + "      \"$\": \"8\"\n"
                + "    },\n"
                + "    \"accountTransaction\": {\n"
                + "      \"transactionKey\": {\n"
                + "        \"$\": \"edVArew6hDNsHmDo%2B%2FqRoi9FLmW2Io7%2FtWf9bYeRsMnOaCwlk9xwExIyFeUXZdC5ZRJm1IYpBn6HXAQW5Omigwxu4PXKB8iBP51TUqNyKp5x3VXezCraOKLhAdw8d5YH12HrFez21xMyxkBQHYuEn1c7IszVXWNaIPXvJC8fAG9Xkjs4u7KF5jxT8Bb85aJr6nB%2BHbBlwfvGGQ3uPN0XjTXon60k9DjQ0uWBS%2BkJsHY%3D\"\n"
                + "      },\n"
                + "      \"transactionDate\": {\n"
                + "        \"$\": \"2015-03-10T12:00:00.371+01:00\"\n"
                + "      },\n"
                + "      \"transactionText\": {\n"
                + "        \"$\": \"HANS\"\n"
                + "      },\n"
                + "      \"transactionCounterpartyName\": {},\n"
                + "      \"transactionCurrency\": {},\n"
                + "      \"transactionAmount\": {\n"
                + "        \"$\": 200.00\n"
                + "      },\n"
                + "      \"isCoverReservationTransaction\": {\n"
                + "        \"$\": false\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}", TransactionListResponse.class);

        Assert.assertNotNull(result.getAccountTransactions());
        Assert.assertEquals(1, result.getAccountTransactions().getAccountTransactions().size());
    }

    @Test
    public void testDeserializeAccountTransactionsWithMultipleRows() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        TransactionListResponse result = mapper.readValue(
                "{\"getAccountTransactionsOut\":{\"accountId\":{\"$\":\"NDEASESSXXX-SE1-SEK-47178051988\"},\"continueKey\":{\"$\":\"8\"},\"accountTransaction\":[{\"transactionKey\":{\"$\":\"vH4lziHml0%2FNCJfZR0lrQxeGAzYGFsNLt3nilsA5AX6IwXohMy%2B0HS6kla8zpmDuyavngGTh1KkXdEKpTavrfBrcaOwbnO0QZRfozqJSc3Nr5uQHCgxlSthqfFqDZQM0rt7SRUuRgLfKVaTCfDCiTyfH6Tgd3XAer46DtAJUwjqt7tsVjV9JXn9Lij0rxVw3uKDHeG0XCkzXYC3vruHqmvUfHssoxDWsS1KSo3yubG4%3D\"},\"transactionDate\":{\"$\":\"2015-10-13T12:00:00.791+02:00\"},\"transactionText\":{\"$\":\"Tink 840114-1935\"},\"transactionCounterpartyName\":{},\"transactionCurrency\":{},\"transactionAmount\":{\"$\":-10.00},\"isCoverReservationTransaction\":{\"$\":false}},{\"transactionKey\":{\"$\":\"Kk%2FtgYYp6amoxfOo2nwTvZT0APflqznwoiC4DBPRA9M8GJKfNyfjzxONuW%2BanPV5obyuaLvnYmJWzYgKL%2B%2Fv5Idl3MtPdxlCUCyWqaGWlBsmw13dQfd1hFKAG5fsoX55vJhKDlEQb81iO8%2F5gzxnJDt3z6jhy%2FClkQSKGEDAljfo0%2Fh15TJ3W3I4UVchF6H4JEK9pBWprgXssSqtKS15FhxKrRv31PmnN6nvl%2F31FFE%3D\"},\"transactionDate\":{\"$\":\"2015-09-28T12:00:00.791+02:00\"},\"transactionText\":{\"$\":\"Överföring 1603 43 32648\"},\"transactionCounterpartyName\":{},\"transactionCurrency\":{},\"transactionAmount\":{\"$\":-100.00},\"isCoverReservationTransaction\":{\"$\":false}},{\"transactionKey\":{\"$\":\"0eVXFjEZdgckQM2D0KUs3gg8SlNsp20oxrAgrgIY7U1T2dU0Tws%2BXJyoyXcD2UGQQNbmzJNFEGmeSIDokQmgpUvX0lk1G%2FfiRmwEYVYKRPwv4EfXHQCnIKF9rwTUauqsPzcx9R9%2BXjzHNIn3PKUQdJ6ksD4bmtIT%2BvLCMoHRajxXYZBuBz7j2SFwbtKaStI81llzEmH08BHzvYpEYoo0d3ooxQs9rah73Tz%2Bpu%2ByFcM%3D\"},\"transactionDate\":{\"$\":\"2015-09-03T12:00:00.791+02:00\"},\"transactionText\":{\"$\":\"Överföring 840114-1935\"},\"transactionCounterpartyName\":{},\"transactionCurrency\":{},\"transactionAmount\":{\"$\":-0.01},\"isCoverReservationTransaction\":{\"$\":false}},{\"transactionKey\":{\"$\":\"oiboLy7yZP%2FoXBoAKVSmkOjStGpGGQO%2BnQMpn8O4FuR8x%2FOb1811cqOjWfH19feBXgU%2FthT%2FjS%2F5faVGHxYDpDvVOY6GTp3Wc71i7ZGr%2BMfWYXSOh%2BldvxGdBGyjVYGM7YzGFNvDUnUgxDDhfGqtASyJFOyxqgWvZF9Q8HRhAuK2ATV6pI2IGVa6RyRZnkavinwqybB6EWF8LnWlCsonBHnT7pL9K1DVjNr44lo75Rc%3D\"},\"transactionDate\":{\"$\":\"2015-03-27T12:00:00.791+01:00\"},\"transactionText\":{\"$\":\"VP Köpnota 199471501\"},\"transactionCounterpartyName\":{},\"transactionCurrency\":{},\"transactionAmount\":{\"$\":-661.00},\"isCoverReservationTransaction\":{\"$\":false}},{\"transactionKey\":{\"$\":\"LGjSr45zJgU4zgbMKfqYZ8QQeXs67pVGvwxqLaji4uHGhKkGK4o14iz%2FT6aQ2LawESaO4ZWGRuB%2FnGSSP8RTA0bFZbMYOs0l8wO0q5t05BJdvV8MAZAUFRz2QhkqCzsZJNI8tjV%2FwcwdspeTCYxq4UPyOTDmyiLxGuej9JphUz%2F02mUlLkjLsBhZ1FDAbZ28AgBUSXCxr%2B%2FZeO5bPtukbjhM3V1Wa9CUlQB3drePcnY%3D\"},\"transactionDate\":{\"$\":\"2015-03-25T12:00:00.791+01:00\"},\"transactionText\":{\"$\":\"Överföring 840114-1935\"},\"transactionCounterpartyName\":{},\"transactionCurrency\":{},\"transactionAmount\":{\"$\":1000.00},\"isCoverReservationTransaction\":{\"$\":false}}]}}",
                TransactionListResponse.class);

        Assert.assertNotNull(result.getAccountTransactions());
        Assert.assertEquals(5, result.getAccountTransactions().getAccountTransactions().size());
    }

    @Test
    public void testDeserializeContextWithOneProduct() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InitialContextResponse context = mapper.readValue(
                "{\"getInitialContextOut\":{\"userPaymentRights\":{\"$\":false},\"product\":{\"productType\":{\"$\":\"Account\"},\"cardGroup\":{},\"productId\":{\"@id\":{\"$\":\"W8sf5T345ps0XvU45rv8yN4a0Lw%3D%3D\"},\"@view\":{\"$\":true},\"@pay\":{\"$\":false},\"@deposit\":{\"$\":true},\"@ownTransferFrom\":{\"$\":true},\"@ownTransferTo\":{\"$\":true},\"@thirdParty\":{\"$\":false},\"@paymentAccount\":{\"$\":true},\"$\":\"NDEASESSXXX-SE1-SEK-30490075150\"},\"productNumber\":{\"$\":\"11111\"},\"accountType\":{\"$\":\"1100\"},\"productTypeExtension\":{\"$\":\"SE0000\"},\"currency\":{\"$\":\"SEK\"},\"nickName\":{},\"productCode\":{},\"productName\":{},\"balance\":{\"$\":208000.00},\"fundsAvailable\":{\"$\":208000.00},\"branchId\":{},\"isMainCard\":{\"$\":false},\"warningCode\":{}}}}",
                InitialContextResponse.class);

        Assert.assertNotNull(context.getData());
        Assert.assertNotNull(context.getData().getProducts());
        Assert.assertEquals(1, context.getData().getProducts().size());
    }

    @Test
    public void testDeserializeContextWithManyProducts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InitialContextResponse context = mapper.readValue(
                "{\"getInitialContextOut\":{\"userPaymentRights\":{\"$\":true},\"product\":[{\"productType\":{\"$\":\"Account\"},\"cardGroup\":{},\"productId\":{\"@id\":{\"$\":\"ZPC1slChBfhzwXdnSU260WVRi%2BpT9ztzT9j0D%3D\"},\"@view\":{\"$\":true},\"@pay\":{\"$\":false},\"@deposit\":{\"$\":true},\"@ownTransferFrom\":{\"$\":true},\"@ownTransferTo\":{\"$\":true},\"@thirdParty\":{\"$\":true},\"@paymentAccount\":{\"$\":false},\"$\":\"NDEASESSXXX-SE1-SEK-16034332648\"},\"productNumber\":{\"$\":\"16034332648\"},\"accountType\":{\"$\":\"1100\"},\"productTypeExtension\":{\"$\":\"SE5700\"},\"currency\":{\"$\":\"SEK\"},\"nickName\":{},\"productCode\":{},\"productName\":{},\"balance\":{\"$\":35419.96},\"fundsAvailable\":{\"$\":35419.96},\"branchId\":{},\"isMainCard\":{\"$\":false},\"warningCode\":{}},{\"productType\":{\"$\":\"Account\"},\"cardGroup\":{},\"productId\":{\"@id\":{\"$\":\"pSbI%2F%2FFoOHRWGkhSdrSK2O0nQWgMTgrSDNmHA%2F3ZhoE0d1hxqUG59SQetLQwAVQgz9TVCKnOg%3D%3D\"},\"@view\":{\"$\":true},\"@pay\":{\"$\":false},\"@deposit\":{\"$\":true},\"@ownTransferFrom\":{\"$\":true},\"@ownTransferTo\":{\"$\":true},\"@thirdParty\":{\"$\":false},\"@paymentAccount\":{\"$\":false},\"$\":\"NDEASESSXXX-SE1-SEK-47178051988\"},\"productNumber\":{\"$\":\"47178051988\"},\"accountType\":{\"$\":\"1100\"},\"productTypeExtension\":{\"$\":\"SE4300\"},\"currency\":{\"$\":\"SEK\"},\"nickName\":{\"$\":\"Nordea ISK\"},\"productCode\":{},\"productName\":{},\"balance\":{\"$\":92.99},\"fundsAvailable\":{\"$\":92.99},\"branchId\":{},\"isMainCard\":{\"$\":false},\"warningCode\":{}},{\"productType\":{\"$\":\"Account\"},\"cardGroup\":{},\"productId\":{\"@id\":{\"$\":\"wvT8G6gyDXHs%2Bg%2BEfnSPcNSsy7rsLDA%3D%3D\"},\"@view\":{\"$\":true},\"@pay\":{\"$\":true},\"@deposit\":{\"$\":true},\"@ownTransferFrom\":{\"$\":true},\"@ownTransferTo\":{\"$\":true},\"@thirdParty\":{\"$\":true},\"@paymentAccount\":{\"$\":true},\"$\":\"NDEASESSXXX-SE1-SEK-840114111\"},\"productNumber\":{\"$\":\"111\"},\"accountType\":{\"$\":\"1100\"},\"productTypeExtension\":{\"$\":\"SE0000\"},\"currency\":{\"$\":\"SEK\"},\"nickName\":{},\"productCode\":{},\"productName\":{},\"balance\":{\"$\":25620.00},\"fundsAvailable\":{\"$\":25620.00},\"branchId\":{},\"isMainCard\":{\"$\":false},\"warningCode\":{}}]}}",
                InitialContextResponse.class);

        Assert.assertNotNull(context.getData());
        Assert.assertNotNull(context.getData().getProducts());
        Assert.assertEquals(3, context.getData().getProducts().size());
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<NordeaAgent> {
        public TransferMessageFormatting() {
            super(NordeaAgent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.NORDEA_EP)
                    .to(TestAccount.IdentifiersWithName.NORDEASSN_EP)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.EP, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.NORDEA_EP)
                    .to(TestAccount.IdentifiersWithName.NORDEASSN_JK)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.EP, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.NORDEA_EP)
                    .to(TestAccount.IdentifiersWithName.NORDEASSN_EP)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            testTransfer(TestSSN.EP, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.NORDEA_EP)
                    .to(TestAccount.IdentifiersWithName.NORDEASSN_JK)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            try {
                testTransfer(TestSSN.EP, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }

        @Override
        protected Provider constructProvider() {
            Provider p = new Provider();
            p.setMarket("SE");
            return p;
        }
    }
}


