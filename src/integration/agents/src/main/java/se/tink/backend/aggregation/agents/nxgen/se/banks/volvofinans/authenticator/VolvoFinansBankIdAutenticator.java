package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.SavingsAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc.CustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc.ErrorStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VolvoFinansBankIdAutenticator implements BankIdAuthenticator<String> {

    private final VolvoFinansApiClient apiClient;
    private final SessionStorage sessionStorage;

    public VolvoFinansBankIdAutenticator(VolvoFinansApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        try {
            HttpResponse httpResponse = apiClient.loginBankIdInit(new InitBankIdRequest(ssn));
            String location = httpResponse.getHeaders().getFirst(VolvoFinansConstants.Headers.HEADER_LOCATION);
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();

            if (httpResponse.getStatus() == HttpStatus.SC_CONFLICT) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);

                if (errorResponse.isBankIdAlreadyInProgressError()) {
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                }
            }

            throw hre;
        }
    }

    @Override
    public BankIdStatus collect(String identificationId) throws AuthenticationException {
        BankIdStatus bankIdStatus = apiClient.loginBankIdPoll(identificationId).getBankIdStatus();

        if (bankIdStatus.equals(BankIdStatus.DONE)) {
            try {
                CustomerResponse customerResponse = apiClient.keepAlive();
                if (isBankOpen(customerResponse)) {
                    sessionStorage.put(VolvoFinansConstants.Storage.CUSTOMER, customerResponse);
                }
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                    throw LoginError.NOT_CUSTOMER.exception();
                }
                throw e;
            }
        }

        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    // this is a somewhat lengthy validation if the bank service is closed for service
    private boolean isBankOpen(CustomerResponse customerResponse) throws AuthenticationException {
        if (customerResponse.hasCard()) {
            return canFetchCardTransactions();
        } else if (customerResponse.hasSavings()) {
            return canFetchSavingsTransactions();
        }

        return true;
    }

    private boolean canFetchSavingsTransactions() throws AuthenticationException {
        SavingsAccountsResponse savingsAccounts = null;

        try {
            savingsAccounts = apiClient.savingsAccounts();
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return true;
            }

            throw hre;
        }

        Optional<String> accountId = Optional.ofNullable(savingsAccounts)
                .orElse(new SavingsAccountsResponse())
                .stream()
                .map(SavingsAccountEntity::getAccountId)
                .findFirst();

        return tryFetchTransactions(true, accountId);
    }

    private boolean canFetchCardTransactions() throws AuthenticationException {
        CreditCardsResponse creditCards = apiClient.creditCardAccounts();

        Optional<String> accountId = Optional.ofNullable(creditCards)
                .orElse(new CreditCardsResponse())
                .stream()
                .map(CreditCardEntity::getAccountId)
                .findFirst();

        return tryFetchTransactions(false, accountId);
    }

    private boolean tryFetchTransactions(boolean savings, Optional<String> accountId) throws AuthenticationException {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = LocalDate.now().minusDays(30);

        if (!accountId.isPresent()) {
            return true;
        }

        try {
            if (savings) {
                apiClient.savingsAccountTransactions(accountId.get(), fromDate, toDate,
                        VolvoFinansConstants.Pagination.LIMIT, 0);
            } else {
                apiClient.creditCardAccountTransactions(accountId.get(), fromDate, toDate,
                        VolvoFinansConstants.Pagination.LIMIT, 0);
            }
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                if (response.hasBody() && response.getBody(ErrorStatusResponse.class).isBankServiceClosed()) {
                    throw BankServiceError.NO_BANK_SERVICE.exception();
                }
            }
        }

        return true;
    }
}
