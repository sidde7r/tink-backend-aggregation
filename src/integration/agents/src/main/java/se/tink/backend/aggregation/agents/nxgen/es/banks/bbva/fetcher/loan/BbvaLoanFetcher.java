package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.control.Try.run;

import io.vavr.collection.List;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan.BaseLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan.ConsumerLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan.MortgageEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BbvaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaLoanFetcher.class);

    private final BbvaApiClient apiClient;

    public BbvaLoanFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        ArrayList<LoanAccount> loans = new ArrayList<>();

        List<PositionEntity> positions = apiClient.fetchFinancialDashboard().getPositions();

        java.util.List<LoanAccount> consumerLoans =
                positions
                        .map(PositionEntity::getContract)
                        .flatMap(ContractEntity::getLoan)
                        .map(this::getConsumerLoanFromDetails)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        java.util.List<LoanAccount> mortgages =
                positions
                        .map(PositionEntity::getContract)
                        .flatMap(ContractEntity::getMortgage)
                        .map(this::getMortgageFromDetails)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        loans.addAll(consumerLoans);
        loans.addAll(mortgages);

        return loans;
    }

    private Optional<LoanAccount> getConsumerLoanFromDetails(ConsumerLoanEntity loan) {

        return Try.of(() -> apiClient.fetchLoanDetails(loan.getId()))
                .onFailure(HttpResponseException.class, handleFetchLoanDetailsException(loan))
                .fold(error -> Optional.empty(), loan::toTinkConsumerLoan);
    }

    private Optional<LoanAccount> getMortgageFromDetails(MortgageEntity loan) {
        return Try.of(() -> apiClient.fetchLoanDetails(loan.getId()))
                .onFailure(HttpResponseException.class, handleFetchLoanDetailsException(loan))
                .fold(error -> Optional.empty(), loan::toTinkMortgage);
    }

    private Consumer<HttpResponseException> handleFetchLoanDetailsException(BaseLoanEntity loan) {
        return e -> {
            final HttpResponse res = e.getResponse();
            Match(res.getStatus())
                    .of(
                            Case($(409), run(() -> logLoanDetailsError(res, loan))),
                            Case($(500), run(() -> handleBankServiceError(res, e))),
                            // Throw e if we have no match.
                            Case(
                                    $(),
                                    () -> {
                                        throw e;
                                    }));
        };
    }

    private void handleBankServiceError(HttpResponse res, HttpResponseException e) {
        if (findTempUnavailableText(res)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }
    }

    private boolean findTempUnavailableText(HttpResponse res) {
        String htmlBody = res.getBody(String.class);
        return Pattern.compile(
                        Pattern.quote(ErrorMessages.TEMPORARILY_UNAVAILABLE),
                        Pattern.CASE_INSENSITIVE)
                .matcher(htmlBody)
                .find();
    }

    private void logLoanDetailsError(HttpResponse res, BaseLoanEntity loan) {
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
