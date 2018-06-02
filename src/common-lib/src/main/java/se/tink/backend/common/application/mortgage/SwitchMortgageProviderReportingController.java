package se.tink.backend.common.application.mortgage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.application.ApplicationArchiveRow;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.application.FieldData;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwitchMortgageProviderReportingController {

    private static final LogUtils log = new LogUtils(SwitchMortgageProviderReportingController.class);

    private static final TypeReference<List<ConfirmationFormListData>> ARCHIVE_CONTENT_TYPE_REFERENCE = new TypeReference<List<ConfirmationFormListData>>() {
    };

    private static final Pattern AMOUNT_FIELD_PATTERN = Pattern.compile("\\d+.*kr");
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D+");
    private static final Pattern PROVIDER_PATTERN = Pattern.compile("^(?<provider>.*?)(?= ?\\d)");
    private static final Locale LOCALE = new Locale("sv", "SE");
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance(LOCALE);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE);
    private static final Joiner HTML_LINE_JOINER = Joiner.on("<br>\n");

    private static final Function<MortgageReportEntry, String> MORTGAGE_REPORT_ENTRY_TO_STRING = e -> {
        return String
                .format("%s\t%s\t%s", ThreadSafeDateFormat.FORMATTER_DAILY.format(e.executed), e.externalId,
                        CURRENCY_FORMAT.format(e.amount));
    };

    private static final Comparator<MortgageReportEntry> MORTGAGE_REPORT_ENTRY_BY_EXECUTION_DATE = (e1, e2) -> {
        return DateUtils.compare(e1.executed, e2.executed);
    };

    private final ApplicationDAO applicationDAO;
    private final ApplicationArchiveRepository applicationArchiveRepository;
    private final BackOfficeConfiguration configuration;
    private final MailSender mailSender;

    private boolean print = false;
    private boolean dryRun = false;

    // Populated by calling one of the load methods
    private ImmutableMap<String, Integer> totalAmountByCurrentProvider;
    private ImmutableMap<String, Integer> totalAmountByNewProvider;
    private ImmutableListMultimap<String, MortgageReportEntry> mortgageEntriesByNewProvider;

    static {
        PERCENT_FORMAT.setRoundingMode(RoundingMode.HALF_DOWN);
        PERCENT_FORMAT.setMaximumFractionDigits(0);

        CURRENCY_FORMAT.setMaximumFractionDigits(0);
    }

    @Inject
    public SwitchMortgageProviderReportingController(ApplicationDAO applicationDAO,
            ApplicationArchiveRepository applicationArchiveRepository, BackOfficeConfiguration configuration,
            MailSender mailSender) {
        this.applicationDAO = applicationDAO;
        this.applicationArchiveRepository = applicationArchiveRepository;
        this.configuration = configuration;
        this.mailSender = mailSender;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean isPrint() {
        return print;
    }

    private boolean isDataLoaded() {
        return totalAmountByCurrentProvider != null && !totalAmountByCurrentProvider.isEmpty()
                && totalAmountByNewProvider != null && !totalAmountByNewProvider.isEmpty()
                && mortgageEntriesByNewProvider != null && !mortgageEntriesByNewProvider.isEmpty();
    }

    private void loadData(final CompileAndSendReportCommand command) {

        log.info(String.format("Loading data for %s to %s",
                ThreadSafeDateFormat.FORMATTER_SECONDS.format(command.getStartDate()),
                ThreadSafeDateFormat.FORMATTER_SECONDS.format(command.getEndDate())));

        ImmutableListMultimap.Builder<String, MortgageReportEntry> builder = ImmutableListMultimap.builder();

        final AtomicInteger totalApplicationsLoaded = new AtomicInteger();
        final AtomicInteger totalEntriesCreated = new AtomicInteger();
        final AtomicInteger totalEntriesIncluded = new AtomicInteger();
        final Map<String, AtomicInteger> tmpTotalAmountByCurrentProvider = Maps.newHashMap();
        final Map<String, AtomicInteger> tmpTotalAmountByNewProvider = Maps.newHashMap();

        applicationDAO.streamAll()
                .filter(a -> Objects.equals(a.getType(), ApplicationType.SWITCH_MORTGAGE_PROVIDER))
                .filter(a -> Objects.equals(a.getStatus().getKey(), ApplicationStatusKey.EXECUTED))
                .doOnNext(a -> totalApplicationsLoaded.incrementAndGet())
                .map(this::getMortgageReportEntry)
                .filter(Objects::nonNull)
                .forEach(entry -> {
                    totalEntriesCreated.incrementAndGet();

                    AtomicInteger totalAmountForCurrentProvider = tmpTotalAmountByCurrentProvider
                            .computeIfAbsent(entry.currentProvider, p -> new AtomicInteger());
                    totalAmountForCurrentProvider.addAndGet(entry.amount);

                    AtomicInteger totalAmountForNewProvider = tmpTotalAmountByNewProvider
                            .computeIfAbsent(entry.newProvider, p -> new AtomicInteger());
                    totalAmountForNewProvider.addAndGet(entry.amount);

                    // We only need the full entry if it's within the specified date range.
                    if (command.isDateIncluded(entry.executed)) {
                        totalEntriesIncluded.incrementAndGet();
                        builder.put(entry.newProvider, entry);
                    }
                });

        totalAmountByCurrentProvider = ImmutableMap
                .copyOf(Maps.transformValues(tmpTotalAmountByCurrentProvider, AtomicInteger::intValue));
        totalAmountByNewProvider = ImmutableMap
                .copyOf(Maps.transformValues(tmpTotalAmountByNewProvider, AtomicInteger::intValue));
        mortgageEntriesByNewProvider = builder.build();

        log.info(String.format(
                "Loading data completed (%s applications were loaded, from which %s entries where created of which %s are within the specified period)",
                totalApplicationsLoaded.intValue(), totalEntriesCreated.intValue(), totalEntriesIncluded.intValue()));
    }

    public void compileAndSendReport(CompileAndSendReportCommand command) {
        log.info("Compile and send report.");

        if (command == null) {
            log.error("The command is undefined. Aborting.");
            return;
        }

        String report = compileReport(command);

        if (Strings.isNullOrEmpty(report)) {
            log.error("The report is empty. Aborting.");
            return;
        }

        if (isPrint()) {
            System.out.println(report);
        }

        if (isDryRun()) {
            log.info("This is just a dry run. Not sending the report as an email.");
            return;
        }

        if (sendReport(report, command.getDescription())) {
            log.info("The report was sent successfully.");
        }
    }

    private String compileReport(CompileAndSendReportCommand command) {
        log.info("Compile report");

        loadData(command);

        if (!isDataLoaded()) {
            log.error("Unable to compile report. Data has not been loaded.");
            return null;
        }

        List<String> lines = Lists.newArrayList();
        lines.add(command.getDescription());
        lines.add("");
        lines.add("");
        lines.addAll(compileReportForProvider("SEB"));
        lines.add("");
        lines.add("");
        lines.addAll(compileReportForProvider("SBAB"));

        if (isDryRun()) {
            lines.add("");
            lines.add("");
            lines.addAll(compileReportForProvider("Unknown"));
        }

        return HTML_LINE_JOINER.join(lines);
    }

    private List<String> compileReportForProvider(String provider) {
        if (!isDataLoaded()) {
            log.error("Unable to compile report. Data has not been loaded.");
            return null;
        }

        int totalAmount = totalAmountByCurrentProvider.values().stream().mapToInt(Integer::intValue).sum();
        int totalAmountFrom = Optional.ofNullable(totalAmountByCurrentProvider.get(provider)).orElse(0);
        int totalAmountTo = Optional.ofNullable(totalAmountByNewProvider.get(provider)).orElse(0);

        List<String> lines = Lists.newArrayList();

        lines.add(Catalog.format("<h1>{0}</h1>", provider));
        lines.add("");
        lines.add(Catalog.format("Total volym från: {0} ({1})", CURRENCY_FORMAT.format(totalAmountFrom),
                PERCENT_FORMAT.format(divide(totalAmountFrom, totalAmount))));
        lines.add(Catalog.format("Total volym till: {0} ({1})", CURRENCY_FORMAT.format(totalAmountTo),
                PERCENT_FORMAT.format(divide(totalAmountTo, totalAmount))));
        lines.add("");

        lines.add("Utbetalda under perioden:");
        lines.add("");

        List<MortgageReportEntry> entries = mortgageEntriesByNewProvider.get(provider);
        if (entries == null || entries.isEmpty()) {
            lines.add("Inga");
        } else {
            lines.add("Datum\tExternt id\tBelopp");
            lines.addAll(entries.stream().sorted(MORTGAGE_REPORT_ENTRY_BY_EXECUTION_DATE)
                    .map(MORTGAGE_REPORT_ENTRY_TO_STRING).collect(Collectors.toList()));
        }

        return lines;
    }

    private static double divide(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }

        return (double) numerator / (double) denominator;
    }

    private boolean sendReport(String report, String description) {
        log.info("Send report");

        if (configuration == null) {
            log.error("No back-office configuration available. Aborting.");
            return false;
        }

        if (!configuration.isEnabled()) {
            log.info("Back-office functionality is disabled. Aborting.");
            return false;
        }

        if (Strings.isNullOrEmpty(configuration.getReportingEmailAddress())) {
            log.info("No reporting email specified. Aborting.");
            return false;
        }

        try {
            return mailSender.sendMessage(configuration.getReportingEmailAddress(),
                    Catalog.format("Rapportering av bolån - {0}", description), configuration.getFromAddress(),
                    configuration.getFromName(), report, true);
        } catch (Exception e) {
            log.error("Unable to send email.", e);
            return false;
        }
    }

    private MortgageReportEntry getMortgageReportEntry(Application application) {

        Optional<ApplicationArchiveRow> archive = applicationArchiveRepository
                .findByUserIdAndApplicationId(application.getUserId(), application.getId());

        if (!archive.isPresent()) {
            log.warn(String.format("[applicationId:%s] No archive entry available",
                    application.getId().toString()));
            return null;
        }

        MortgageSpecification specification = getSpecification(archive.get());

        if (!specification.isComplete()) {
            log.warn(
                    String.format("[applicationId:%s] Mortgage specification could not be extracted from archive entry",
                            application.getId().toString()));
            return null;
        }

        return new MortgageReportEntry(
                archive.get().getExternalId(),
                application.getStatus().getUpdated(),
                specification.currentProvider,
                specification.newProvider,
                specification.amount);
    }

    private MortgageSpecification getSpecification(ApplicationArchiveRow archive) {

        List<ConfirmationFormListData> content = SerializationUtils
                .deserializeFromString(archive.getContent(), ARCHIVE_CONTENT_TYPE_REFERENCE);

        List<FieldData> mortgageGroup = content.stream()
                .filter(d -> Objects.equals(d.getTitle(), "Bolån"))
                .flatMap(d -> d.getFields().stream())
                .collect(Collectors.toList());

        Optional<FieldData> currentMortgageFieldData = mortgageGroup.stream()
                .filter(d -> Objects.equals(d.getTitle(), "Befintliga lån"))
                .findFirst();

        Optional<FieldData> newMortgageFieldData = mortgageGroup.stream()
                .filter(d -> Objects.equals(d.getTitle(), "Nytt lån"))
                .findFirst();

        if (!currentMortgageFieldData.isPresent()) {
            log.warn(String.format("[applicationId:%s] Unable to extract current mortgage information from archive.",
                    archive.getApplicationId().toString()));
            return null;
        }

        if (!newMortgageFieldData.isPresent()) {
            log.warn(String.format("[applicationId:%s] Unable to extract new mortgage information from archive.",
                    archive.getApplicationId().toString()));
            return null;
        }

        KVPair<String, Integer> currentMortgage = getProviderAndAmount(currentMortgageFieldData.get());

        if (currentMortgage == null) {
            log.warn(String.format(
                    "[applicationId:%s] Unable to extract current mortgage information from field data in archive.",
                    archive.getApplicationId().toString()));
            return null;
        }

        KVPair<String, Integer> newMortgage = getProviderAndAmount(newMortgageFieldData.get());

        if (newMortgage == null) {
            log.warn(String.format(
                    "[applicationId:%s] Unable to extract new mortgage information from field data in archive.",
                    archive.getApplicationId().toString()));
            return null;
        }

        String newProvider = "Unknown";

        if (newMortgage.getKey() != null) {
            if (newMortgage.getKey().toUpperCase().contains("SEB")) {
                newProvider = "SEB";
            } else if (newMortgage.getKey().toUpperCase().contains("SBAB")) {
                newProvider = "SBAB";
            }
        }

        if (Objects.equals("Unknown", newProvider)) {
            log.warn(String.format("[applicationId:%s] Unable to identify new provider.",
                    archive.getApplicationId().toString()));
        }

        return new MortgageSpecification(currentMortgage.getKey(), newProvider, newMortgage.getValue());
    }

    private KVPair<String, Integer> getProviderAndAmount(FieldData fieldData) {

        String provider = null;
        Integer amount = null;

        for (List<String> fieldValues : fieldData.getValues()) {
            for (String fieldValue : fieldValues) {
                if (AMOUNT_FIELD_PATTERN.matcher(fieldValue).find()) { // We do this in case the order is not fixed
                    amount = Integer.parseInt(NON_DIGIT_PATTERN.matcher(fieldValue).replaceAll(""));
                } else if (fieldValue.contains("ränta")) {
                    Matcher matcher = PROVIDER_PATTERN.matcher(fieldValue);
                    if (matcher.find()) {
                        provider = matcher.group("provider");
                    }
                }

                if (amount != null && !Strings.isNullOrEmpty(provider)) {
                    return new KVPair<>(provider, amount);
                }
            }
        }

        return null;
    }

    class MortgageSpecification {
        private final String currentProvider;
        private final String newProvider;
        private final Integer amount;

        MortgageSpecification(String currentProvider, String newProvider, Integer amount) {
            this.currentProvider = currentProvider;
            this.newProvider = newProvider;
            this.amount = amount;
        }

        public boolean isComplete() {
            return !Strings.isNullOrEmpty(currentProvider) && !Strings.isNullOrEmpty(newProvider) && amount != null;
        }
    }

    class MortgageReportEntry {
        private final String externalId;
        private final Date executed;
        private final String currentProvider;
        private final String newProvider;
        private final int amount;

        MortgageReportEntry(String externalId, Date executed, String currentProvider, String newProvider, int amount) {
            this.externalId = externalId;
            this.executed = executed;
            this.currentProvider = currentProvider;
            this.newProvider = newProvider;
            this.amount = amount;
        }
    }
}
