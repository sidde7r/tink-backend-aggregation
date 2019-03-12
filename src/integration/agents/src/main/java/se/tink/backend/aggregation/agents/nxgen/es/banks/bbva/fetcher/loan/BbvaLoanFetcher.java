package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan;

import io.vavr.control.Try;
import java.util.Collection;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.control.Try.run;

public class BbvaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaLoanFetcher.class);

    private final BbvaApiClient apiClient;

    public BbvaLoanFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient
                .fetchFinancialDashboard()
                .getPositions()
                .map(PositionEntity::getContract)
                .flatMap(ContractEntity::getLoan)
                .map(this::enrichLoanAccountWithDetails)
                .toJavaList();
    }

    private LoanAccount enrichLoanAccountWithDetails(LoanEntity loan) {
        return Try.of(() -> apiClient.fetchLoanDetails(loan.getId()))
                .onFailure(HttpResponseException.class, handleFetchLoanDetailsException(loan))
                .fold(
                        error -> loan.toTinkLoanAccount(),
                        loanDetails -> loanDetails.toTinkLoanAccount(loan));
    }

    private Consumer<HttpResponseException> handleFetchLoanDetailsException(LoanEntity loan) {
        return e -> {
            final HttpResponse res = e.getResponse();
            Match(res.getStatus()).of(Case($(409), run(() -> logLoanDetailsError(res, loan))));
        };
    }

    private void logLoanDetailsError(HttpResponse res, LoanEntity loan) {
        final BbvaErrorResponse errorResponse = res.getBody(BbvaErrorResponse.class);

        LOGGER.warn(
                String.format(
                        "%s: Couldn't fetching loan details for loan %s; Error Code: %s; Message: %s",
                        BbvaConstants.LogTags.LOAN_DETAILS,
                        loan.getId(),
                        errorResponse.getErrorCode(),
                        errorResponse.getErrorMessage()));
    }
}
