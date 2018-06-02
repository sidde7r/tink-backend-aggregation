package se.tink.backend.system.cli.debug;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferStatisticsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private CredentialsRepository credentialsRepository;
    private TransferEventRepository transferEventRepository;
    private UserRepository userRepository;
    private ProviderRepository providerRepository;
    private static final LogUtils log = new LogUtils(TransferStatisticsCommand.class);

    public TransferStatisticsCommand() {
        super("transfer-statistics", "Dump statistics for transfers");
    }

    @Override protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Gets a System property for how many days back this command should calculate statistics.
        // If not specified, the value will be null and the statistics calculated for all transfers in Tink history.

        Integer daysBack = Integer.getInteger("daysBack", 7);
        Integer errorMessagesToShow = Integer.getInteger("errorMessagesToShow", 3);
        Date tmpFromDate = null;

        if (daysBack != null) {
            if (daysBack < 0) {
                daysBack *= -1;
            }
            tmpFromDate = DateUtils.addDays(DateUtils.getToday(), -daysBack);
            tmpFromDate = DateUtils.setInclusiveStartTime(tmpFromDate);
        }

        final Date fromDate = tmpFromDate;

        log.info("Examening transfers from " + ThreadSafeDateFormat.FORMATTER_DAILY.format(tmpFromDate));

        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transferEventRepository = serviceContext.getRepository(TransferEventRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        providerRepository = serviceContext.getRepository(ProviderRepository.class);
        AggregationControllerCommonClient aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();

        // Get all transfer providers.
        List<Provider> providerList;
        if (serviceContext.isProvidersOnAggregation()) {
            providerList = aggregationControllerClient.listProviders();
        } else {
            providerList = providerRepository.findAll();
        }

        Set<Provider> providers = providerList.stream()
                .filter(provider -> provider.getCapabilities() != null &&
                        provider.getCapabilities().contains(Provider.Capability.TRANSFERS))
                .collect(Collectors.toSet());

        ListMultimap<String, Provider> providersByDisplayName = Multimaps.index(providers,
                Provider::getDisplayName);

        // Get all the FAILED and EXECUTED transfer events. The streaming from the userRepository is necessary to avoid
        // reading all transfer events into memory.

        final List<TransferEvent> allExecutedTransfers = Lists.newArrayList();
        final List<TransferEvent> allFailedTransfers = Lists.newArrayList();

        // FIXME: Better to do everything in this forEach so all TransferEvents are not in memory at the same time.
        // This would require a rebuild of this command so we'll do it when we have > 500k transfers.
        userRepository.streamAll().forEach(user -> {
            List<TransferEvent> userTransferEvents = transferEventRepository
                    .findAllByUserId(UUIDUtils.fromTinkUUID(user.getId()));

            ImmutableList<TransferEvent> executedUserTransferEvents = FluentIterable.from(userTransferEvents)
                    .filter(transferEvent -> Objects.equal(transferEvent.getStatus(), SignableOperationStatuses.EXECUTED)).filter(transferEvent -> fromDate == null || transferEvent.getCreated().after(fromDate)).toList();

            ImmutableList<TransferEvent> failedUserTransferEvents = FluentIterable.from(userTransferEvents)
                    .filter(transferEvent -> Objects.equal(transferEvent.getStatus(), SignableOperationStatuses.FAILED)).filter(transferEvent -> fromDate == null || transferEvent.getCreated().after(fromDate)).toList();

            allExecutedTransfers.addAll(executedUserTransferEvents);
            allFailedTransfers.addAll(failedUserTransferEvents);
        });

        // Some transfers are in the database with credentials that do not exist anymore. We display these as unknown.

        List<TransferEvent> executedTransfersForDeletedCredentials = findTransfersForDeletedCredentials(
                allExecutedTransfers);
        List<TransferEvent> failedTransfersForDeletedCredentials = findTransfersForDeletedCredentials(
                allFailedTransfers);

        // Output presented to the end user.

        System.out.println("<!-- START -->");

        try {

            printHeader("TINK TRANSFERS - INFORMATION", "=", 80);
            System.out.println();

            if (daysBack != null) {
                System.out.println("Statics will be calculated for " + daysBack + " days back.");
                System.out.println("All transfers after " + ThreadSafeDateFormat.FORMATTER_MINUTES.format(tmpFromDate)
                        + " will be used.");
            } else {
                System.out.println("Statics will be calculated for all transfers in Tink history.");
            }

            // Print information about all executed transfers.

            printMainHeader("ALL EXECUTED TRANSFERS");
            System.out.println("In total, " + allExecutedTransfers.size() + " transfers have been executed.");
            System.out.println();
            printSubHeader("By provider");
            for (String providerDisplayName : providersByDisplayName.keySet()) {
                List<TransferEvent> executedTransfersForProviders = findTransfersForProviders(
                        providersByDisplayName.get(providerDisplayName), allExecutedTransfers);
                System.out.println(providerDisplayName + ": " + executedTransfersForProviders.size());
            }
            System.out.println("Unknown providers: " + executedTransfersForDeletedCredentials.size());
            System.out.println("-------------------");
            System.out.println("TOTAL: " + allExecutedTransfers.size());

            // Print information about unique users who have made transfers, for all transfers and for each provider.

            printMainHeader("UNIQUE USERS WHO HAVE MADE TRANSFERS");
            System.out.println("For all providers together, " + getUniqueUsersWhoMadeTransfers(allExecutedTransfers)
                    + " unique users have made transfers.");
            System.out.println();
            printSubHeader("By provider");
            for (String providerDisplayName : providersByDisplayName.keySet()) {
                List<TransferEvent> executedTransfersForProviders = findTransfersForProviders(
                        providersByDisplayName.get(providerDisplayName), allExecutedTransfers);
                System.out.println(providerDisplayName + ": " +
                        getUniqueUsersWhoMadeTransfers(executedTransfersForProviders));
            }
            System.out.println(
                    "Unknown providers: " + getUniqueUsersWhoMadeTransfers(executedTransfersForDeletedCredentials));

            // Print information about transfer types, both for all transfers and for each provider.

            printMainHeader("TYPES OF EXECUTED TRANSFERS");
            printSubHeader("All transfers");
            printTransferTypes(allExecutedTransfers);
            System.out.println();
            printSubHeader("By provider");
            System.out.println();
            for (String providerDisplayName : providersByDisplayName.keySet()) {
                printSubSubHeader(providerDisplayName);
                List<TransferEvent> executedTransfersForProvider = findTransfersForProviders(
                        providersByDisplayName.get(providerDisplayName), allExecutedTransfers);
                printTransferTypes(executedTransfersForProvider);
                System.out.println();
            }
            printSubSubHeader("Unknown providers");
            printTransferTypes(executedTransfersForDeletedCredentials);

            // Print information about FAILED transfers (not CANCELLED) for each transfer type and each provider.

            printMainHeader("ALL FAILED TRANSFERS");
            System.out.println();
            System.out.println("The below represents all FAILED transfers (not CANCELLED).");
            System.out.println("'By type' refers to all transfers.");
            System.out.println("'By provider' shows the percentage of all transfers for that specific provider.");
            System.out.println("'Error messages' shows number of occurrence and message");
            System.out.println();

            printSubHeader("By type");
            int totalAmountOfTransfers = allExecutedTransfers.size() + allFailedTransfers.size();
            int totalAmountOfFails = allFailedTransfers.size();
            printTotalTransfersAndTotalFails(totalAmountOfTransfers, totalAmountOfFails);
            System.out.println();
            printTransferTypes(allFailedTransfers);

            System.out.println();
            printSubHeader("By provider");
            System.out.println();
            for (String providerDisplayName : providersByDisplayName.keySet()) {
                printSubSubHeader(providerDisplayName);
                List<TransferEvent> executedTransfersForProvider = findTransfersForProviders(
                        providersByDisplayName.get(providerDisplayName), allExecutedTransfers);
                List<TransferEvent> failedTransfersForProvider = findTransfersForProviders(
                        providersByDisplayName.get(providerDisplayName), allFailedTransfers);

                System.out.println("Total: " + percent(failedTransfersForProvider.size(),
                        executedTransfersForProvider.size() + failedTransfersForProvider.size()));
                printTransferTypes(failedTransfersForProvider, executedTransfersForProvider);
                System.out.println();
            }
            printSubSubHeader("Unknown providers");
            System.out.println("Total: " + percent(failedTransfersForDeletedCredentials.size(),
                    failedTransfersForDeletedCredentials.size() + executedTransfersForDeletedCredentials.size()));
            printTransferTypes(failedTransfersForDeletedCredentials);

            System.out.println();
            printSubHeader("Error messages");
            Map<String, Integer> sortedFailedtransfersByErrorMessages = getSortedErrorMessages(allFailedTransfers,
                    errorMessagesToShow);
            printErrorMessages(sortedFailedtransfersByErrorMessages);

            // Print information about the amount of money transferred.

            printMainHeader("MONEY TRANSFERRED");
            printSubSubHeader("Total amount");
            printAmountOfMoneyForTransfers(allExecutedTransfers);
            System.out.println();

        } finally {

            // Without this finally clause, the Salt runner will not be able to match output correctly.

            System.out.println("<!-- END -->");
        }
    }

    private void printHeader(String name, String pattern, int length) {
        String delimiter = "";
        for (int i = 0; i < length; i++) {
            delimiter += pattern;
        }
        System.out.println(delimiter);
        String space = "";
        for (int i = 0; i < (length - name.length()) / 2; i++) {
            space += " ";
        }
        System.out.print(space + name + space);
        System.out.println();
        System.out.println(delimiter);
    }

    private void printSubHeader(String name) {
        printHeader(name, "-", 40);
    }

    private void printMainHeader(String name) {
        System.out.println();
        printHeader(name, "~", 80);
        System.out.println();
    }

    private void printSubSubHeader(String name) {
        System.out.println("[" + name + "]");
    }

    private int getUniqueUsersWhoMadeTransfers(List<TransferEvent> executedTransfers) {
        Set<UUID> userIds = Sets.newHashSet();

        for (TransferEvent transfer : executedTransfers) {
            UUID userId = transfer.getUserId();
            if (userId != null && !userIds.contains(transfer.getUserId())) {
                userIds.add(transfer.getUserId());
            }
        }

        return userIds.size();
    }

    private void printTotalTransfersAndTotalFails(int totalAmountOfTransfers, int totalAmountOfFails) {
        String procentOfFails = percent(totalAmountOfFails, totalAmountOfTransfers);
        System.out.println("Of total " + totalAmountOfTransfers + " transfers, " + totalAmountOfFails + " failed ("
                + procentOfFails + ")");
    }

    private void printErrorMessages(Map<String, Integer> sortedFailedtransfersByErrorMessages) {
        for (String key : sortedFailedtransfersByErrorMessages.keySet()) {
            int value = sortedFailedtransfersByErrorMessages.get(key);
            System.out.println(value + ": " + key);
        }
    }

    private Map<String, Integer> getSortedErrorMessages(List<TransferEvent> allFailedTransfers,
            Integer errorMessagesToShow) {
        /* Creating a Map with error message and how often message occurred */
        Map<String, Integer> failedTransfers = getErrorMessagesByNumberFromFailedTransfers(allFailedTransfers);

        /* Creating a linked list to be able to sort the map failedTransfers */
        List<Map.Entry<String, Integer>> failedTransfersLinkedList = Lists.newArrayList(failedTransfers.entrySet());

        Collections.sort(failedTransfersLinkedList, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        /* Only saving the error massages to show in new Map from int: errorMessagesToShow */
        Map<String, Integer> sortedErrorMessages = Maps.newLinkedHashMap();
        int count = 0;
        for (Map.Entry<String, Integer> entry : failedTransfersLinkedList) {

            if (count < errorMessagesToShow) {
                sortedErrorMessages.put(entry.getKey(), entry.getValue());
                count++;
            } else {
                break;
            }
        }

        return sortedErrorMessages;
    }

    /**
     * Find how many times an error has occurred in the failed transfers.
     *
     * @param allFailedTransfers
     * @return
     */
    private Map<String, Integer> getErrorMessagesByNumberFromFailedTransfers(List<TransferEvent> allFailedTransfers) {
        Map<String, Integer> errorMessagesByNumberOfFailedTransfers = Maps.newHashMap();

        for (TransferEvent transfer : allFailedTransfers) {

            String message = transfer.getStatusMessage();
            if (!Strings.isNullOrEmpty(message)) {

                Integer nrOfErrorMessages = 0;
                if (errorMessagesByNumberOfFailedTransfers.containsKey(message)) {
                    nrOfErrorMessages = errorMessagesByNumberOfFailedTransfers.get(message);
                }
                nrOfErrorMessages++;
                errorMessagesByNumberOfFailedTransfers.put(message, nrOfErrorMessages);
            }
        }

        return errorMessagesByNumberOfFailedTransfers;
    }

    private void printTransferTypes(List<TransferEvent> transfers) {

        int nrOfBankTransfers = 0;
        int nrOfPayments = 0;
        int nrOfEinvoices = 0;
        int nrOfNullTypes = 0; // The TransferEvent repository has null types before the beginning of July 2016.

        for (TransferEvent transfer : transfers) {

            if (Objects.equal(transfer.getTransferType(), TransferType.BANK_TRANSFER)) {
                nrOfBankTransfers++;
            } else if (Objects.equal(transfer.getTransferType(), TransferType.PAYMENT)) {
                nrOfPayments++;
            } else if (Objects.equal(transfer.getTransferType(), TransferType.EINVOICE)) {
                nrOfEinvoices++;
            } else if (transfer.getTransferType() == null) {
                nrOfNullTypes++;
            }
        }

        int totalAmount = nrOfBankTransfers + nrOfPayments + nrOfEinvoices + nrOfNullTypes;

        printOutTransferTypes(totalAmount, nrOfBankTransfers, nrOfPayments, nrOfEinvoices, nrOfNullTypes);
    }

    private void printTransferTypes(List<TransferEvent> transfersToCalculateStatisticsFor,
            List<TransferEvent> executedTransfersForProvider) {

        int nrOfBankTransfers = 0;
        int nrOfPayments = 0;
        int nrOfEinvoices = 0;
        int nrOfNullTypes = 0; // The TransferEvent repository has null types before the beginning of July 2016.

        for (TransferEvent transfer : transfersToCalculateStatisticsFor) {

            if (Objects.equal(transfer.getTransferType(), TransferType.BANK_TRANSFER)) {
                nrOfBankTransfers++;
            } else if (Objects.equal(transfer.getTransferType(), TransferType.PAYMENT)) {
                nrOfPayments++;
            } else if (Objects.equal(transfer.getTransferType(), TransferType.EINVOICE)) {
                nrOfEinvoices++;
            } else if (transfer.getTransferType() == null) {
                nrOfNullTypes++;
            }
        }

        int totalAmount = transfersToCalculateStatisticsFor.size() + executedTransfersForProvider.size();

        printOutTransferTypes(totalAmount, nrOfBankTransfers, nrOfPayments, nrOfEinvoices, nrOfNullTypes);
    }

    private void printOutTransferTypes(int totalAmount, int nrOfBankTransfers, int nrOfPayments, int nrOfEinvoices,
            int nrOfNullTypes) {
        System.out.println("Bank transfers: " + percent(nrOfBankTransfers, totalAmount));
        System.out.println("Payments: " + percent(nrOfPayments, totalAmount));
        System.out.println("E-invoices: " + percent(nrOfEinvoices, totalAmount));
        System.out.println("Unknown types: " + percent(nrOfNullTypes, totalAmount));
    }

    // FIXME: Better to build a multimap out of this and not run this method many times.
    private List<TransferEvent> findTransfersForProviders(List<Provider> providers,
            List<TransferEvent> transferEvents) {

        List<TransferEvent> transfersForProviders = Lists.newArrayList();

        for (final Provider provider : providers) {
            ImmutableList<TransferEvent> transfersForProvider = FluentIterable.from(transferEvents)
                    .filter(transfer -> {
                        UUID credentialsId = transfer.getCredentialsId();
                        Credentials credentials = credentialsRepository
                                .findOne(UUIDUtils.toTinkUUID(credentialsId));
                        return credentials != null && Objects
                                .equal(credentials.getProviderName(), provider.getName());
                    }).toList();
            transfersForProviders.addAll(transfersForProvider);
        }

        return transfersForProviders;
    }

    private List<TransferEvent> findTransfersForDeletedCredentials(List<TransferEvent> transferEvents) {
        return FluentIterable.from(transferEvents)
                .filter(transfer -> {
                    UUID credentialsId = transfer.getCredentialsId();
                    Credentials credentials = credentialsRepository
                            .findOne(UUIDUtils.toTinkUUID(credentialsId));
                    return credentials == null; // If it's null, the credential has been deleted.
                }).toList();
    }

    private List<TransferEvent> getTransferEventsBetweenDates(List<TransferEvent> transfers, final Date firstDate,
            final Date secondDate) {
        return FluentIterable.from(transfers)
                .filter(transfer -> transfer.getUpdated().after(firstDate) && transfer.getUpdated().before(secondDate)).toList();
    }

    private void printAmountOfMoneyForTransfers(List<TransferEvent> transfers) {
        Map<String, Double> amountByCurrency = Maps.newHashMap();

        for (TransferEvent transfer : transfers) {
            String currency = transfer.getCurrency();
            if (amountByCurrency.containsKey(currency)) {
                amountByCurrency.put(currency, amountByCurrency.get(currency) + transfer.getAmount());
            } else {
                amountByCurrency.put(currency, transfer.getAmount());
            }
        }

        for (String currency : amountByCurrency.keySet()) {
            System.out.println(amountByCurrency.get(currency) + " " + currency);
        }
    }

    private String percent(int first, int second) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        if (second != 0) {
            return decimalFormat.format((float) first / second * 100) + "%";
        } else {
            return "0%";
        }
    }

}
