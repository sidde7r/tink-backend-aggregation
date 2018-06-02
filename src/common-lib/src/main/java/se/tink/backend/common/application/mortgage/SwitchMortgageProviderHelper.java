package se.tink.backend.common.application.mortgage;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwitchMortgageProviderHelper {

    private static final LogUtils log = new LogUtils(SwitchMortgageProviderHelper.class);

    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final LoanDataRepository loanDataRepository;
    private final AggregationControllerCommonClient aggregationControllerClient;
    private final boolean isProvidersOnAggregation;

    public SwitchMortgageProviderHelper(RepositoryFactory repositoryFactory,
            AggregationControllerCommonClient aggregationControllerClient, boolean isProvidersOnAggregation) {
        this.accountRepository = repositoryFactory.getRepository(AccountRepository.class);
        this.credentialsRepository = repositoryFactory.getRepository(CredentialsRepository.class);
        this.providerRepository = repositoryFactory.getRepository(ProviderRepository.class);
        this.loanDataRepository = repositoryFactory.getRepository(LoanDataRepository.class);
        this.aggregationControllerClient = aggregationControllerClient;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
    }

    public Optional<CurrentMortgage> getCurrentMortgage(Application application, User user) {

        Optional<ApplicationForm> form = ApplicationUtils.getFirst(application, ApplicationFormName.CURRENT_MORTGAGES);
        if (!form.isPresent()) {
            return Optional.empty();
        }

        return getCurrentMortgage(form.get(), user);
    }

    public Optional<CurrentMortgage> getCurrentMortgage(ApplicationForm form, User user) {

        Optional<String> mortgages = form.getFieldValue(ApplicationFieldName.CURRENT_MORTGAGE);
        if (!mortgages.isPresent() || Strings.isNullOrEmpty(mortgages.get())) {
            return Optional.empty();
        }

        CurrentMortgage currentMortgage = new CurrentMortgage();

        List<CurrentMortgage.CurrentMortgagePart> loanParts = Lists.newArrayList();
        currentMortgage.setLoanParts(loanParts);

        Map<String, Account> accountById = Maps.uniqueIndex(accountRepository.findByUserId(user.getId()),
                Account::getId);
        List<String> accountIds = SerializationUtils.deserializeFromString(mortgages.get(),
                TypeReferences.LIST_OF_STRINGS);

        double loanAmount = 0;
        double loanAmountForInterestRateCalculations = 0;
        double interestRate = 0;

        for (String accountId : accountIds) {

            Account account = accountById.get(accountId);

            if (account == null) {
                // This should never happen, since the mortgages are selected (and verified) from a list based
                // on the user's accounts.
                log.error(user.getId(), String.format("The mortgage account doesn't exist [accountId:%s].", accountId));
                continue;
            }

            double loanPartAmount = Math.abs(account.getBalance());

            CurrentMortgage.CurrentMortgagePart part = currentMortgage.new CurrentMortgagePart();
            part.setAmount(loanPartAmount);
            part.setId(account.getAccountNumber());
            loanParts.add(part);

            Loan firstLoanEntry = loanDataRepository.findLeastRecentOneByAccountId(account.getId());
            if (firstLoanEntry != null) {
                part.setFirstSeen(firstLoanEntry.getUpdated());
                part.setInitialDate(firstLoanEntry.getInitialDate());
            }

            loanAmount += loanPartAmount;

            Loan mostRecentLoanEntry = loanDataRepository.findMostRecentOneByAccountId(account.getId());

            if (mostRecentLoanEntry != null) {

                Double loanPartInterestRate = mostRecentLoanEntry.getInterest();

                if (loanPartInterestRate != null) {
                    double weighted = loanAmountForInterestRateCalculations * interestRate;
                    loanAmountForInterestRateCalculations += loanPartAmount;
                    if (loanAmountForInterestRateCalculations > 0) {
                        part.setInterestRate(loanPartInterestRate);
                        interestRate = (weighted + (loanPartInterestRate * loanPartAmount))
                                / loanAmountForInterestRateCalculations;
                    }
                }
            }

            if (Strings.isNullOrEmpty(currentMortgage.getProviderDisplayName())) {
                Credentials credentials = credentialsRepository.findOne(account.getCredentialsId());

                if (credentials == null) {
                    // This should never happen; if the account exists, its credentials should too.
                    log.error(user.getId(), account.getCredentialsId(),
                            "The credentials for the mortgage account doesn't exist.");
                    continue;
                }

                Provider provider;
                if (isProvidersOnAggregation) {
                    provider = aggregationControllerClient.getProviderByName(credentials.getProviderName());
                } else {
                    provider = providerRepository.findByName(credentials.getProviderName());
                }

                if (provider == null) {
                    // This should never happen; if the credentials exist, its provider should too.
                    log.error(user.getId(), account.getCredentialsId(), String.format(
                            "The provider ('%s') for the mortgage account doesn't exist.",
                            credentials.getProviderName()));
                    continue;
                }

                currentMortgage.setProviderName(provider.getName());
                currentMortgage.setProviderDisplayName(provider.getDisplayName());
            }
        }

        currentMortgage.setAmount(loanAmount);
        currentMortgage.setInterestRate(interestRate);

        return Optional.of(currentMortgage);
    }

    public OptionalDouble getCsnDebt(User user) {
        List<Account> relevantAccounts = Lists.newArrayList();

        List<Credentials> credentials = credentialsRepository.findAllByUserIdAndProviderName(user.getId(), "csn");
        for (Credentials credential : credentials) {
            if (Objects.equals(credential.getField(Field.Key.USERNAME), user.getNationalId())) {
                relevantAccounts.addAll(accountRepository.findByCredentialsId(credential.getId()).stream()
                        .filter(a -> !a.isClosed())
                        .filter(a -> !a.isExcluded())
                        .collect(Collectors.toList()));
            }
        }

        if (relevantAccounts.isEmpty()) {
            return OptionalDouble.empty();
        } else {
            Double totalBalance = 0.0;
            for (Account account : relevantAccounts) {
                totalBalance += account.getBalance();
            }

            return OptionalDouble.of(Math.abs(totalBalance));
        }
    }

}
