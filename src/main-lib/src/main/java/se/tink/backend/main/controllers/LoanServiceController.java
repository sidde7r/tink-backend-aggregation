package se.tink.backend.main.controllers;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEvent;
import se.tink.backend.core.LoanEventsResponse;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.core.LoanTimeline;
import se.tink.backend.core.LoanTimelineResponse;
import se.tink.backend.core.TemporalValue;
import se.tink.backend.core.TemporalValueUtils;
import se.tink.backend.main.controllers.loans.exceptions.AccountExcludedClosedOrNotLoanAccountException;
import se.tink.backend.main.controllers.loans.exceptions.AccountNotFoundException;
import se.tink.backend.main.controllers.loans.exceptions.IdsNotEqualException;
import se.tink.backend.main.controllers.loans.exceptions.LoanNotFoundException;
import se.tink.backend.rpc.loans.GetLoanEventsCommand;
import se.tink.backend.rpc.loans.GetLoanTimelinesCommand;
import se.tink.backend.rpc.loans.ListLoansCommand;
import se.tink.backend.rpc.loans.UpdateLoansCommand;
import se.tink.backend.utils.LoanUtils;
import se.tink.backend.utils.ValueWeight;

public class LoanServiceController {
    private final LoanDataRepository loanDataRepository;
    private final AccountRepository accountRepository;
    private final LoanDAO loanDAO;

    @Inject
    public LoanServiceController(LoanDataRepository loanDataRepository, AccountRepository accountRepository,
            LoanDAO loanDAO) {
        this.loanDataRepository = loanDataRepository;
        this.accountRepository = accountRepository;
        this.loanDAO = loanDAO;
    }

    public LoanResponse list(ListLoansCommand command) {
        List<Account> accounts = accountRepository.findByUserId(command.getUserId());

        List<Loan> loans =
                accounts.stream()
                        .filter(a -> !a.isExcluded())
                        .filter(LoanServiceController::isLoan)
                        .map(a -> loanDataRepository.findMostRecentOneByAccountId(a.getId()))
                        .filter(Objects::nonNull)
                        .map(LoanServiceController::cleanupInternalData)
                        .collect(Collectors.toList());

        Double loanSum = LoanUtils.sumBalance(loans);
        Double interestRateWeightedAverage = LoanUtils.interestRateWeightedAverage(loans);

        LoanResponse response = new LoanResponse(loans);
        response.setTotalLoanAmount(loanSum);
        response.setWeightedAverageInterestRate(interestRateWeightedAverage);
        return response;
    }

    private static boolean isLoan(Account a) {
        return Objects.equals(a.getType(), AccountTypes.LOAN) || Objects.equals(a.getType(), AccountTypes.MORTGAGE);
    }

    private static Loan cleanupInternalData(Loan loan) {
        loan.setSerializedLoanResponse(null);
        return loan;
    }

    public Loan update(UpdateLoansCommand command)
            throws AccountNotFoundException, IdsNotEqualException, AccountExcludedClosedOrNotLoanAccountException,
            LoanNotFoundException {

        Account account = accountRepository.findOne(command.getAccountId());

        if (account == null) {
            throw new AccountNotFoundException();
        }

        if (!Objects.equals(command.getUserId(), account.getUserId())) {
            throw new IdsNotEqualException();
        }

        if (account.isExcluded() || account.isClosed() || !isLoan(account)) {
            // The correct status would be `FORBIDDEN`, but that would enable a malicious invoker to distinguish between
            // this and whether an account exists with a given id.
            throw new AccountExcludedClosedOrNotLoanAccountException();
        }

        Loan updatedLoan = loanDataRepository.findMostRecentOneByAccountId(command.getAccountId());

        if (updatedLoan == null) {
            throw new LoanNotFoundException();
        }

        updatedLoan.setInterest(command.getInterest());
        updatedLoan.setBalance(command.getBalance());
        updatedLoan.setType(command.getLoanType());

        loanDAO.saveIfUpdated(updatedLoan);
        return cleanupInternalData(updatedLoan);
    }

    private List<LoanEvent> getAllLoanEventsByAccountId(String accountId, String userLocale) {
        List<Loan> loanStates = loanDAO.getLoanDataByAccountId(accountId);

        return LoanUtils.createLoanEvents(loanStates, userLocale);
    }

    public LoanEventsResponse getEvents(GetLoanEventsCommand command) {
        List<LoanEvent> events = accountRepository.findByUserId(command.getUserId()).stream()
                .filter(a -> !a.isExcluded() && isLoan(a))
                .flatMap(a -> getAllLoanEventsByAccountId(a.getId(), command.getLocale()).stream())
                .collect(Collectors.toList());

        LoanEventsResponse response = new LoanEventsResponse();
        response.setLoanEvents(events);
        return response;
    }

    public LoanTimeline convertTemporalLoanStatesToLoanTimelines(List<TemporalValue<Loan>> temporalLoanStates) {
        LoanTimeline loanTimeline = new LoanTimeline();
        List<TemporalValue<Loan>> timeline = TemporalValueUtils.convertTemporalValuesToTimeline(temporalLoanStates);
        List<TemporalValue<Double>> interestTimeline = timeline.stream()
                .map(tempLoanVal -> new TemporalValue<>(tempLoanVal.getDate(), tempLoanVal.getValue().getInterest()))
                .collect(Collectors.toList());
        List<TemporalValue<Double>> balanceTimeline = timeline.stream()
                .map(tempLoanVal -> new TemporalValue<>(tempLoanVal.getDate(), tempLoanVal.getValue().getBalance()))
                .collect(Collectors.toList());

        loanTimeline.setBalanceTimeline(balanceTimeline);
        loanTimeline.setInterestRateTimeline(interestTimeline);
        loanTimeline.setAccountId(temporalLoanStates.get(0).getValue().getAccountId().toString());

        return loanTimeline;

    }

    public LoanTimelineResponse getLoanTimelines(GetLoanTimelinesCommand command) {
        LoanTimelineResponse response = new LoanTimelineResponse();

        List<Account> accounts = accountRepository.findByUserId(command.getUserId()).stream()
                .filter(a -> !a.isExcluded() && isLoan(a))
                .collect(Collectors.toList());

        List<List<TemporalValue<Loan>>> filteredLoanStates = Lists.newArrayList();

        for (Account account : accounts) {
            List<TemporalValue<Loan>> filtered = LoanUtils.convertLoansToTemporalValues(
                    LoanUtils.filterForMonthlyPeriods(loanDAO.getLoanDataByAccountId(account.getId())));
            filteredLoanStates.add(filtered);
        }

        List<LoanTimeline> loanTimelines = Lists.newArrayList();

        for (List<TemporalValue<Loan>> filteredLoanState : filteredLoanStates) {
            loanTimelines.add(convertTemporalLoanStatesToLoanTimelines(filteredLoanState));
        }

        ListMultimap<YearMonth, ValueWeight> timelineMap = LoanUtils.convertLoanTimelinesToTimelineMap(loanTimelines);

        List<KVPair<String, Double>> weightedAverages = LoanUtils.timelineMapWeightedAverage(timelineMap);

        response.setWeightedAverageTimeline(weightedAverages);
        response.setLoanTimelines(loanTimelines);
        return response;
    }
}
