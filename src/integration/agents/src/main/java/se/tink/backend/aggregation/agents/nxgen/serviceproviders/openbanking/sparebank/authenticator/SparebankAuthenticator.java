package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.CONSENT_VALIDITY_IN_DAYS;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        if (!isAccountOrCardResponseStored()) {
            log.info("[SpareBank] TPP session invalid - empty accounts & cards storage");
            return false;
        }

        List<String> storedResourceIds = getResourceIdsOfStoredAccountsAndCards();
        if (storedResourceIds.isEmpty()) {
            log.info("[SpareBank] TPP session invalid - no accounts or cards in storage");
            return false;
        }
        String resourceId = storedResourceIds.get(0);

        /*
        ITE-2621
        - consent id and its status are handled internally by Evry - we cannot check them
        - the only way of checking if our session is really valid is to make a real request
        - fetching accounts & cards is available only for the 1st hour after consent creation, so we cannot use it
        - every resourceId has its own separate background refreshes counters for balances and transactions
        - because of that, when we make a real call here, it counts as a BG refresh, so we need to cache the response
        - transactions response may be too big, so we need to fetch balances
        - currently API allows to fetch balances with expired token and we can only check if consent was revoked
          (but it might be change in the future)
         */
        try {
            BalanceResponse balanceResponse = apiClient.fetchBalances(resourceId);
            storage.storeBalanceResponse(resourceId, balanceResponse);
            return true;

        } catch (ConsentRevokedException e) {
            log.info(
                    "[SpareBank] TPP session invalid - consent revoked. Consent creation ts: {}",
                    storage.getConsentCreationTimestamp());
            return false;

        } catch (ConsentExpiredException e) {
            log.info(
                    "[SpareBank] TPP session invalid - consent expired. Consent creation ts: {}",
                    storage.getConsentCreationTimestamp());
            return false;
        }
    }

    private boolean isAccountOrCardResponseStored() {
        return storage.getStoredAccounts().isPresent() || storage.getStoredCards().isPresent();
    }

    private List<String> getResourceIdsOfStoredAccountsAndCards() {
        Stream<String> accountResourceIds =
                storage.getStoredAccounts().orElse(AccountResponse.empty()).getAccounts().stream()
                        .map(AccountEntity::getResourceId);

        Stream<String> cardResourceIds =
                storage.getStoredCards().orElse(CardResponse.empty()).getCardAccounts().stream()
                        .map(CardEntity::getResourceId);

        return Stream.concat(accountResourceIds, cardResourceIds).collect(Collectors.toList());
    }
}
