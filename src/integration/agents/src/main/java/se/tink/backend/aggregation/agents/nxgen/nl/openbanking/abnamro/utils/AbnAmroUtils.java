package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AbnAmroUtils {

    public static String getDate() {
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(AbnAmroConstants.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static OAuth2Token getOauthToken(final PersistentStorage persistentStorage) {
        return persistentStorage
                .get(AbnAmroConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));
    }

    public static Optional<String> getAccountIdFromStorage(PersistentStorage persistentStorage) {
        return persistentStorage.get(AbnAmroConstants.StorageKey.ACCOUNT_ID, String.class);
    }

    public static void putAccountIdInStorage(
            String accountId, PersistentStorage persistentStorage) {
        persistentStorage.put(AbnAmroConstants.StorageKey.ACCOUNT_ID, accountId);
    }
}
