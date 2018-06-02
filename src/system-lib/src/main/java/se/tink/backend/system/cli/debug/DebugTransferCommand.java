package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class DebugTransferCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final Ordering<SignableOperation> ORDERING_AFTER_UNDERLYINGID = new Ordering<SignableOperation>() {
        @Override
        public int compare(SignableOperation s1, SignableOperation s2) {
            int res = s1.getUnderlyingId().toString().compareTo(s2.getUnderlyingId().toString());
            if (res == 0) {
                res = s1.getUpdated().compareTo(s2.getUpdated());
            }
            return res;
        }
    };

    private static final Ordering<Transfer> ORDERING_AFTER_ID = new Ordering<Transfer>() {
        @Override
        public int compare(Transfer t1, Transfer t2) {
            return t1.getId().toString().compareTo(t2.getId().toString());
        }
    };

    private static Map<String, String> keyValueEntry(String key, String value) {
        Map<String, String> entry = Maps.newLinkedHashMap();
        entry.put("Property", key);
        entry.put("Value", value);
        return entry;
    }

    private static void printTable(List<Map<String, String>> rows) {

        if (rows.isEmpty()) {
            System.out.println("<no data>");
            return;
        }

        // Populating max width of each column

        final Map<String, Integer> columnWidths = Maps.newLinkedHashMap();
        for (Map<String, String> row : rows) {
            for (Entry<String, String> col : row.entrySet()) {
                if (col.getValue() == null) {
                    col.setValue("<null>");
                }
                columnWidths.put(col.getKey(),
                        Math.max(Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0), col.getValue().length
                                ()));
            }
        }
        for (Entry<String, Integer> col : columnWidths.entrySet()) {
            columnWidths.put(col.getKey(),
                    Math.max(Optional.ofNullable(columnWidths.get(col.getKey())).orElse(0), col.getKey().length()));
        }

        // Calculating total width of table.

        int totalWidth = 4 + (columnWidths.size() - 1) * 3;
        for (Entry<String, Integer> col : columnWidths.entrySet()) {
            totalWidth += col.getValue();
        }

        // Print table.

        System.out.println(Strings.repeat("=", totalWidth));

        // Print column headers

        System.out.print("| ");
        boolean first = true;
        for (String key : columnWidths.keySet()) {
            if (!first) {
                System.out.print(" | ");
            } else {
                first = false;
            }

            System.out.print(key);
            System.out.print(Strings.repeat(" ", columnWidths.get(key) - key.length())); // Padding
        }
        System.out.println(" |");

        System.out.print("|");
        System.out.print(Strings.repeat("-", totalWidth - 2));
        System.out.println("|");

        // Print data

        for (Map<String, String> row : rows) {
            System.out.print("| ");
            first = true;
            for (Entry<String, Integer> column : columnWidths.entrySet()) {
                if (!first) {
                    System.out.print(" | ");
                } else {
                    first = false;
                }
                String value = Optional.ofNullable(row.get(column.getKey())).orElse("");
                System.out.print(value);
                System.out.print(Strings.repeat(" ", column.getValue() - value.length())); // Padding
            }
            System.out.println(" |");
        }

        // Print footer

        System.out.println(Strings.repeat("=", totalWidth));
    }

    public DebugTransferCommand() {
        super("debug-transfer", "Dump debug information for a transfer.");
    }

    private void printAccounts(AccountRepository accountsRepository, String userId) {
        List<Account> devices = accountsRepository.findByUserId(userId);
        List<Map<String, String>> output = Lists.transform(devices, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", input.getId());
            data.put("credentialsid", input.getCredentialsId());
            data.put("accountnumber", input.getAccountNumber());
            data.put("bankid", input.getBankId());
            data.put("name", input.getName());
            data.put("type", input.getType().toString());
            data.put("excluded", Boolean.toString(input.isExcluded()));
            data.put("favored", Boolean.toString(input.isFavored()));
            data.put("ownership", Double.toString(input.getOwnership()));
            data.put("payload", String.valueOf(input.getPayload()));
            data.put("certaindate", String.valueOf(input.getCertainDate()));
            return data;
        });
        printTable(output);
    }

    private String objToString(@Nullable Object object) {
        return object == null ? "null" : object.toString();
    }

    private void printTransfers(TransferRepository repository, String userId, ImmutableSet<UUID> transferIds) {

        List<Transfer> allTransfers = repository.findAllByUserId(userId);
        List<Transfer> transfers = FluentIterable.from(allTransfers)
                .filter(Predicates.transfersWithIdInSet(transferIds))
                .toSortedList(ORDERING_AFTER_ID);

        List<Map<String, String>> output = Lists.transform(transfers, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", objToString(input.getId()));
            data.put("credentialsid", objToString(input.getCredentialsId()));
            data.put("type", objToString(input.getType()));
            data.put("currency", objToString(input.getAmount().getCurrency()));
            data.put("amount", objToString(input.getAmount().getValue()));
            data.put("duedate", input.getDueDate() == null ?
                    "null" :
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(input.getDueDate()));
            data.put("source", objToString(input.getSource()));
            data.put("originalsource", objToString(input.getOriginalSource()));
            data.put("destination", objToString(input.getDestination().toUriAsString()));
            data.put("originaldestination", objToString(input.getOriginalDestination()));
            data.put("sourcemessage", objToString(input.getSourceMessage()));
            data.put("destinationmessage", objToString(input.getDestinationMessage()));
            return data;
        });
        printTable(output);
    }

    private void printSignableOperations(List<SignableOperation> signableOperations) {
        List<Map<String, String>> output = Lists.transform(signableOperations, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();

            data.put("underlyingid", objToString(input.getUnderlyingId()));
            data.put("id", objToString(input.getId()));
            data.put("credentialsid", objToString(input.getCredentialsId()));
            data.put("type", objToString(input.getType()));
            data.put("created", input.getCreated() == null ?
                    "null" :
                    ThreadSafeDateFormat.FORMATTER_SECONDS.format(input.getCreated()));
            data.put("updated", input.getUpdated() == null ?
                    "null" :
                    ThreadSafeDateFormat.FORMATTER_SECONDS.format(input.getUpdated()));
            data.put("status", objToString(input.getStatus()));
            data.put("statusmessage", objToString(input.getStatusMessage()));
            return data;
        });
        printTable(output);
    }

    private void printTransferEvents(TransferEventRepository repository, UUID userId, ImmutableSet<UUID> transferIds) {

        List<Map<String, String>> output = Lists.newArrayList();

        for (UUID transferId : transferIds) {
            List<TransferEvent> events = repository.findAllByUserIdAndTransferId(userId, transferId);

            for (TransferEvent input : events) {
                Map<String, String> data = Maps.newLinkedHashMap();

                data.put("transferid", objToString(input.getTransferId()));
                data.put("id", objToString(input.getId()));
                data.put("eventsource", objToString(input.getEventSource()));
                data.put("remoteaddress", objToString(input.getRemoteAddress()));

                data.put("so_created", input.getCreated() == null ? "null" : ThreadSafeDateFormat.FORMATTER_SECONDS.format(input.getCreated()));
                data.put("so_updated", input.getUpdated() == null ? "null" : ThreadSafeDateFormat.FORMATTER_SECONDS.format(input.getUpdated()));
                data.put("so_status", objToString(input.getStatus()));
                data.put("so_statusmessage", objToString(input.getStatusMessage()));

                data.put("t_type", objToString(input.getTransferType()));
                data.put("t_currency", objToString(input.getCurrency()));
                data.put("t_amount", objToString(input.getAmount()));
                data.put("t_source", objToString(input.getSource()));
                data.put("t_originalsource", objToString(input.getOriginalSource()));
                data.put("t_destination", objToString(input.getDestination()));
                data.put("t_originaldestination", objToString(input.getOriginalDestination()));
                data.put("t_sourcemessage", objToString(input.getSourceMessage()));
                data.put("t_destinationmessage", objToString(input.getDestinationMessage()));

                output.add(data);
            }
        }

        printTable(output);
    }

    private void printCredentials(CredentialsRepository credentialsRepository,
            String userId) {
        List<Credentials> devices = credentialsRepository.findAllByUserId(userId);
        List<Map<String, String>> output = Lists.transform(devices, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", input.getId());
            data.put("providername", input.getProviderName());
            data.put("status", String.valueOf(input.getStatus()));
            data.put("type", String.valueOf(input.getType()));
            data.put("fields", input.getFieldsSerialized());
            data.put("updated", String.valueOf(input.getUpdated()));
            data.put("statusUpdated", String.valueOf(input.getStatusUpdated()));
            data.put("statusPayload", input.getStatusPayload());
            data.put("debugFlag", String.valueOf(input.isDebug()));
            return data;
        });
        printTable(output);
    }

    private void printUserInfo(User user) {
        List<Map<String, String>> output = Lists.newArrayList();
        output.add(keyValueEntry("id", String.format("%s (%s)", user.getId(),
                UUIDUtils.fromTinkUUID(user.getId()).toString())));
        output.add(keyValueEntry("username", user.getUsername()));
        output.add(keyValueEntry("profile_market", user.getProfile().getMarket()));
        output.add(keyValueEntry("created", String.valueOf(user.getCreated())));
        output.add(keyValueEntry("profile_periodadjustedday",
                Integer.toString(user.getProfile().getPeriodAdjustedDay())));
        output.add(keyValueEntry("blocked", Boolean.toString(user.isBlocked())));
        output.add(keyValueEntry("flags", String.valueOf(user.getFlags())));
        output.add(keyValueEntry("debugFlag", String.valueOf(user.isDebug())));

        printTable(output);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input validation

        final String username = System.getProperty("username");
        final String userId = System.getProperty("userId");
        final String numDaysString = System.getProperty("numDays");

        final String transferId = System.getProperty("transferId");

        int numDays = Strings.isNullOrEmpty(numDaysString) ? -14 : Integer.parseInt(numDaysString);

        if (numDays > 0) {
            numDays *= -1;
        }

        System.out.println("Username to search for is: " + username);
        System.out.println("UserId to search for is: " + userId);
        System.out.println("Number of days to search for: " + numDays);
        System.out.println("Filtering only transferId: " + transferId);

        Preconditions.checkArgument(
                Strings.nullToEmpty(username).trim().length() > 0 || Strings.nullToEmpty(userId).trim().length() > 0
        );

        TransferRepository transferRepository = serviceContext.getRepository(TransferRepository.class);
        TransferEventRepository transferEventRepository = serviceContext.getRepository(TransferEventRepository.class);
        SignableOperationRepository signableOperationRepository = serviceContext.getRepository(SignableOperationRepository.class);
        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        AccountRepository accountsRepository = serviceContext.getRepository(AccountRepository.class);

        User user = Strings.isNullOrEmpty(userId) ?
                userRepository.findOneByUsername(username) :
                userRepository.findOne(userId);

        // Filter out transfers that have signable operations created no more than 14 days ago

        ImmutableList<SignableOperation> signableOperations = null;
        ImmutableSet<UUID> transferIds = null;

        if (user != null) {
            List<SignableOperation> allSignableOperations = signableOperationRepository.findAllByUserId(user.getId());

            Date cutOff = DateUtils.addDays(new Date(), numDays);

            signableOperations = FluentIterable.from(allSignableOperations)
                    .filter(filterTransferIdIfExisting(transferId))
                    .filter(Predicates.signableOperationsOfType(SignableOperationTypes.TRANSFER))
                    .filter(Predicates.signableOperationsCreatedAfter(cutOff))
                    .toSortedList(ORDERING_AFTER_UNDERLYINGID);

            transferIds = FluentIterable.from(signableOperations)
                    .transform(SignableOperation::getUnderlyingId)
                    .toSet();
        }

        // Output presented to the end user.

        System.out.println("<!-- START -->");

        try {

            if (user == null) {

                System.out.println("Could not find user.");

            } else {

                System.out.println("User");
                printUserInfo(user);

                System.out.println();
                System.out.println("Credentials");
                printCredentials(credentialsRepository, user.getId());

                System.out.println();
                System.out.println("Accounts");
                printAccounts(accountsRepository, user.getId());

                System.out.println();
                System.out.println("Signable Operations (sorted on transferId)");
                printSignableOperations(signableOperations);

                System.out.println();
                System.out.println("TransferEvents (sorted on transferId)");
                printTransferEvents(transferEventRepository, UUIDUtils.fromTinkUUID(user.getId()), transferIds);

                System.out.println();
                System.out.println("Transfers (might have been deduplicated)");
                printTransfers(transferRepository, user.getId(), transferIds);
            }

        } finally {

            // Without this finally clause, the Salt runner will not be able to match output correctly.

            System.out.println("<!-- END   -->");

        }

        // Data parsed by Salt to be able to make log searches.

        if (transferIds != null) {
            for (UUID id : transferIds) {
                System.out.println("SALT(transferid) = " + id);
            }
        }
    }

    private Predicate<SignableOperation> filterTransferIdIfExisting(final String transferId) {
        final Optional<UUID> uuid = Optional.ofNullable(Strings.isNullOrEmpty(transferId) ?
                null : UUIDUtils.fromTinkUUID(transferId));
        return o -> !uuid.isPresent() || uuid.get().equals(o.getUnderlyingId());
    }
}
