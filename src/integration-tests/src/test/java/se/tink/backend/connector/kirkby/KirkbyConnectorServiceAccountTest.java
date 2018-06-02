package se.tink.backend.connector.kirkby;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.AccountListEntity;
import se.tink.backend.connector.transport.ConnectorAccountServiceJerseyTransport;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static se.tink.backend.connector.TestBase.accountsEqual;
import static se.tink.backend.connector.TestBase.createAccountEntity;
import static se.tink.backend.connector.TestBase.createAccountListEntity;

/**
 * TODO this is a unit test
 */
public class KirkbyConnectorServiceAccountTest extends KirkbyConnectorServiceIntegrationTest {

    private ConnectorAccountServiceJerseyTransport accountServiceTransport;
    private String defaultProviderName;

    @Before
    public void setUp() throws Exception {
        accountServiceTransport = injector.getInstance(ConnectorAccountServiceJerseyTransport.class);
        defaultProviderName = configuration.getConnector().getDefaultProviderName();
    }

    @Test
    public void accountCreateRequest_savesCorrectlyToDB() throws RequestException {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        AccountEntity accountEntity = createAccountEntity(5000d, AccountTypes.CHECKING, "accountNr1", "accountName1");
        AccountListEntity accountListEntity = createAccountListEntity(accountEntity);

        successfulPostStubFor("/update/accounts/update");

        accountServiceTransport.createAccounts(user.getUsername(), accountListEntity);

        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/update/accounts/update"))).get(0);
        UpdateAccountRequest updateAccountRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), UpdateAccountRequest.class);

        verify(1, postRequestedFor(urlEqualTo("/update/accounts/update")));

        assertEquals(credentials.getId(), updateAccountRequest.getCredentialsId());
        assertEquals(user.getId(), updateAccountRequest.getUser());

        Assert.assertTrue(accountsEqual(accountEntity, updateAccountRequest.getAccount()));
    }

    @Test
    public void accountCreateRequest_multipleAccounts_savesCorrectlyToDB() throws RequestException {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        List<AccountEntity> accountEntities = Lists.newArrayList(
                createAccountEntity(5000d, AccountTypes.CHECKING, "accountNr1", "accountName1"),
                createAccountEntity(3000d, AccountTypes.CHECKING, "accountNr2", "accountName2"));
        AccountListEntity accountListEntity = createAccountListEntity(accountEntities);

        successfulPostStubFor("/update/accounts/update");

        accountServiceTransport.createAccounts(user.getUsername(), accountListEntity);

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
}
