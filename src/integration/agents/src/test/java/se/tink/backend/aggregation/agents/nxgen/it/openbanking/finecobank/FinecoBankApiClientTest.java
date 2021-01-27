package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankApiClientTest {

    private Object[] emptyTransactionalAccountBalances() {
        return new Object[] {
            Collections.emptyList(),
            Collections.singletonList(new AccountConsent(null, "1111")),
            Arrays.asList(new AccountConsent(null, "1111"), new AccountConsent(null, "2222")),
        };
    }

    private Object[] notEmptyTransactionalAccountBalances() {
        return new Object[] {
            Collections.singletonList(new AccountConsent("1111", null)),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent("2222", null)),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent(null, "2222")),
        };
    }

    private Object[] emptyCreditCardBalances() {
        return new Object[] {
            Collections.emptyList(),
            Collections.singletonList(new AccountConsent("1111", null)),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent("2222", null)),
        };
    }

    private Object[] notEmptyCreditCardBalances() {
        return new Object[] {
            Collections.singletonList(new AccountConsent(null, "1111")),
            Arrays.asList(new AccountConsent(null, "1111"), new AccountConsent(null, "2222")),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent(null, "2222")),
        };
    }

    @Test
    @Parameters(method = "emptyTransactionalAccountBalances")
    public void isEmptyTransactionalAccountBalanceConsentShouldReturnTrue(
            List<AccountConsent> balancesItems) {
        // given
        FinecoBankApiClient apiClient = prepareApiClientWithMockedStorage(balancesItems);

        // when
        boolean isEmpty = apiClient.isEmptyTransactionalAccountBalanceConsent();

        // then
        assertThat(isEmpty).isEqualTo(true);
    }

    @Test
    @Parameters(method = "notEmptyTransactionalAccountBalances")
    public void isEmptyTransactionalAccountBalanceConsentShouldReturnFalse(
            List<AccountConsent> balancesItems) {
        // given
        FinecoBankApiClient apiClient = prepareApiClientWithMockedStorage(balancesItems);

        // when
        boolean isEmpty = apiClient.isEmptyTransactionalAccountBalanceConsent();

        // then
        assertThat(isEmpty).isEqualTo(false);
    }

    @Test
    @Parameters(method = "emptyCreditCardBalances")
    public void isEmptyCreditCardAccountBalanceConsentShouldReturnTrue(
            List<AccountConsent> balancesItems) {
        // given
        FinecoBankApiClient apiClient = prepareApiClientWithMockedStorage(balancesItems);

        // when
        boolean isEmpty = apiClient.isEmptyCreditCardAccountBalanceConsent();

        // then
        assertThat(isEmpty).isEqualTo(true);
    }

    @Test
    @Parameters(method = "notEmptyCreditCardBalances")
    public void isEmptyCreditCardAccountBalanceConsentShouldReturnFalse(
            List<AccountConsent> balancesItems) {
        // given
        FinecoBankApiClient apiClient = prepareApiClientWithMockedStorage(balancesItems);

        // when
        boolean isEmpty = apiClient.isEmptyCreditCardAccountBalanceConsent();

        // then
        assertThat(isEmpty).isEqualTo(false);
    }

    private FinecoBankApiClient prepareApiClientWithMockedStorage(
            List<AccountConsent> balancesItems) {
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        FinecoBankConfiguration configuration = mock(FinecoBankConfiguration.class);
        when(persistentStorage.get(
                        ArgumentMatchers.eq(StorageKeys.BALANCES_CONSENTS),
                        ArgumentMatchers.<TypeReference<List<AccountConsent>>>any()))
                .thenReturn(Optional.of(balancesItems));

        return new FinecoBankApiClient(
                null,
                persistentStorage,
                new AgentConfiguration.Builder()
                        .setProviderSpecificConfiguration(configuration)
                        .setRedirectUrl("REDIRECT_URL")
                        .build(),
                true,
                "127.0.0.1");
    }
}
