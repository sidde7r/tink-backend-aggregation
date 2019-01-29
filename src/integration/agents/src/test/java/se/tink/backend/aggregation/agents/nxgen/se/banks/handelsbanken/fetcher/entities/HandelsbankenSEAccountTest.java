package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

import java.util.stream.Collectors;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandelsbankenSEAccountTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    private void createTinkAccount() {
        HandelsbankenSEAccount account = new HandelsbankenSEAccount()
                .setNumber(number)
                .setAmountAvailable(new HandelsbankenAmount().setCurrency("SEK").setAmount(20.20)
                ).setNumberFormatted("123 456 78");

        TransactionsSEResponse transactionsResponse = mock(TransactionsSEResponse.class);
        HandelsbankenSEAccount transactionsAccount = mock(HandelsbankenSEAccount.class);
        when(transactionsResponse.getAccount()).thenReturn(transactionsAccount);
        when(transactionsAccount.getClearingNumber()).thenReturn("1234");

        tinkAccount = account.toTransactionalAccount(transactionsResponse).orElseThrow(() -> new IllegalStateException("No account found!"));
    }

    private void assertTinkAccountIsValid() {
        assertEquals(new Double(20.20), tinkAccount.getBalance().getValue());
        assertEquals("1234-123 456 78", tinkAccount.getAccountNumber());
        assertThat(tinkAccount.getIdentifiers().stream()
                        .map(AccountIdentifier::getType)
                        .collect(Collectors.toList()),
                hasItems(AccountIdentifier.Type.SE, AccountIdentifier.Type.SE_SHB_INTERNAL));
    }
}
