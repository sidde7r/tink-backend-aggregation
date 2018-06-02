package se.tink.backend.main.resources;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import se.tink.backend.api.LoanService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEventsResponse;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.core.LoanTimelineResponse;
import se.tink.backend.core.UpdateLoanRequest;
import se.tink.backend.main.controllers.LoanServiceController;
import se.tink.backend.main.controllers.loans.exceptions.AccountExcludedClosedOrNotLoanAccountException;
import se.tink.backend.main.controllers.loans.exceptions.AccountNotFoundException;
import se.tink.backend.main.controllers.loans.exceptions.IdsNotEqualException;
import se.tink.backend.main.controllers.loans.exceptions.LoanNotFoundException;
import se.tink.backend.rpc.loans.GetLoanEventsCommand;
import se.tink.backend.rpc.loans.GetLoanTimelinesCommand;
import se.tink.backend.rpc.loans.ListLoansCommand;
import se.tink.backend.rpc.loans.UpdateLoansCommand;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

@Path("/api/v1/loans")
public class LoanServiceResource implements LoanService {
    private final AccountRepository accountRepository;
    private final LoanDataRepository loanDataRepository;
    private final LoanDAO loanDAO;
    private LoanServiceController loanServiceController;

    public LoanServiceResource(ServiceContext context) {
        this.accountRepository = context.getRepository(AccountRepository.class);
        this.loanDataRepository = context.getRepository(LoanDataRepository.class);
        this.loanDAO = context.getDao(LoanDAO.class);
        this.loanServiceController = new LoanServiceController(loanDataRepository, accountRepository, loanDAO);
    }

    @Override
    @Timed
    public LoanResponse get(AuthenticatedUser authenticatedUser) {
        return loanServiceController.list(new ListLoansCommand(authenticatedUser.getUser().getId()));
    }

    @Override
    @Timed
    public Loan update(AuthenticatedUser authenticatedUser, UpdateLoanRequest updateLoanRequest) {
        if (updateLoanRequest == null) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        UpdateLoansCommand command = UpdateLoansCommand.builder()
                .withUserId(authenticatedUser.getUser().getId())
                .withAccountId(updateLoanRequest.getAccountId())
                .withLoanType(updateLoanRequest.getLoanType())
                .withInterest(updateLoanRequest.getInterest())
                .withBalance(updateLoanRequest.getBalance())
                .build();

        try {
            return loanServiceController.update(command);
        } catch (AccountNotFoundException e) {
            HttpResponseHelper.error(Response.Status.NOT_FOUND);
        } catch (IdsNotEqualException e) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        } catch (AccountExcludedClosedOrNotLoanAccountException e) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        } catch (LoanNotFoundException e) {
            HttpResponseHelper.error(Response.Status.NOT_FOUND);
        }
        return null;
    }

    @Override
    @Timed
    public LoanEventsResponse getEvents(AuthenticatedUser authenticatedUser) {
        try {
            return loanServiceController.getEvents(
                    new GetLoanEventsCommand(authenticatedUser.getUser().getId(), authenticatedUser.getLocale()));
        } catch (InvalidLocaleException e) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }
        return null;
    }

    @Override
    @Timed
    public LoanTimelineResponse getLoanTimelines(AuthenticatedUser authenticatedUser) {
        return loanServiceController.getLoanTimelines(new GetLoanTimelinesCommand(authenticatedUser.getUser().getId()));
    }
}
