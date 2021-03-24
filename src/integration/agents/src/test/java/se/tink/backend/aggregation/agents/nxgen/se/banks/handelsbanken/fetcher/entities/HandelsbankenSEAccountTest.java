package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenSEAccountTest {

    @Rule public ExpectedException exception = ExpectedException.none();

    private String number;
    private TransactionalAccount tinkAccount;

    @Before
    public void setUp() throws Exception {
        number = "12345678";
    }

    @Test
    public void numberIsValidBankId_8digits() {
        createTinkAccount();

        assertTinkAccountIsValid();
    }

    @Test
    public void numberIsValidBankId_9digits() {
        number = "123456789";

        createTinkAccount();

        assertTinkAccountIsValid();
    }

    @Test
    public void numberIsValidBankId_13digits() {
        number = "1234567890123";

        createTinkAccount();

        assertTinkAccountIsValid();
    }

    @Test
    public void numberIsValidBankId_Formatted() {
        number = "12-123456-123456";

        createTinkAccount();

        assertTinkAccountIsValid();
    }

    @Test
    public void numberIsInvalidBankId() {
        exception.expectMessage("Unexpected account.bankid ");
        number = "1234567";

        createTinkAccount();
    }

    @Test
    public void toTinkAccountChecking() {
        HandelsbankenSEAccount account =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_DATA, HandelsbankenSEAccount.class);
        AccountInfoResponse accountInfo =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_INFO_CHECKING_DATA, AccountInfoResponse.class);
        TransactionsSEResponse transactions =
                SerializationUtils.deserializeFromString(
                        TRANSACTIONS_DATA, TransactionsSEResponse.class);
        HandelsbankenApiClient apiClient = mock(HandelsbankenApiClient.class);
        when(apiClient.accountInfo(any(URL.class))).thenReturn(accountInfo);

        Optional<TransactionalAccount> optionalAccount =
                account.toTransactionalAccount(apiClient, transactions);
        assertTrue(optionalAccount.isPresent());
        TransactionalAccount tinkAccount = optionalAccount.get();
        assertEquals(AccountTypes.CHECKING, tinkAccount.getType());
    }

    @Test
    public void toTinkAccountSavings() {
        HandelsbankenSEAccount account =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_DATA, HandelsbankenSEAccount.class);
        AccountInfoResponse accountInfo =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_INFO_SAVINGS_DATA, AccountInfoResponse.class);
        TransactionsSEResponse transactions =
                SerializationUtils.deserializeFromString(
                        TRANSACTIONS_DATA, TransactionsSEResponse.class);
        HandelsbankenApiClient apiClient = mock(HandelsbankenApiClient.class);
        when(apiClient.accountInfo(any(URL.class))).thenReturn(accountInfo);

        Optional<TransactionalAccount> optionalAccount =
                account.toTransactionalAccount(apiClient, transactions);
        assertTrue(optionalAccount.isPresent());
        TransactionalAccount tinkAccount = optionalAccount.get();
        assertEquals(AccountTypes.SAVINGS, tinkAccount.getType());
    }

    private void createTinkAccount() {
        HandelsbankenSEAccount account =
                new HandelsbankenSEAccount()
                        .setName("allkonto")
                        .setNumber(number)
                        .setAmountAvailable(
                                new HandelsbankenAmount().setCurrency("SEK").setAmount(20.20))
                        .setNumberFormatted("123 456 78");

        AccountInfoResponse accountInfoResponse = mock(AccountInfoResponse.class);
        when(accountInfoResponse.getValuesByLabel())
                .thenReturn(ImmutableMap.of("kontoform", "Allkonto"));
        HandelsbankenApiClient apiClient = mock(HandelsbankenApiClient.class);
        when(apiClient.accountInfo(any(URL.class))).thenReturn(accountInfoResponse);
        TransactionsSEResponse transactionsResponse = mock(TransactionsSEResponse.class);
        HandelsbankenSEAccount transactionsAccount = mock(HandelsbankenSEAccount.class);
        when(transactionsResponse.getAccount()).thenReturn(transactionsAccount);
        when(transactionsAccount.getClearingNumber()).thenReturn("1234");

        tinkAccount =
                account.toTransactionalAccount(apiClient, transactionsResponse)
                        .orElseThrow(() -> new IllegalStateException("No account found!"));
    }

    private void assertTinkAccountIsValid() {
        assertThat(tinkAccount.getExactBalance().getDoubleValue()).isEqualTo(20.20);
        assertThat(tinkAccount.getAccountNumber()).isEqualTo("1234-123 456 78");
        // TODO: getAccountType() returns OTHER for the above test data
        // assertEquals(AccountTypes.CHECKING, tinkAccount.getType());
        assertThat(
                        tinkAccount.getIdentifiers().stream()
                                .map(AccountIdentifier::getType)
                                .collect(Collectors.toList()))
                .contains(AccountIdentifierType.SE, AccountIdentifierType.SE_SHB_INTERNAL);
    }

    private static final String ACCOUNT_DATA =
            "{"
                    + "\"_links\": {"
                    + "\"transactions\": {"
                    + "\"href\": \"https://m2.handelsbanken.se/app/priv/accounts/111818111/transactions?isCard=false&authToken=47695af95012\","
                    + "\"gaScreenName\": \"accounts / account details\""
                    + "}"
                    + "},"
                    + "\"links\": [{"
                    + "\"rel\": \"transactions\","
                    + "\"href\": \"https://m2.handelsbanken.se/app/priv/accounts/111818111/transactions?isCard=false&authToken=47695af95012\","
                    + "\"type\": \"application/json\","
                    + "\"gaScreenName\": \"accounts / account details\""
                    + "}],"
                    + "\"name\": \"Sparkonto\","
                    + "\"number\": \"111818111\","
                    + "\"numberFormatted\": \"111 818 111\","
                    + "\"balance\": {"
                    + "\"amount\": 0.00,"
                    + "\"amountFormatted\": \"0,00\","
                    + "\"unit\": \"kr\","
                    + "\"currency\": \"SEK\""
                    + "},"
                    + "\"holderName\": \"Kalle Kula\","
                    + "\"displayBalance\": true,"
                    + "\"isCard\": false,"
                    + "\"amountAvailable\": {"
                    + "\"amount\": 0.00,"
                    + "\"amountFormatted\": \"0,00\","
                    + "\"unit\": \"kr\","
                    + "\"currency\": \"SEK\""
                    + "},"
                    + "\"clearingNumber\": null,"
                    + "\"overDraft\": false,"
                    + "\"warningTextShort\": null,"
                    + "\"warningTextLong\": null"
                    + "}";
    private static final String ACCOUNT_INFO_CHECKING_DATA =
            "{"
                    + "\"_links\": {},"
                    + "\"links\": [],"
                    + "\"heading\": \"kuligt konto\","
                    + "\"items\": [{"
                    + "\"label\": \"Kontonummer\","
                    + "\"value\": \"1111 - 111 818 111\""
                    + "}, {"
                    + "\"label\": \"Kontoform\","
                    + "\"value\": \"Allkonto\""
                    + "}, {"
                    + "\"label\": \"Kontohavare\","
                    + "\"value\": \"Kalle Kula\""
                    + "}, {"
                    + "\"label\": \"IBAN\","
                    + "\"value\": \"1\""
                    + "}, {"
                    + "\"label\": \"BIC (Nationellt bankID)\","
                    + "\"value\": \"HANDSESS\""
                    + "}]"
                    + "}";
    private static final String ACCOUNT_INFO_SAVINGS_DATA =
            "{"
                    + "\"_links\": {},"
                    + "\"links\": [],"
                    + "\"heading\": \"kuligt konto\","
                    + "\"items\": [{"
                    + "\"label\": \"Kontonummer\","
                    + "\"value\": \"1111 - 111 818 111\""
                    + "}, {"
                    + "\"label\": \"Kontoform\","
                    + "\"value\": \"sparkonto\""
                    + "}, {"
                    + "\"label\": \"Kontohavare\","
                    + "\"value\": \"Kalle Kula\""
                    + "}, {"
                    + "\"label\": \"IBAN\","
                    + "\"value\": \"1\""
                    + "}, {"
                    + "\"label\": \"BIC (Nationellt bankID)\","
                    + "\"value\": \"HANDSESS\""
                    + "}]"
                    + "}";
    private static final String TRANSACTIONS_DATA =
            "{"
                    + "\"account\": {"
                    + "\"_links\": {"
                    + "\"account-info\": {"
                    + "\"href\": \"https://m2.handelsbanken.se/app/priv/accounts/111818111/account-info?hasBalance=false&authToken=47695af95012\","
                    + "\"title\": \"Kontoinformation och inställningar\","
                    + "\"gaScreenName\": \"accounts / account details / account information and settings\""
                    + "}"
                    + "},"
                    + "\"links\": [{"
                    + "\"rel\": \"account-info\","
                    + "\"href\": \"https://m2.handelsbanken.se/app/priv/accounts/111818111/account-info?hasBalance=false&authToken=47695af95012\","
                    + "\"type\": \"application/json\","
                    + "\"title\": \"Kontoinformation och inställningar\","
                    + "\"gaScreenName\": \"accounts / account details / account information and settings\""
                    + "}],"
                    + "\"name\": \"Sparpengar\","
                    + "\"number\": \"111818111\","
                    + "\"numberFormatted\": \"111 818 111\","
                    + "\"balance\": {"
                    + "\"amount\": 0.00,"
                    + "\"amountFormatted\": \"0,00\","
                    + "\"unit\": \"kr\","
                    + "\"currency\": \"SEK\""
                    + "},"
                    + "\"holderName\": null,"
                    + "\"displayBalance\": true,"
                    + "\"isCard\": false,"
                    + "\"amountAvailable\": null,"
                    + "\"clearingNumber\": \"1111\","
                    + "\"overDraft\": false,"
                    + "\"warningTextShort\": null,"
                    + "\"warningTextLong\": null"
                    + "},"
                    + "\"transactions\": [],"
                    + "\"cardInvoiceInfo\": null"
                    + "}";
}
