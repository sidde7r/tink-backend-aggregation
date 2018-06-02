package se.tink.backend.system.cli.debug;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.system.cli.debug.credentials.CredentialsTypeChanger;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChangeCredentialsTypeCommandTest {
    private static final CredentialsTypes PASSWORD_TYPE = CredentialsTypes.PASSWORD;
    private static final CredentialsTypes BANKID_TYPE = CredentialsTypes.MOBILE_BANKID;
    private static final String PASSWORD_PROVIDER_NAME = "lansforsakringar";
    private static final String BANKID_PROVIDER_NAME = "lansforsakringar-bankid";
    private static final String CREDENTIALS_ID = "credentialsId";
    private static final String USER_ID1 = "userId1";
    private static final String USER_ID2 = "userId2";
    private static final boolean IS_PROVIDERS_ON_AGGREGATION = false;

    private final Credentials credentials = new Credentials();
    private CredentialsRepository credentialsRepository;
    private ProviderRepository providerRepository;
    private CredentialsTypeChanger credentialsTypeChanger;
    private ChangeCredentialsTypeCommand changeCredentialsTypeCommand;

    @Before
    public void setUp() throws Exception {
        credentials.setType(PASSWORD_TYPE);
        credentials.setProviderName(PASSWORD_PROVIDER_NAME);

        credentialsRepository = mock(CredentialsRepository.class);
        providerRepository = mock(ProviderRepository.class);
        changeCredentialsTypeCommand = new ChangeCredentialsTypeCommand();

        AggregationControllerCommonClient aggregationControllerClient = mock(AggregationControllerCommonClient.class);
        credentialsTypeChanger = new CredentialsTypeChanger(credentialsRepository, providerRepository,
                IS_PROVIDERS_ON_AGGREGATION, aggregationControllerClient);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenUserId_isNull() {
        changeCredentialsTypeCommand.validateInput(null, CREDENTIALS_ID, BANKID_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenCredentialsId_isNull() {
        changeCredentialsTypeCommand.validateInput(USER_ID1, null, BANKID_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenTypeToChangeTo_isNull() {
        changeCredentialsTypeCommand.validateInput(USER_ID1, CREDENTIALS_ID, null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenCredentials_isNotFound() {
        changeCredentialsTypeCommand.credentialsRepository = credentialsRepository;
        Mockito.when(credentialsRepository.findOne(CREDENTIALS_ID)).thenReturn(null);
        changeCredentialsTypeCommand.getAndValidateCredentials(USER_ID1, CREDENTIALS_ID, BANKID_TYPE);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenCredentials_doesNotBelongToUser() {
        changeCredentialsTypeCommand.credentialsRepository = credentialsRepository;
        credentials.setUserId(USER_ID2);
        Mockito.when(credentialsRepository.findOne(CREDENTIALS_ID)).thenReturn(credentials);

        changeCredentialsTypeCommand.getAndValidateCredentials(USER_ID1, CREDENTIALS_ID, BANKID_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionIsThrown_whenCredentialsType_isAlreadySet() {
        changeCredentialsTypeCommand.credentialsRepository = credentialsRepository;
        credentials.setUserId(USER_ID1);
        credentials.setType(BANKID_TYPE);
        Mockito.when(credentialsRepository.findOne(CREDENTIALS_ID)).thenReturn(credentials);

        changeCredentialsTypeCommand.getAndValidateCredentials(USER_ID1, CREDENTIALS_ID, BANKID_TYPE);
    }

    @Test
    public void ensureCredentialsType_changed_whenNewProviderName_exists() {
        Provider provider = new Provider();
        Mockito.when(providerRepository.findByName(BANKID_PROVIDER_NAME)).thenReturn(provider);

        credentialsTypeChanger.changeCredentialsType(credentials, BANKID_TYPE);
        ArgumentCaptor<Credentials> argumentCaptor = ArgumentCaptor.forClass(Credentials.class);
        verify(credentialsRepository).saveAndFlush(argumentCaptor.capture());

        assertEquals(BANKID_PROVIDER_NAME, argumentCaptor.getValue().getProviderName());
        assertEquals(BANKID_TYPE, argumentCaptor.getValue().getType());
    }

    @Test(expected = NullPointerException.class)
    public void ensureException_isThrown_whenNewProviderName_notFound() {
        Mockito.when(providerRepository.findByName(anyString())).thenReturn(null);
        credentialsTypeChanger.changeCredentialsType(credentials, BANKID_TYPE);
    }

    @Test
    public void ensureCorrectConstructionOfProviderName_whenPassword_toBankId() {
        String newProviderName = credentialsTypeChanger.constructNewProviderName(PASSWORD_PROVIDER_NAME, BANKID_TYPE);
        assertEquals(BANKID_PROVIDER_NAME, newProviderName);
    }

    @Test
    public void ensureCorrectConstructionOfProviderName_whenbankId_toPassword() {
        String newProviderName = credentialsTypeChanger.constructNewProviderName(BANKID_PROVIDER_NAME, PASSWORD_TYPE);
        assertEquals(PASSWORD_PROVIDER_NAME, newProviderName);
    }
}
