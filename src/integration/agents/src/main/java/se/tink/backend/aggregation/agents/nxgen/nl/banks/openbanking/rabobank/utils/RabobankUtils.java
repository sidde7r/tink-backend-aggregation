package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankUtils {

    public static OAuth2Token getOauthToken(final PersistentStorage persistentStorage) {
        return persistentStorage
                .get(StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));
    }

    public static void removeOauthToken(final PersistentStorage persistentStorage) {
        persistentStorage.remove(StorageKey.OAUTH_TOKEN);
    }

    public static void removeConsent(final PersistentStorage persistentStorage) {
        persistentStorage.remove(StorageKey.CONSENT_ID);
    }

    public static String getRefreshTokenExpireDate(final Long refreshTokenExpiresInSeconds) {
        return Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .plusSeconds(refreshTokenExpiresInSeconds)
                .toLocalDate()
                .toString();
    }

    public static String createSignatureString(
            final String date, final String digest, final String requestId) {
        String result = Signature.SIGNING_STRING_DATE + date + "\n";
        result +=
                Signature.SIGNING_STRING_DIGEST + Signature.SIGNING_STRING_SHA_512 + digest + "\n";
        result += Signature.SIGNING_STRING_REQUEST_ID + requestId;
        return result;
    }

    public static String createSignatureHeader(
            final String keyId,
            final String algorithm,
            final String b64Signature,
            final String headersValue) {
        return new RabobankSignatureHeader(keyId, algorithm, b64Signature, headersValue).toString();
    }
}
