package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public final class VolksbankUtils {

    private VolksbankUtils() {
        throw new AssertionError();
    }

    public static Map<String, String> convertURLQueryToMap(String query) {
        if (Strings.isNullOrEmpty(query)) {
            return Collections.emptyMap();
        }
        return Splitter.on('&').trimResults().withKeyValueSeparator('=').split(query);
    }

    public static boolean isEntryReferenceFromAfterDate(String entryReferenceFrom, Date date) {
        final SimpleDateFormat entryReferenceDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            return entryReferenceDateFormat
                            .parse(entryReferenceFrom.substring(0, 8))
                            .compareTo(date)
                    > 0;
        } catch (RuntimeException | ParseException e) {
            log.warn("Unable to parse entryReferenceFrom to date: {}", entryReferenceFrom);
            return true;
        }
    }

    public static OAuth2Token getOAuth2TokenFromStorage(PersistentStorage persistentStorage) {
        return persistentStorage
                .get(VolksbankConstants.Storage.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Missing OAuth2 Token!"));
    }

    public static String getConsentIdFromStorage(PersistentStorage persistentStorage) {
        final String consentId = persistentStorage.get(VolksbankConstants.Storage.CONSENT);
        if (Strings.isNullOrEmpty(consentId)) {
            throw new IllegalStateException("Consent ID was not found, can't fetch accounts.");
        }
        return consentId;
    }

    public static String getAccountNumber(String iban) {
        return iban.replace(" ", "").substring(8);
    }
}
