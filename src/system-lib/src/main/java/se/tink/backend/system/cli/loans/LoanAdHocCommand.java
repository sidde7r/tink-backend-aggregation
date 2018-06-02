package se.tink.backend.system.cli.loans;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.inf.Namespace;
import rx.Observable;
import rx.observables.ConnectableObservable;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.loans.SwedishLoanNameInterpreter;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Loan;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class LoanAdHocCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(LoanAdHocCommand.class);
    private ImmutableSet<String> commands = ImmutableSet.of("seed-providers-and-type",
            "correct-lf-num-months", "remove-shb-zeroes");

    static final Pattern PATTERN_RATE_BINDING =
            Pattern.compile("\"rateBindingPeriodLength\":\"(\\d+) MÅNADER\"");
    static final Pattern PATTERN_VARIABLE_RATE =
            Pattern.compile("\"rateBindingPeriodLength\":(null),.*\"fixedRate\":(false)");
    private LoanDataRepository loanDataRepository;
    private CredentialsRepository credentialsRepository;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    public LoanAdHocCommand() {
        super("loan-ad-hoc", "Multiple ad-hoc commands collected");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        String command = System.getProperty("command", null);
        if (command == null || !commands.contains(command)) {
            log.info("\"command\" not set. Possible commands: [ " + Joiner.on(", ").join(commands) + " ]");
            return;
        }

        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);

        if (command.equals("seed-providers-and-type")) {
            seedProvidersAndType();
        } else if (command.equals("correct-lf-num-months")) {
            correctLfNumMonthsBound();
        } else if (command.equals("remove-shb-zeroes")) {
            removeShbZeroes();
        }
    }

    private Observable<Account> streamAccounts() {
        return userRepository.streamAll().flatMapIterable(user -> accountRepository.findByUserId(user.getId()));
    }

    private void removeShbZeroes() {
        final Map<String, String> providerByCredentials = createProviderByCredentials();

        streamAccounts().filter(account -> {

            String providerName = providerByCredentials.get(account.getCredentialsId());
            if ("handelsbanken".equals(providerName) || "handelsbanken-bankid".equals(providerName)) {
                return account.getType() == AccountTypes.LOAN || account.getType() == AccountTypes.MORTGAGE;
            }

            return false;
        }).forEach(account -> {
            List<Loan> loans = loanDataRepository.findAllByAccountId(account.getId());
            if (loans == null || loans.size() == 0) {
                return;
            }

            removeZeroes(account.getId(), loans);
        });
    }

    private void removeZeroes(String accountId, List<Loan> loans) {
        List<Loan> pointsToRemove = Lists.newArrayList();
        for(Loan loan : loans) {
            if (loan.getInterest() == 0) {
                pointsToRemove.add(loan);
            }
        }

        if (pointsToRemove.size() != loans.size()) {
            for(Loan remove : pointsToRemove) {
                loanDataRepository.delete(remove);
            }
            log.info("Removed " + pointsToRemove.size() + " points (of " + loans.size() + ") from accountid: "
                    + accountId);
        } else {
            log.info("Did not remove any loans. All data points had 0 interest. Accountid: " + accountId);
        }
    }

    private void correctLfNumMonthsBound() {

        final Map<String, String> providerByCredentials = createProviderByCredentials();

        streamAccounts().filter(account -> {

            String providerName = providerByCredentials.get(account.getCredentialsId());
            if ("lansforsakringar".equals(providerName) || "lansforsakringar-bankid".equals(providerName)) {
                return account.getType() == AccountTypes.LOAN;
            }

            return false;
        }).forEach(account -> {
            List<Loan> loans = loanDataRepository.findAllByAccountId(account.getId());
            if (loans == null || loans.size() == 0) {
                return;
            }

            correctLoans(loans);
        });
    }

    private void correctLoans(List<Loan> loans) {

        for (Loan loan : loans) {
            try {
                String response = loan.getSerializedLoanResponse();
                if (response != null) {
                    Matcher mRateBindingLength = PATTERN_RATE_BINDING.matcher(response);
                    Matcher mVariableRate = PATTERN_VARIABLE_RATE.matcher(response);
                    if (mRateBindingLength.find()) {
                        String numbers = mRateBindingLength.group(1);
                        int months = Integer.parseInt(numbers);
                        if (loan.getNumMonthsBound() == null || loan.getNumMonthsBound().intValue() != months) {

                            log.info("Swapping out " + loan.getNumMonthsBound() + " for new correct value: " +
                                    months + " which was inserted: " +
                                    ThreadSafeDateFormat.FORMATTER_DAILY.format(UUIDUtils.UUIDToDate(loan.getId())));

                            loan.setNumMonthsBound(months);
                            loanDataRepository.save(loan);
                        }
                    } else if (mVariableRate.find()) {
                        if ("false".equals(mVariableRate.group(2)) && loan.getNumMonthsBound() == null) {
                            log.info("Setting numMonthsBound to 1 for loan with accountid: " + loan.getAccountId());
                            loan.setNumMonthsBound(1);
                            loanDataRepository.save(loan);
                        } else {
                            log.info("Wasn't false or numMonths was already set to something else. Check accountid: " +
                                    loan.getAccountId());
                        }
                    } else {
                        log.info("Didn't match any of the two patterns: " + response);
                    }
                }
            } catch (Exception e) {
                log.info("Something happend when doing: aid: " + loan.getAccountId() + " id: " + loan.getId());
                e.printStackTrace();
            }
        }
    }

    private void seedProvidersAndType() {
        Map<String, AccountDetails> providerNameByAccount = accountDetailsByAccount(createProviderByCredentials());

        for (String accountId : providerNameByAccount.keySet()) {
            AccountDetails details = providerNameByAccount.get(accountId);
            if (details.providerName == null || details.providerName.length() == 0) {
                continue;
            }

            List<Loan> loans = loanDataRepository.findAllByAccountId(accountId);

            if (loans != null && loans.size() > 0) {
                for (Loan loan : loans) {
                    if (loan.getProviderName() == null) {
                        loan.setProviderName(details.providerName);
                    }

                    SwedishLoanNameInterpreter interpreter = new SwedishLoanNameInterpreter(loan.getName());
                    if (loan.getType() == null) {
                        loan.setType(interpreter.getGuessedLoanType());
                    }
                    if (loan.getNumMonthsBound() == null) {
                        loan.setNumMonthsBound(interpreter.getGuessedNumMonthsBound());
                    }

                    try {
                        loanDataRepository.save(loan);
                    } catch(Exception e) {
                        log.error(UUIDUtils.toTinkUUID(loan.getUserId()),
                                UUIDUtils.toTinkUUID(loan.getCredentialsId()), "Wasn't able to save the loan.", e);
                    }
                }
            }
        }
    }

    private Map<String, AccountDetails> accountDetailsByAccount(final Map<String, String> providerByCredentials) {

        final Map<String, AccountDetails> providerByAccount = Maps.newHashMap();
        ConnectableObservable<Account> observable = streamAccounts().publish();

        observable.filter(account -> {
            if (account == null) {
                return false;
            }
            if (account.getType() == AccountTypes.LOAN) {
                return true;
            }
            if (account.getType() == AccountTypes.MORTGAGE) {
                return true;
            }

            return false;
        }).forEach(a -> {
            if (a.getCredentialsId() != null && providerByCredentials.containsKey(a.getCredentialsId())) {
                String provider = providerByCredentials.get(a.getCredentialsId());
                AccountDetails details = new AccountDetails();
                details.providerName = provider;
                providerByAccount.put(a.getId(), details);
            }
        });

        observable.connect();
        return providerByAccount;
    }

    private Map<String, String> createProviderByCredentials() {
        return credentialsRepository.findAllIdsAndProviderNames();
    }

    private static class AccountDetails {
        public String providerName;
    }
}
