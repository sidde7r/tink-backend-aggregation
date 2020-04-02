package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OAuth2TokenStorageDefaultImplTest {

    private PersistentStorage persistentStorage;
    private OAuth2Token token;
    private OAuth2TokenStorageDefaultImpl objectUnderTest;

    @Before
    public void init() {
        persistentStorage = new PersistentStorage();
        objectUnderTest = new OAuth2TokenStorageDefaultImpl(persistentStorage);
        initToken();
    }

    private void initToken() {
        token = new OAuth2Token(System.currentTimeMillis() / 1000);
        token.setAccessToken("accessToken");
        token.setRefreshToken("refreshToken");
        token.setExpiresIn(3600l);
        token.setTokenType("Bearer");
        token.setScope("scope");
    }

    @Test
    public void shouldFetchPersistedToken() {
        // given
        objectUnderTest.storeToken(token);
        // when
        Optional<OAuth2Token> result = objectUnderTest.fetchToken();
        // then
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(token, result.get());
    }
}
