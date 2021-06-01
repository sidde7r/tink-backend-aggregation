package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.CONSENT_VALIDITY_IN_DAYS;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentExpiredException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentRevokedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@AllArgsConstructor
public class SparebankAuthenticator {

    private final SparebankApiClient apiClient;
    private final SparebankStorage storage;
    private final Credentials credentials;
    private final LocalDateTimeSource dateSource;

    public URL buildAuthorizeUrl(String state) {
        return apiClient
                .getScaRedirect(state)
                .getRedirectUri()
                .filter(StringUtils::isNotBlank)
                .map(URL::new)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparebankConstants.ErrorMessages.SCA_REDIRECT_MISSING));
    }

    boolean hasSessionExpired() {
        if (!isSessionDataPresent()) {
            log.info("[SpareBank] Session expired - missing session data");
            return true;
        }
        if (hasReachedSessionExpiryDate()) {
            log.info("[SpareBank] Session expired - reached session expiry date");
            return true;
        }
        if (!isTppSessionStillValid()) {
            log.info("[SpareBank] Session expired - TPP session invalid");
            return true;
        }
        return false;
    }

    void storeSessionData(String psuId, String tppSessionId) {
        storage.storePsuId(psuId);
        storage.storeTppSessionId(tppSessionId);

        long now = Instant.now().toEpochMilli();
        log.info("[SpareBank] Consent creation timestamp: {}", now);
        storage.storeConsentCreationTimestamp(now);
    }

    void clearSessionData() {
        storage.clearSessionData();
    }

    void handleSuccessfulManualAuth() {
        log.info(
                "[SpareBank] Manual authentication finished - fetching accounts and cards for future auto refreshes");
        apiClient.fetchAccounts();
        apiClient.fetchCards();
    }

    private boolean isSessionDataPresent() {
        if (!storage.getPsuId().isPresent()) {
            log.info("Session expired - missing PSU id");
            return false;
        }
        if (!storage.getSessionId().isPresent()) {
            log.info("Session expired - missing session id");
            return false;
        }
        if (!storage.getConsentCreationTimestamp().isPresent()) {
            log.info("Session expired - missing consent creation timestamp");
            return false;
        }
        return true;
    }

    private boolean hasReachedSessionExpiryDate() {
        LocalDateTime sessionExpiryDate = getSessionExpiryDateOrSetIfMissing();
        return !dateSource.now().isBefore(sessionExpiryDate);
    }

    private LocalDateTime getSessionExpiryDateOrSetIfMissing() {
        if (credentials.getSessionExpiryDate() != null) {
            return dateToLocalDateTime(credentials.getSessionExpiryDate());
        }
        log.info("[SpareBank] Setting missing session expiry date");

        long consentCreationTs =
                storage.getConsentCreationTimestamp()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing consent creation timestamp"));

        LocalDateTime consentCreationDate =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(consentCreationTs), TimeZone.getDefault().toZoneId());

        LocalDateTime sessionExpiryDate = consentCreationDate.plusDays(CONSENT_VALIDITY_IN_DAYS);

        credentials.setSessionExpiryDate(sessionExpiryDate.toLocalDate());
        return sessionExpiryDate;
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private boolean isTppSessionStillValid() {
        Optional<AccountResponse> maybeAccounts = storage.getStoredAccounts();
        Optional<CardResponse> maybeCards = storage.getStoredCards();

        if (!maybeAccounts.isPresent() && !maybeCards.isPresent()) {
            log.info("[SpareBank] TPP session invalid - empty accounts & cards storage");
            return false;
        }
        try {
            // ITE-1648 No other way to validate the session (that I know of) than to run true
            // operation.
            // We fetch first account/card balance and store it, then when actual balance fetching
            // occurs we retrieve balance for first account from storage and remove it. This logic
            // helps us to limit balance fetching request and in result increase the number of
            // background refreshes
            Optional<String> maybeResourceId = Optional.empty();
            if (maybeAccounts.isPresent()) {
                maybeResourceId =
                        maybeAccounts.get().getAccounts().stream()
                                .map(AccountEntity::getResourceId)
                                .findFirst();
            }
            if (!maybeResourceId.isPresent() && maybeCards.isPresent()) {
                maybeResourceId =
                        maybeCards.get().getCardAccounts().stream()
                                .map(CardEntity::getResourceId)
                                .findFirst();
            }

            if (maybeResourceId.isPresent()) {
                String resourceId = maybeResourceId.get();
                BalanceResponse balanceResponse = apiClient.fetchBalances(resourceId);
                storage.storeBalanceResponse(resourceId, balanceResponse);
                return true;
            } else {
                log.info("[SpareBank] TPP session invalid - no accounts or cards in storage");
                return false;
            }
        } catch (ConsentExpiredException | ConsentRevokedException e) {
            log.info(
                    "[SpareBank] TPP session invalid - fetch balances unauthorized error. Consent creation ts: {}",
                    storage.getConsentCreationTimestamp());
            // We are sure that the session is invalid and will require full auth again
            return false;
        }
    }
}
