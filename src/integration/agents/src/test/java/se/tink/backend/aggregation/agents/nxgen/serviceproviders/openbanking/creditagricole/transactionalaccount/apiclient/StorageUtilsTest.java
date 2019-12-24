package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StorageUtilsTest {

    @Rule public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldReturnTokenFromStorage() {
        // given
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        String accessToken = "accessToken";

        when(oAuth2Token.getAccessToken()).thenReturn(accessToken);
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        // when
        String resp = StorageUtils.getTokenFromStorage(persistentStorage);

        // then
        assertEquals(accessToken, resp);
    }

    @Test
    public void shouldReturnIllegalStateExceptionWhenStorageHasNoToken() {
        // given
        PersistentStorage persistentStorage = mock(PersistentStorage.class);

        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.empty());

        // then
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage(
                CreditAgricoleBaseConstants.ErrorMessages.UNABLE_LOAD_OAUTH_TOKEN);

        // when
        String resp = StorageUtils.getTokenFromStorage(persistentStorage);
    }
}
