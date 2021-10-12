package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transferdestination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SkandiaBankenTransferDestinationFetcherTest {

    private SkandiaBankenTransferDestinationFetcher objectUnderTest;

    @Before
    public void setUp() {
        this.objectUnderTest = new SkandiaBankenTransferDestinationFetcher();
    }

    @Test
    public void shouldFetchTransferDestinations() {
        // given
        List<Account> accounts = getAccounts();
        Account checkingAccountWithTransferCapability = accounts.get(0);
        Account savingsAccountWithTransferCapability = accounts.get(1);

        // when
        TransferDestinationsResponse transferDestinationsResponse =
                objectUnderTest.fetchTransferDestinationsFor(accounts);

        // then
        assertThat(transferDestinationsResponse.getDestinations().size()).isEqualTo(2);

        List<TransferDestinationPattern> checkingTransferDestinationPatterns =
                transferDestinationsResponse
                        .getDestinations()
                        .get(checkingAccountWithTransferCapability);
        List<TransferDestinationPattern> savingsTransferDestinationPatterns =
                transferDestinationsResponse
                        .getDestinations()
                        .get(savingsAccountWithTransferCapability);

        assertNotNull(checkingTransferDestinationPatterns);
        assertNotNull(savingsTransferDestinationPatterns);
        assertThat(checkingTransferDestinationPatterns).isEqualTo(getTransferAndGiroDestinations());
        assertThat(savingsTransferDestinationPatterns).isEqualTo(getTransferDestinations());
    }

    private List<Account> getAccounts() {
        return Arrays.asList(
                SkandiaBankenTransferDestinationFetcherTestData
                        .getCheckingAccountWithExternalTransferCapability(),
                SkandiaBankenTransferDestinationFetcherTestData
                        .getSavingsAccountWithExternalTransferCapability(),
                SkandiaBankenTransferDestinationFetcherTestData.getInvestmentAccount(),
                SkandiaBankenTransferDestinationFetcherTestData
                        .getCheckingAccountWithNoCapabilities(),
                SkandiaBankenTransferDestinationFetcherTestData
                        .getCheckingAccountWithNoExternalTransferCapability());
    }

    private List<TransferDestinationPattern> getTransferDestinations() {
        return Collections.singletonList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    private List<TransferDestinationPattern> getTransferAndGiroDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG));
    }
}
