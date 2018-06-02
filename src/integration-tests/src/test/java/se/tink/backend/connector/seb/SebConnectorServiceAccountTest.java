package se.tink.backend.connector.seb;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;
import se.tink.backend.connector.rpc.seb.AccountEntity;
import se.tink.backend.connector.rpc.seb.AccountListEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.seb.utils.SEBUtils;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static se.tink.backend.connector.seb.SebConnectorTestBase.accountsEqual;
import static se.tink.backend.connector.seb.SebConnectorTestBase.createAccountEntity;
import static se.tink.backend.connector.seb.SebConnectorTestBase.createAccountListEntity;

/**
 * TODO this is a unit test
 */
public class SebConnectorServiceAccountTest extends SebConnectorServiceIntegrationTest {

    private SEBConnectorService sebConnectorService;

    @Before
    public void setUp() throws Exception {
        sebConnectorService = injector.getInstance(SEBConnectorServiceResource.class);
    }

    @Test
    public void accountCreateRequest_savesCorrectlyToDB() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        AccountEntity accountEntity = createAccountEntity(5000d, AccountTypes.CHECKING, "accountNr1", "accountName1");
        AccountListEntity accountListEntity = createAccountListEntity(accountEntity);

        successfulPostStubFor("/update/accounts/update");

        sebConnectorService.accounts(user.getUsername(), accountListEntity);

        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/update/accounts/update"))).get(0);

        UpdateAccountRequest updateAccountRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), UpdateAccountRequest.class);

        verify(1, postRequestedFor(urlEqualTo("/update/accounts/update")));

        assertEquals(credentials.getId(), updateAccountRequest.getCredentialsId());
        assertEquals(user.getId(), updateAccountRequest.getUser());

        Assert.assertTrue(accountsEqual(accountEntity, updateAccountRequest.getAccount()));
    }

    @Test
    public void accountCreateRequest_multipleAccounts_savesCorrectlyToDB() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        List<AccountEntity> accountEntities = Lists.newArrayList(
                createAccountEntity(5000d, AccountTypes.CHECKING, "accountNr1", "accountName1"),
                createAccountEntity(3000d, AccountTypes.CHECKING, "accountNr2", "accountName2"));
        AccountListEntity accountListEntity = createAccountListEntity(accountEntities);

        successfulPostStubFor("/update/accounts/update");

        sebConnectorService.accounts(user.getUsername(), accountListEntity);

        List<UpdateAccountRequest> requests = findAll(postRequestedFor(urlEqualTo("/update/accounts/update"))).stream()
                .map(r -> SerializationUtils.deserializeFromString(r.getBodyAsString(), UpdateAccountRequest.class))
                .collect(Collectors.toList());

        verify(2, postRequestedFor(urlEqualTo("/update/accounts/update")));

        requests.sort(Comparator.comparing(r -> r.getAccount().getBankId()));
        accountEntities.sort(Comparator.comparing(AccountEntity::getExternalId));

        for (int i = 0; i < requests.size(); i++) {
            Account account = requests.get(i).getAccount();
            assertEquals(credentials.getId(), account.getCredentialsId());
            assertEquals(user.getId(), account.getUserId());
            Assert.assertTrue(accountsEqual(accountEntities.get(i), account));
        }
    }

    @Test
    public void nullAccountList_generatesError() {
        User user = createUserAndSaveToDB();
        AccountListEntity accountListEntity = new AccountListEntity();
        accountListEntity.setAccounts(null);

        try {
            sebConnectorService.accounts(user.getUsername(), accountListEntity);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void nullAccountInList_generatesError() {
        User user = createUserAndSaveToDB();
        List<AccountEntity> accountEntities = Lists.newArrayList();
        accountEntities.add(null);
        AccountListEntity accountListEntity = createAccountListEntity(accountEntities);

        try {
            sebConnectorService.accounts(user.getUsername(), accountListEntity);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void nullBalanceOnAccount_doesNotSilentlyGenerateZeroButGeneratesError() {
        User user = createUserAndSaveToDB();
        AccountEntity accountEntity = createAccountEntity(null, AccountTypes.CHECKING, "accountNr1", "accountName1");

        try {
            sebConnectorService.accounts(user.getUsername(), createAccountListEntity(accountEntity));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }

        verify(0, postRequestedFor(urlEqualTo("/update/accounts/update")));
    }

    @Test
    public void nullOnRequiredFields_generatesError() {
        User user = createUserAndSaveToDB();
        AccountEntity accountEntity = createAccountEntity(5000d, null, "accountNr1", "accountName1");

        try {
            sebConnectorService.accounts(user.getUsername(), createAccountListEntity(accountEntity));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }

        accountEntity.setType(AccountTypes.CHECKING);
        accountEntity.setNumber(null);

        try {
            sebConnectorService.accounts(user.getUsername(), createAccountListEntity(accountEntity));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }

        accountEntity.setNumber("accountNr1");
        accountEntity.setName(null);

        try {
            sebConnectorService.accounts(user.getUsername(), createAccountListEntity(accountEntity));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }

        accountEntity.setName("accountName1");
        accountEntity.setExternalId(null);

        try {
            sebConnectorService.accounts(user.getUsername(), createAccountListEntity(accountEntity));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }

        verify(0, postRequestedFor(urlEqualTo("/update/accounts/update")));
    }
}
