package se.tink.backend.aggregation.workers.commands.migrations.implemntations.other.handelsbanken;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAgent;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HandelsbankenBankIdMigrationNoClearingNumberTest {

  public static final String PROVIDER_NAME = "handelsbanken-bankid";
  public static final String NEW_AGENT_NAME = HandelsbankenSEAgent.class.getCanonicalName();
  public static final String OLD_AGENT_NAME = "HandelsbankenV6";
  private HandelsbankenBankIdMigrationNoClearingNumber migration;
  private CredentialsRequest request;
  private List<Account> accountList;
  private Provider provider;
  private Account oldFormat;
  private Account newFormat;
  private ControllerWrapper wrapper;

  @Before
  public void setUp() throws Exception {
    this.wrapper = Mockito.mock(ControllerWrapper.class);
    this.migration = new HandelsbankenBankIdMigrationNoClearingNumber();
    this.migration.setWrapper(this.wrapper);

    this.provider = new Provider();
    this.provider.setName(PROVIDER_NAME);

    this.accountList = Lists.newArrayList();

    this.oldFormat = new Account();
    this.oldFormat.setBankId("1234-12345678");
    this.oldFormat.setAccountNumber("1234-12 345 678");
    this.oldFormat.setType(AccountTypes.CHECKING);

    this.newFormat = this.oldFormat.clone();
    this.newFormat.setBankId("12345678");

    this.request =
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
    this.request.setAccounts(accountList);
    this.request.setProvider(provider);
  }

  @Test
  public void shouldChangeRequest_yes() {
    provider.setClassName(OLD_AGENT_NAME);
    assertTrue(this.migration.shouldChangeRequest(this.request));
  }

  @Test
  public void shouldChangeRequest_sameAgent_no() {
    provider.setClassName(NEW_AGENT_NAME);
    assertFalse(this.migration.shouldChangeRequest(this.request));
  }

  @Test
  public void shouldMigrateData_yes() {}

  @Test
  public void shouldMigrateData_alreadyNewFormat_no() {}

  @Test
  public void shouldMigrateData_noAccountsToMgrate_no() {}

  @Test
  public void changeRequest() {
    migration.changeRequest(request);
  }

  @Test
  public void migrateData() {
    this.accountList.add(this.oldFormat);

    when(wrapper.updateAccountMetaData(any(String.class), any(String.class)))
        .thenReturn(this.newFormat);

    this.migration.updateAccounts(request);

    verify(wrapper).updateAccountMetaData(this.oldFormat.getId(), this.newFormat.getBankId());

    assertEquals(request.getAccounts().size(), 1);
    assertEquals(
        request.getAccounts().get(0).getAccountNumber(), this.newFormat.getAccountNumber());
    assertEquals(request.getAccounts().get(0).getBankId(), this.newFormat.getBankId());
    assertEquals(request.getAccounts().get(0).getId(), this.newFormat.getId());
  }
}
