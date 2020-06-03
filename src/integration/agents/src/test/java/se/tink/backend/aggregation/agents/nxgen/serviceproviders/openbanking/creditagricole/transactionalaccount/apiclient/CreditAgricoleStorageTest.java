package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.ACCESS_TOKEN;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleStorageTest {

    private CreditAgricoleStorage creditAgricoleStorage;

    private PersistentStorage persistentStorageMock;

    @Before
    public void setUp() {
        persistentStorageMock = mock(PersistentStorage.class);

        creditAgricoleStorage = new CreditAgricoleStorage(persistentStorageMock);
    }

    @Test
    public void shouldReturnTokenFromStorage() {
        // given
        final OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        when(oAuth2Token.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(persistentStorageMock.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        // when
        final String returnedResponse = creditAgricoleStorage.getTokenFromStorage();

        // then
        assertThat(returnedResponse).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    public void shouldReturnIllegalStateExceptionWhenStorageHasNoToken() {
        // given
        when(persistentStorageMock.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(creditAgricoleStorage::getTokenFromStorage);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(CreditAgricoleBaseConstants.ErrorMessages.UNABLE_LOAD_OAUTH_TOKEN);
    }
}
