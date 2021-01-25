package se.tink.backend.aggregation.workers.commands.migrations.implementations.brokers.avanza;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AvanzaStripClearingMigrationTest {

    private static final String PROVIDER_NAME = "avanza-bankid";
    private static final String NEW_AGENT_NAME = "nxgen.se.brokers.avanza.AvanzaAgent";
    private static final String OLD_AGENT_NAME = "brokers.avanza.AvanzaV2Agent";

    private AvanzaStripClearingMigration migration;

    private CredentialsRequest request;
    private List<Account> accountList;

    private Account oldFormat;
    private Account newFormat;

    @Before
    public void setUp() throws Exception {
        ControllerWrapper wrapper = Mockito.mock(ControllerWrapper.class);
        when(wrapper.updateAccountMetaData(any(), any())).thenReturn(null);

        migration = new AvanzaStripClearingMigration();
        migration.setWrapper(wrapper);
        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(OLD_AGENT_NAME);

        request =
                new CredentialsRequest() {
                    @Override
                    public boolean isManual() {
                        return true;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return CredentialsRequestType.UPDATE;
                    }
                };

        oldFormat = new Account();
        oldFormat.setBankId("9553-5382765");
        oldFormat.setType(AccountTypes.INVESTMENT);

        newFormat = oldFormat.clone();
        newFormat.setBankId("5382765");

        accountList = Lists.newArrayList();
        request.setAccounts(accountList);
        request.setProvider(provider);

        LoggerFactory.getLogger(AvanzaStripClearingMigration.class)
                .warn("null test: {}", getSomething());
    }

    private String getSomething() {
        return null;
    }

    @Test
    public void migrateData() {
        accountList.add(oldFormat);
        migration.migrateData(request);

        assertEquals(1, request.getAccounts().size());
        assertEquals(this.newFormat.getBankId(), request.getAccounts().get(0).getBankId());
    }

    @Test
    public void isOldAgent() {
        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(OLD_AGENT_NAME);
        assertTrue(migration.isOldAgent(provider));

        provider.setName(PROVIDER_NAME);
        provider.setClassName(NEW_AGENT_NAME);
        assertFalse(migration.isOldAgent(provider));
    }

    @Test
    public void isNewAgent() {
        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(NEW_AGENT_NAME);
        assertTrue(migration.isNewAgent(provider));

        provider.setName(PROVIDER_NAME);
        provider.setClassName(OLD_AGENT_NAME);
        assertFalse(migration.isNewAgent(provider));
    }

    @Test
    public void getNewAgentClassName() {
        assertEquals(NEW_AGENT_NAME, migration.getNewAgentClassName(null));
    }

    @Test
    public void isDataMigrated() {
        accountList.add(this.oldFormat);
        assertFalse(migration.isDataMigrated(request));

        accountList = Lists.newArrayList(this.newFormat);
        request.setAccounts(accountList);
        assertTrue(migration.isDataMigrated(request));
    }
}
