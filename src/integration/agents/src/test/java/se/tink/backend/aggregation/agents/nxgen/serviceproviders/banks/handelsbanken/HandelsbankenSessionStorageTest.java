package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links.ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links.TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.StorageTestHelper.createLinks;

import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListFIResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenSessionStorageTest {

    public static final Map<String, Link> TRANSACTIONS_LINK =
            createLinks(TRANSACTIONS, "https://safe.transactions.com");
    public static final Map<String, Link> ACCOUNTS_LINK =
            createLinks(ACCOUNTS, "http://unsafe.accounts.com");
    private HandelsbankenSessionStorage sessionStorage;

    @Test
    public void canPersistSEApplicationEntryPoint() {
        initiateSweden();

        ApplicationEntryPointResponse applicationEntryPoint = createApplicationEntryPoint();

        sessionStorage.persist(applicationEntryPoint);

        assertThat(sessionStorage.applicationEntryPoint(), is(Optional.of(applicationEntryPoint)));
    }

    @Test
    public void canPersistFIApplicationEntryPoint() {
        initiateFinland();

        ApplicationEntryPointResponse applicationEntryPoint = createApplicationEntryPoint();

        sessionStorage.persist(applicationEntryPoint);

        assertThat(sessionStorage.applicationEntryPoint(), is(Optional.of(applicationEntryPoint)));
    }

    @Test
    public void willRemovePersistedApplicationEntryPointSE() {
        initiateSweden();

        ApplicationEntryPointResponse applicationEntryPoint = createApplicationEntryPoint();

        sessionStorage.persist(applicationEntryPoint);
        sessionStorage.removeApplicationEntryPoint();

        assertThat(sessionStorage.applicationEntryPoint(), is(Optional.empty()));
    }

    @Test
    public void willRemovePersistedApplicationEntryPointFI() {
        initiateFinland();

        ApplicationEntryPointResponse applicationEntryPoint = createApplicationEntryPoint();

        sessionStorage.persist(applicationEntryPoint);
        sessionStorage.removeApplicationEntryPoint();

        assertThat(sessionStorage.applicationEntryPoint(), is(Optional.empty()));
    }

    @Test
    public void canPersistSEAccountList() {
        initiateSweden();

        AccountListSEResponse accountList = new AccountListSEResponse();
        accountList.setLinks(TRANSACTIONS_LINK);

        sessionStorage.persist(accountList);

        assertThat(sessionStorage.accountList(), is(Optional.of(accountList)));
    }

    @Test
    public void canPersistFIAccountList() {
        initiateFinland();

        AccountListFIResponse accountList = new AccountListFIResponse();
        accountList.setLinks(TRANSACTIONS_LINK);

        sessionStorage.persist(accountList);

        assertThat(sessionStorage.accountList(), is(Optional.of(accountList)));
    }

    private void initiateSweden() {
        initiateSessionStorage(new HandelsbankenSEConfiguration());
    }

    private void initiateFinland() {
        initiateSessionStorage(new HandelsbankenFIConfiguration());
    }

    private void initiateSessionStorage(HandelsbankenConfiguration configuration) {
        sessionStorage =
                new HandelsbankenSessionStorage(
                        configuration, new SessionStorage(), new FakeLogMasker());
    }

    private static ApplicationEntryPointResponse createApplicationEntryPoint() {
        ApplicationEntryPointResponse applicationEntryPoint = new ApplicationEntryPointResponse();
        applicationEntryPoint.setLinks(ACCOUNTS_LINK);
        return applicationEntryPoint;
    }
}
