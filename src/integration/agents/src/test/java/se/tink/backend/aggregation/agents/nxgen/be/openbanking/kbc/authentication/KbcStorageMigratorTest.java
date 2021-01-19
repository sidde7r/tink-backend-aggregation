package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class KbcStorageMigratorTest {

    private KbcStorageMigrator objectUnderTest;
    private PersistentStorage persistentStorage;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void init() {
        objectUnderTest = new KbcStorageMigrator(objectMapper);
        persistentStorage = new PersistentStorage();
    }

    @Test
    public void shouldMigrateFullSetOfData() {
        // given
        OAuth2Token oAuth2Token =
                OAuth2Token.create("bearer", "testDummyAccessToken", "testDummyRefreshToken", 360);
        String consentId = "testDummyConsentId";
        String codeVerifier = "testDummyCodeVerifier";
        persistentStorage.put(BerlinGroupConstants.StorageKeys.CONSENT_ID, consentId);
        persistentStorage.put(BerlinGroupConstants.StorageKeys.CODE_VERIFIER, codeVerifier);
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory =
                new KbcPersistedDataAccessorFactory(objectMapper);
        // when
        AgentAuthenticationPersistedData result = objectUnderTest.migrate(persistentStorage);
        // then
        KbcAuthenticationData kbcAuthenticationData =
                kbcPersistedDataAccessorFactory
                        .createKbcAuthenticationPersistedDataAccessor(result)
                        .getKbcAuthenticationData();
        Assertions.assertThat(kbcAuthenticationData.getConsentId()).isEqualTo(consentId);
        Assertions.assertThat(kbcAuthenticationData.getCodeVerifier()).isEqualTo(codeVerifier);
        AgentRefreshableAccessTokenAuthenticationPersistedData
                redirectTokensAuthenticationPersistedData =
                        new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                        result);
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .isPresent())
                .isTrue();
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .get()
                                .getAccessToken()
                                .getBody())
                .isEqualTo(oAuth2Token.getAccessToken().getBytes());
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .get()
                                .getAccessToken()
                                .getIssuedAtInSeconds())
                .isEqualTo(oAuth2Token.getIssuedAt());
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .get()
                                .getAccessToken()
                                .getExpiresInSeconds())
                .isEqualTo(oAuth2Token.getExpiresInSeconds());
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .get()
                                .getAccessToken()
                                .getTokenType())
                .isEqualTo(oAuth2Token.getTokenType());
        Assertions.assertThat(
                        redirectTokensAuthenticationPersistedData
                                .getRefreshableAccessToken()
                                .get()
                                .getRefreshToken()
                                .getBody())
                .isEqualTo(oAuth2Token.getRefreshToken().get().getBytes());
    }
}
