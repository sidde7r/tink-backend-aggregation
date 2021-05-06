package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static io.vavr.Predicates.not;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@AllArgsConstructor
public class SparebankAuthenticator {

    private final SparebankApiClient apiClient;
    private final SparebankStorage storage;

    public URL buildAuthorizeUrl(String state) {
        return Try.of(() -> apiClient.getScaRedirect(state))
                .map(ScaResponse::getRedirectUri)
                .recoverWith(HttpResponseException.class, this::maybeGetRedirectUri)
                .filter(not(Strings::isNullOrEmpty))
                .map(URL::new)
                .getOrElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparebankConstants.ErrorMessages.SCA_REDIRECT_MISSING));
    }

    private Try<String> maybeGetRedirectUri(HttpResponseException e) {
        return isExceptionWithScaRedirect(e)
                ? Try.success(e.getResponse().getBody(ScaResponse.class).getRedirectUri())
                : Try.failure(e);
    }

    private boolean isExceptionWithScaRedirect(HttpResponseException e) {
        return e.getResponse().hasBody()
                && e.getResponse().getBody(String.class).contains("scaRedirect");
    }

    void storeSessionData(String psuId, String tppSessionId) {
        storage.storePsuId(psuId);
        storage.storeTppSessionId(tppSessionId);

        long now = Instant.now().toEpochMilli();
        log.info("Consent creation timestamp: {}", now);
        storage.storeConsentCreationTimestamp(now);
    }

    void clearSessionData() {
        storage.clearSessionData();
    }

    boolean psuAndSessionPresent() {
        return storage.getPsuId().isPresent() && storage.getSessionId().isPresent();
    }

    boolean isTppSessionStillValid() {
        Optional<AccountResponse> maybeAccounts = storage.getStoredAccounts();
        Optional<CardResponse> maybeCards = storage.getStoredCards();

        if (!maybeAccounts.isPresent() && !maybeCards.isPresent()) {
            log.info("TPP session invalid - empty accounts & cards storage");
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
                log.info("TPP session invalid - no accounts or cards in storage");
                return false;
            }
        } catch (HttpResponseException e) {
            if (isExceptionWithScaRedirect(e)) {
                log.info("TPP session invalid - fetch balances unauthorized error");
                // We are sure that the session is invalid and will require full auth again
                return false;
            } else {
                // Something else gone wrong, we don't want to invalidate the session just yet
                throw e;
            }
        }
    }
}
