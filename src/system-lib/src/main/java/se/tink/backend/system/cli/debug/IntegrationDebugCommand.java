package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.LoanDetailsRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.libraries.uuid.UUIDUtils;

public class IntegrationDebugCommand extends ServiceContextCommand<ServiceConfiguration> {

    public IntegrationDebugCommand() {
        super("integration-debug-command", "description");
    }

    private void printAccounts(AccountRepository accountsRepository, String userId, String credentialsId) {
        List<Account> accounts = accountsRepository.findByUserIdAndCredentialsId(userId, credentialsId);

        List<Map<String, String>> output = Lists.newArrayList();

        accounts.forEach(account -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", account.getId());
            data.put("credentialsid", account.getCredentialsId());
            data.put("accountnumber", account.getAccountNumber());
            data.put("bankid", account.getBankId());
            data.put("identifiers", account.getIdentifiers().toString());
            data.put("name", account.getName());
            data.put("type", account.getType().toString());
            data.put("excluded", Boolean.toString(account.isExcluded()));
            data.put("closed", Boolean.toString(account.isClosed()));
            data.put("favored", Boolean.toString(account.isFavored()));
            data.put("ownership", Double.toString(account.getOwnership()));
            data.put("payload", String.valueOf(account.getPayload()));
            data.put("certaindate", String.valueOf(account.getCertainDate()));
            data.put("usermodifiedexcluded", String.valueOf(account.isUserModifiedExcluded()));
            data.put("usermodifiedname", String.valueOf(account.isUserModifiedName()));
            data.put("usermodifiedtype", String.valueOf(account.isUserModifiedType()));
            output.add(data);
        });

        CliPrintUtils.printTable(output);
    }

    private void printLoans(AccountRepository accountRepository, LoanDataRepository loanDataRepository, String userId,
            String credentialsId) {
        List<Account> loanAccounts = accountRepository.findByUserIdAndCredentialsId(userId, credentialsId).stream()
                .filter(account -> Objects.equals(account.getType(), AccountTypes.LOAN))
                .collect(Collectors.toList());

        List<Map<String, String>> output = Lists.newArrayList();

        loanAccounts.forEach(account -> {
            List<Loan> threeMostRecentLoans = loanDataRepository.findMostRecentByAccountId(UUIDUtils.fromTinkUUID(account.getId()), 3);

            threeMostRecentLoans.forEach(loan -> {
                Map<String, String> data = Maps.newLinkedHashMap();
                data.put("id", String.valueOf(UUIDUtils.toTinkUUID(loan.getId())));
                data.put("accountid", String.valueOf(UUIDUtils.toTinkUUID(loan.getAccountId())));
                data.put("credentialsid", String.valueOf(UUIDUtils.toTinkUUID(loan.getCredentialsId())));
                data.put("name", loan.getName());
                data.put("loannumber", loan.getLoanNumber());
                data.put("nummonthsbound", String.valueOf(loan.getNumMonthsBound()));
                data.put("interest", String.valueOf(loan.getInterest()));
                data.put("type", asName(loan.getType()));
                data.put("updated", String.valueOf(loan.getUpdated()));
                output.add(data);
            });
        });

        CliPrintUtils.printTable(output);
    }

    private void printLoanDetails(AccountRepository accountRepository, LoanDetailsRepository loanDetailsRepository,
            String userId, String credentialsId) {
        List<Account> loanAccounts = accountRepository.findByUserIdAndCredentialsId(userId, credentialsId).stream()
                .filter(account -> Objects.equals(account.getType(), AccountTypes.LOAN))
                .collect(Collectors.toList());

        List<Map<String, String>> output = Lists.newArrayList();

        loanAccounts.stream()
                .map(account -> loanDetailsRepository
                        .findOneByAccountId(UUIDUtils.fromTinkUUID(account.getId())))
                .filter(Objects::nonNull)
                .forEach(loanDetails -> {
                    Map<String, String> data = Maps.newLinkedHashMap();
                    data.put("accountid", String.valueOf(UUIDUtils.toTinkUUID(loanDetails.getAccountId())));
                    data.put("applicants", String.valueOf(loanDetails.getApplicants()));
                    data.put("coapplicant", String.valueOf(loanDetails.getCoApplicant()));
                    data.put("loansecurity", String.valueOf(loanDetails.getLoanSecurity()));
                    output.add(data);
                });

        CliPrintUtils.printTable(output);
    }

    private void printLastCredentialsEvents(CredentialsEventRepository credentialsEventRepository,
            Credentials credential) {
        List<CredentialsEvent> credentialsEvents = credentialsEventRepository
                .findMostRecentByUserIdAndCredentialsId(credential.getUserId(), credential.getId(), 20);

        List<Map<String, String>> output = Lists.newArrayList();

        credentialsEvents.forEach(credentialsEvent -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("credentialsid", String.valueOf(UUIDUtils.toTinkUUID(credentialsEvent.getCredentialsId())));
            data.put("providername", credentialsEvent.getProviderName());
            data.put("status", String.valueOf(credentialsEvent.getStatus()));
            data.put("timestamps", String.valueOf(credentialsEvent.getTimestamp()));
            data.put("message", credentialsEvent.getMessage());
            output.add(data);
        });

        CliPrintUtils.printTable(output);
    }

    private void printCredentials(Credentials credential) {
        List<Map<String, String>> output = Lists.newArrayList();

        output.add(CliPrintUtils.keyValueEntry("id", credential.getId()));
        output.add(CliPrintUtils.keyValueEntry("providername", credential.getProviderName()));
        output.add(CliPrintUtils.keyValueEntry("status", String.valueOf(credential.getStatus())));
        output.add(CliPrintUtils.keyValueEntry("type", String.valueOf(credential.getType())));
        output.add(CliPrintUtils.keyValueEntry("fields", credential.getFieldsSerialized()));
        output.add(CliPrintUtils.keyValueEntry("updated", String.valueOf(credential.getUpdated())));
        output.add(CliPrintUtils.keyValueEntry("statusUpdated", String.valueOf(credential.getStatusUpdated())));
        output.add(CliPrintUtils.keyValueEntry("statusPayload", credential.getStatusPayload()));
        output.add(CliPrintUtils.keyValueEntry("debugFlag", String.valueOf(credential.isDebug())));

        CliPrintUtils.printTable(output);
    }

    private void printUserInfo(User user) {
        List<Map<String, String>> output = Lists.newArrayList();

        output.add(CliPrintUtils.keyValueEntry("id", String.format("%s (%s)", user.getId(),
                UUIDUtils.fromTinkUUID(user.getId()).toString())));
        output.add(CliPrintUtils.keyValueEntry("username", user.getUsername()));
        output.add(CliPrintUtils.keyValueEntry("profile_market", user.getProfile().getMarket()));
        output.add(CliPrintUtils.keyValueEntry("profile_locale", user.getProfile().getLocale()));
        output.add(CliPrintUtils.keyValueEntry("created", String.valueOf(user.getCreated())));
        output.add(CliPrintUtils.keyValueEntry("profile_periodadjustedday",
                Integer.toString(user.getProfile().getPeriodAdjustedDay())));
        output.add(CliPrintUtils.keyValueEntry("blocked", Boolean.toString(user.isBlocked())));
        output.add(CliPrintUtils.keyValueEntry("flags", String.valueOf(user.getFlags())));
        output.add(CliPrintUtils.keyValueEntry("debugFlag", String.valueOf(user.isDebug())));

        CliPrintUtils.printTable(output);
    }

    private void printInvestments(AccountRepository accountRepository, PortfolioRepository portfolioRepository,
            InstrumentRepository instrumentRepository, String userId, String credentialsId) {
        accountRepository.findByUserIdAndCredentialsId(userId, credentialsId).stream()
                .map(account -> portfolioRepository.findAllByUserIdAndAccountId(
                        UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(account.getId())))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(portfolio -> {
                    System.out.println();
                    System.out.println("Portfolio");
                    printPortfolio(portfolio);
                    System.out.println();
                    System.out.println("Instruments");
                    printInstruments(instrumentRepository, userId, portfolio.getId());
                });
    }

    private void printPortfolio(Portfolio portfolio) {
        List<Map<String, String>> output = Lists.newArrayList();
        Map<String, String> data = Maps.newLinkedHashMap();

        data.put("id", UUIDUtils.toTinkUUID(portfolio.getId()));
        data.put("accountid", UUIDUtils.toTinkUUID(portfolio.getAccountId()));
        data.put("uniqueidentifier", portfolio.getUniqueIdentifier());
        data.put("rawtype", portfolio.getRawType());
        data.put("type", asName(portfolio.getType()));

        output.add(data);

        CliPrintUtils.printTable(output);
    }

    private void printInstruments(InstrumentRepository instrumentRepository, String userId, UUID portfolioID) {
        List<Map<String, String>> output = Optional.ofNullable(
                instrumentRepository.findAllByUserIdAndPortfolioId(UUIDUtils.fromTinkUUID(userId), portfolioID))
                .map(Collection::stream)
                .map(instruments -> instruments.map(instrument -> {
                            Map<String, String> data = Maps.newLinkedHashMap();

                            data.put("id", UUIDUtils.toTinkUUID(instrument.getId()));
                            data.put("portfolioid", UUIDUtils.toTinkUUID(instrument.getPortfolioId()));
                            data.put("uniqueidentifier", instrument.getUniqueIdentifier());
                            data.put("isin", instrument.getIsin());
                            data.put("marketplace", instrument.getMarketPlace());
                            data.put("name", instrument.getName());
                            data.put("currency", instrument.getCurrency());
                            data.put("rawtype", instrument.getRawType());
                            data.put("type", asName(instrument.getType()));

                            return data;
                        }).collect(Collectors.toList())
                ).orElse(ImmutableList.of());
        CliPrintUtils.printTable(output);
    }

    private void printTransferDestinationPatterns(AccountRepository accountRepository, String userId,
            String credentialsId, TransferDestinationPatternRepository transferDestinationPatternRepository) {
        List<Account> accounts = accountRepository.findByCredentialsId(credentialsId);
        List<Map<String, String>> output = Lists.newArrayList();

        accounts.stream()
                .map(account -> transferDestinationPatternRepository
                        .findAllByUserIdAndAccountId(userId, account.getId()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(pattern -> {
                    Map<String, String> data = Maps.newLinkedHashMap();

                    data.put("accountid", UUIDUtils.toTinkUUID(pattern.getAccountId()));
                    data.put("type", asName(pattern.getType()));
                    data.put("name", pattern.getName());
                    data.put("bank", pattern.getBank());
                    data.put("pattern", pattern.getPattern());
                    data.put("matchesmultiple", String.valueOf(pattern.isMatchesMultiple()));

                    output.add(data);
                });

        CliPrintUtils.printTable(output);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input validation
        final String username = System.getProperty("username");
        final String userId = System.getProperty("userId");
        final String credentialsId = System.getProperty("credentialsId");

        System.out.println("Username to search for is: " + username);
        System.out.println("UserId to search for is: " + userId);
        System.out.println("CredentialsId to search for is: " + credentialsId);

        Preconditions.checkArgument(
                Strings.nullToEmpty(username).trim().length() > 0 || Strings.nullToEmpty(userId).trim().length() > 0);
        Preconditions.checkArgument(Strings.nullToEmpty(credentialsId).trim().length() > 0);

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        AccountRepository accountsRepository = serviceContext.getRepository(AccountRepository.class);
        CredentialsEventRepository credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        LoanDataRepository loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        LoanDetailsRepository loanDetailsRepository = serviceContext.getRepository(LoanDetailsRepository.class);
        PortfolioRepository portfolioRepository = serviceContext.getRepository(PortfolioRepository.class);
        InstrumentRepository instrumentRepository = serviceContext.getRepository(InstrumentRepository.class);
        TransferDestinationPatternRepository transferDestinationPatternRepository = serviceContext
                .getRepository(TransferDestinationPatternRepository.class);

        User user = Strings.isNullOrEmpty(userId) ? userRepository.findOneByUsername(username) : userRepository.findOne(
                userId);

        Credentials credential = credentialsRepository.findOne(credentialsId);

        // Output presented to the end user.

        System.out.println("<!-- START -->");

        try {

            if (user == null || credential == null) {

                System.out.println("Could not find user or credential.");

            } else {

                System.out.println("User");
                printUserInfo(user);

                System.out.println();
                System.out.println("Credential");
                printCredentials(credential);

                System.out.println();
                System.out.println("Credential events");
                printLastCredentialsEvents(credentialsEventRepository, credential);

                System.out.println();
                System.out.println("Accounts");
                printAccounts(accountsRepository, user.getId(), credential.getId());

                System.out.println();
                System.out.println("Loans");
                printLoans(accountsRepository, loanDataRepository, user.getId(), credential.getId());

                System.out.println();
                System.out.println("Loan details");
                printLoanDetails(accountsRepository, loanDetailsRepository, user.getId(), credential.getId());

                printInvestments(accountsRepository, portfolioRepository, instrumentRepository, userId, credentialsId);

                System.out.println();
                System.out.println("Transfer destination patterns");
                printTransferDestinationPatterns(accountsRepository, user.getId(), credential.getId(),
                        transferDestinationPatternRepository);
            }

        } finally {

            // Without this finally clause, the Salt runner will not be able to match output correctly.

            System.out.println("<!-- END   -->");

        }

        // Data parsed by Salt to be able to make log searches.

        System.out.println("SALT(credentialsid) = " + credential.getId());

    }

    private static String asName(Enum<?> value) {
        return value != null ? value.name() : null;
    }
}
