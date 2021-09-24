package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public abstract class SibsBaseTransactionFetcher {

    protected static final String ENCODED_SPACE = "%20";
    public static final int DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD = 89;
    public static final LocalDate BIG_BANG_DATE = LocalDate.of(1970, 1, 1);

    protected final SibsBaseApiClient apiClient;
    private final CredentialsRequest credentialsRequest;
    private final SibsUserState userState;
    private final LocalDateTimeSource localDateTimeSource;

    public LocalDate getTransactionsFetchBeginDate(final Account account) {
        LocalDate updateDate = getCertainDate(account).orElse(BIG_BANG_DATE);

        if (isFetchingFromBeginningNotAllowed(updateDate)
                || certainDateWasOlderThan90DaysBack(updateDate)) {
            updateDate =
                    localDateTimeSource
                            .now()
                            .toLocalDate()
                            .minusDays(DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD);
        }
        return updateDate;
    }

    private boolean isFetchingFromBeginningNotAllowed(LocalDate updateDate) {
        return isDateABigBang(updateDate) && userState.getConsent().isConsentOlderThan30Minutes();
    }

    private boolean certainDateWasOlderThan90DaysBack(LocalDate updateDate) {
        return !isDateABigBang(updateDate)
                && DAYS.between(updateDate, localDateTimeSource.now().toLocalDate())
                        > DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD;
    }

    private boolean isDateABigBang(LocalDate date) {
        return date.getYear() == BIG_BANG_DATE.getYear();
    }

    private Optional<LocalDate> getCertainDate(Account account) {
        return credentialsRequest.getAccounts().stream()
                .filter(rpcAccount -> account.isUniqueIdentifierEqual(rpcAccount.getBankId()))
                .findAny()
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .map(d -> new java.sql.Date(d.getTime()).toLocalDate());
    }
}
