package se.tink.backend.system.cli.abnamro;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.config.DatawarehouseRemoteConfiguration;

public class ExportCustomerDetailsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private FollowItemRepository followItemRepository;
    private AbnAmroConfiguration abnAmroConfiguration;
    private DeviceRepository deviceRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String OUTPUT_FILE = "/var/tmp/Grip_AAB_BC_Properties_%s.csv.gz";
    private static final String SCP_BINARY_LOCATION = "/usr/bin/scp";
    private static final String SFTP_BINARY_LOCATION = "/usr/bin/sftp";
    private static final String SFTP_COMMANDS_TMP_FILENAME = "sftp_commands";
    private static final String SFTP_COMMANDS_TMP_SUFFIX = "tmp";
    private static final String LEGACY_CIPHER_SUITE = "KexAlgorithms=diffie-hellman-group1-sha1";

    private static final List<FollowTypes> BUDGET_FOLLOW_TYPES = Lists.newArrayList(FollowTypes.EXPENSES,
            FollowTypes.SEARCH);

    private static final Object[] FILE_HEADER = {
            "BcNumber",
            "FirstLogin",
            "LastLogin",
            "InitialCategorizationLevel",
            "CategorizationLevel",
            "UsingTags",
            "NumberOfBudgets",
            "EnabledPushNotifications",
            "Locale"
    };

    private static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(';').withRecordSeparator('\n').withQuote('"')
            .withEscape('\\').withNullString("");

    public ExportCustomerDetailsCommand() {
        super("abnamro-export-customer-details", "Export customer specific data. ");
    }

    private static final LogUtils log = new LogUtils(ExportCustomerDetailsCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        userRepository = serviceContext.getRepository(UserRepository.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        followItemRepository = serviceContext.getRepository(FollowItemRepository.class);
        deviceRepository = serviceContext.getRepository(DeviceRepository.class);
        abnAmroConfiguration = serviceContext.getConfiguration().getAbnAmro();

        if (!isScpInstalled()) {
            throw new FileNotFoundException(String.format("Could not find executable at location %s",
                    SCP_BINARY_LOCATION));
        }


        File localFile = new File(String.format(OUTPUT_FILE, DATE_FORMAT.format(LocalDateTime.now())));
        try {
            generateCustomerDetailsFile(localFile);
            sendFileToRemoteHost(localFile);
        } finally {
            if (abnAmroConfiguration.getDatawarehouseRemoteConfiguration().shouldDeleteFileWhenDone()) {
                localFile.delete();
            }
        }

    }

    private boolean isScpInstalled() {
        File scp_executable = new File(SCP_BINARY_LOCATION);
        return scp_executable.canExecute();
    }

    private void sendFileToRemoteHost(File localFile) throws IOException, InterruptedException {
        DatawarehouseRemoteConfiguration configuration = abnAmroConfiguration.getDatawarehouseRemoteConfiguration();

        if (configuration.shouldUseSftp()) {
            sendFileSftp(localFile, configuration);
        } else {
            sendFileScp(localFile, configuration);
        }
    }

    private void sendFileSftp(File localFile, DatawarehouseRemoteConfiguration configuration) throws IOException,
            InterruptedException {
        String remoteHost = configuration.getHost();
        String remoteDirectory = configuration.getRemotePath();
        String remotePort = String.format("-P %d", configuration.getRemotePort());

        /* When using SFTP we create a temporary batch file since SFTP can't be run solely from the command line */
        File sftpCommands = File.createTempFile(SFTP_COMMANDS_TMP_FILENAME, SFTP_COMMANDS_TMP_SUFFIX);
        try (FileWriter sftpCommandsWriter = new FileWriter(sftpCommands)) {
            sftpCommandsWriter.write(String.format("chdir %s\n", remoteDirectory));
            sftpCommandsWriter.write(String.format("put %s\n", localFile.getAbsolutePath()));
        }

        ProcessBuilder processBuilder;

        if (configuration.shouldUseDiffieHellmanSha1()) {
            processBuilder = new ProcessBuilder(SFTP_BINARY_LOCATION, "-q", "-b", "-", "-o",
                    LEGACY_CIPHER_SUITE, remotePort, remoteHost);
        } else {
            processBuilder = new ProcessBuilder(SFTP_BINARY_LOCATION, "-q", "-b", "-",
                    remotePort, remoteHost);
        }

        processBuilder.redirectInput(sftpCommands);
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IOException("sftp did not return status code 0");
        };
    }

    private void sendFileScp(File localFile, DatawarehouseRemoteConfiguration configuration) throws IOException,
            InterruptedException {
        String remoteHost = configuration.getHost();
        String remoteDirectory = configuration.getRemotePath();
        String remotePort = String.format("-P %d", configuration.getRemotePort());
        String remotePath = String.format("%s:%s", remoteHost, remoteDirectory);
        ProcessBuilder processBuilder;

        if (configuration.shouldUseDiffieHellmanSha1()) {
            processBuilder = new ProcessBuilder(SCP_BINARY_LOCATION, "-B", "-o",
                    LEGACY_CIPHER_SUITE, remotePort, localFile.getAbsolutePath(), remotePath);
        } else {
            processBuilder = new ProcessBuilder(SCP_BINARY_LOCATION, "-B",
                    remotePort, localFile.getAbsolutePath(), remotePath);
        }
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IOException("scp did not return status code 0");
        };
    }

    private void generateCustomerDetailsFile(File file) throws IOException {
        file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(gzipOutputStream);
        CSVPrinter csvFilePrinter = new CSVPrinter(outputStreamWriter, CSV_FORMAT);

        csvFilePrinter.printRecord(FILE_HEADER);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        print(user, csvFilePrinter);
                    } catch (Exception e) {
                        log.error(user.getId(), "Failed to export user", e);
                    }
                });

        csvFilePrinter.flush();
        csvFilePrinter.close();
    }

    private Row generateRow(User user, UserState userState) {
        Row row = new Row();

        row.bcNumber = user.getUsername();
        row.firstLogin = dateToIsoString(user.getCreated());
        row.numberOfBudgets = followItemRepository.countByUserIdAndTypeIn(user.getId(), BUDGET_FOLLOW_TYPES);
        row.hasEnabledPushNotifications = deviceRepository.countByUserId(user.getId()) > 0;
        row.locale = user.getLocale();

        if (userState == null) {
            log.error(user.toString(), "Could not fetch user state");
            row.usingTags = false;
        } else {
            row.lastLogin = dateToIsoString(userState.getLastLogin());
            row.initialCategorizationLevel = userState.getInitialAmountCategorizationLevel();
            row.categorizationLevel = userState.getAmountCategorizationLevel();
            row.usingTags = userState.getTags().size() != 0;
        }

        return row;
    }

    private void print(User user, CSVPrinter csvFilePrinter) throws IOException {
        UserState userState = userStateRepository.findOneByUserId(user.getId());
        Row row = generateRow(user, userState);
        csvFilePrinter.printRecord(row.toRecord());
    }

    private String dateToIsoString(Date date) {
        if (date == null) {
            return "";
        }

        return date.toInstant().atOffset(ZoneOffset.UTC).toString();
    }

    private class Row {
        private String bcNumber;
        private String firstLogin;
        private String lastLogin;
        private Long initialCategorizationLevel;
        private Long categorizationLevel;
        private boolean usingTags;
        private long numberOfBudgets;
        private boolean hasEnabledPushNotifications;
        private String locale;

        private List<Object> toRecord() {
            return Lists.newArrayList(bcNumber,
                    firstLogin,
                    lastLogin,
                    initialCategorizationLevel,
                    categorizationLevel,
                    usingTags,
                    numberOfBudgets,
                    hasEnabledPushNotifications,
                    locale);
        }
    }
}
