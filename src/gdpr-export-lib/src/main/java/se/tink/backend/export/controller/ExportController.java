package se.tink.backend.export.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.repository.cassandra.DataExportFragmentsRepository;
import se.tink.backend.common.repository.cassandra.DataExportsRepository;
import se.tink.backend.common.repository.mysql.main.DataExportRequestRepository;
import se.tink.backend.core.DataExport;
import se.tink.backend.core.DataExportFragment;
import se.tink.backend.core.DataExportRequest;
import se.tink.backend.export.factory.ExportObjectFactory;
import se.tink.backend.export.helper.LocalFileTemplateLoader;
import se.tink.backend.export.model.AccountHistory;
import se.tink.backend.export.model.Accounts;
import se.tink.backend.export.model.ApplicationEvents;
import se.tink.backend.export.model.Applications;
import se.tink.backend.export.model.Booleans;
import se.tink.backend.export.model.Budgets;
import se.tink.backend.export.model.Consents;
import se.tink.backend.export.model.Credentials;
import se.tink.backend.export.model.Documents;
import se.tink.backend.export.model.FacebookDetails;
import se.tink.backend.export.model.FraudDetails;
import se.tink.backend.export.model.InstrumentHistory;
import se.tink.backend.export.model.Instruments;
import se.tink.backend.export.model.Loans;
import se.tink.backend.export.model.PortfolioHistory;
import se.tink.backend.export.model.Portfolios;
import se.tink.backend.export.model.Properties;
import se.tink.backend.export.model.PropertyEstimates;
import se.tink.backend.export.model.SavingsGoals;
import se.tink.backend.export.model.Transactions;
import se.tink.backend.export.model.Transfers;
import se.tink.backend.export.model.UserDetails;
import se.tink.backend.export.model.UserDevices;
import se.tink.backend.export.model.UserEvents;
import se.tink.backend.export.model.UserLocations;
import se.tink.backend.export.validators.exception.DataExportException;
import se.tink.backend.export.validators.exception.DataExportFragmentNotFoundException;
import se.tink.backend.export.validators.exception.DataExportNotFoundException;
import se.tink.backend.rpc.DataExportRequestStatus;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.uuid.UUIDUtils;

public class ExportController {

    private static final String EXPORT_TEMPLATE_MARKDOWN = "user-data-export-md.ftl";
    private static final String EXPORT_TEMPLATES_DIRECTORY = "/data/data-export";
    private static final String SYSTEM_PATH = System.getProperty("user.dir");
    private static final int FRAGMENT_CHUNK_BYTE_SIZE = 200 * 1024;
    private static final MetricId DATA_EXPORT_DURATION = MetricId.newId("data_export_duration");
    private static final MetricId DATA_EXPORT_REQUESTS = MetricId.newId("data_export_requests");
    private static final LogUtils log = new LogUtils(ExportController.class);

    private final Configuration configuration;

    private final ExportObjectFactory exportObjectFactory;
    private final DataExportsRepository dataExportsRepository;
    private final DataExportFragmentsRepository dataExportFragmentsRepository;
    private final DataExportRequestRepository dataExportRequestRepository;

    private final ListenableThreadPoolExecutor<Runnable> executor;

    private final MetricRegistry metricRegistry;

    @Inject
    public ExportController(ExportObjectFactory exportObjectFactory,
            DataExportsRepository dataExportsRepository,
            DataExportFragmentsRepository dataExportFragmentsRepository,
            DataExportRequestRepository dataExportRequestRepository,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor,
            MetricRegistry metricRegistry) {
        this.exportObjectFactory = exportObjectFactory;
        this.dataExportsRepository = dataExportsRepository;
        this.dataExportFragmentsRepository = dataExportFragmentsRepository;
        this.dataExportRequestRepository = dataExportRequestRepository;
        this.metricRegistry = metricRegistry;

        this.configuration = new Configuration(new Version("2.3.20"));

        // Where we load the templates from:
        File file = new File(SYSTEM_PATH + EXPORT_TEMPLATES_DIRECTORY);

        LocalFileTemplateLoader templateLoader = new LocalFileTemplateLoader();
        templateLoader.setBaseDir(file);

        configuration.setTemplateLoader(templateLoader);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.executor = executor;
    }

    public DataExportRequest storeRequest(String userId) {

        // TODO add logic to check if a user already have a created request or should it be overridden?

        DataExportRequest dataExportRequest = new DataExportRequest();

        dataExportRequest.setUserId(userId);
        dataExportRequest.setId(UUIDUtils.generateUUID());
        dataExportRequest.setUpdated(new Date());

        return dataExportRequestRepository.save(dataExportRequest);
    }

    public DataExportRequest updateRequestStatus(DataExportRequest dataExportRequest, DataExportRequestStatus status) {
        dataExportRequest.setStatus(status);
        dataExportRequest.setUpdated(new Date());
        return dataExportRequestRepository.save(dataExportRequest);
    }

    private void updateRequestCompleted(DataExportRequest dataExportRequest, String exportId, String saltString) {
        dataExportRequest.setDataExportId(exportId);
        dataExportRequest.setSalt(saltString);
        updateRequestStatus(dataExportRequest, DataExportRequestStatus.COMPLETED);
    }

    public void generateAndStoreExportAsync(final String userId, final DataExportRequest inputDataExportRequest)
            throws DataExportException {

        Runnable runnable = () -> {
            final Context exportTimerContext = metricRegistry.timer(DATA_EXPORT_DURATION).time();

            DataExportRequest dataExportRequest = inputDataExportRequest;

            // Where do we load the templates from:
            String userDir = System.getProperty("user.dir");
            File file = new File(userDir + EXPORT_TEMPLATES_DIRECTORY);

            LocalFileTemplateLoader templateLoader = new LocalFileTemplateLoader();
            templateLoader.setBaseDir(file);

            log.info(userId, "Generating export for request " + dataExportRequest.getId());
            dataExportRequest = updateRequestStatus(dataExportRequest, DataExportRequestStatus.IN_PROGRESS);

            try {
                // Lot's of stuff happening on this line, where we fetch and map from all repositories
                Map<String, Object> mapper = getMappedExportObjects(userId);

                Template template = configuration.getTemplate(EXPORT_TEMPLATE_MARKDOWN);

                // 2.3. Generate the output to a temporary file
                String exportFileId = UUIDUtils.generateUUID();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // Write output to the console
                OutputStreamWriter consoleWriter = new OutputStreamWriter(
                        new BufferedOutputStream(outputStream));
                template.process(mapper, consoleWriter);

                // Fixme: Temporary workaround for piping the data from the outputStream to inputStream, since PipedInputStream didn't work
                // This essentially allocates two copies of our data in memory for a while before closing the outputStream, which might be
                // a concern if many exports are done at once on a server.
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                outputStream.close();

                storeExport(userId, exportFileId, inputStream);

                //TODO: Create hash of file, with a random salt value and store it in dataExportRequestRepository
                String salt = UUIDUtils.generateUUID();
                // Make hash of tmpExportFile and salt

                updateRequestCompleted(dataExportRequest, exportFileId, salt);
                log.info(userId, "Completed export for request" + dataExportRequest.getId());

                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", true)).inc();

            } catch (TemplateNotFoundException e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId, String.format("No export template found for %s", e.getTemplateName()), e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } catch (MalformedTemplateNameException e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId, String.format("Export template name not supported %s", e.getTemplateName()), e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } catch (ParseException e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId,
                        String.format("Could not parse template %s at line %d and column %d ", e.getTemplateName(),
                                e.getLineNumber(), e.getColumnNumber()), e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } catch (IOException | TemplateException e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId, e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } catch (NullPointerException e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId, "Data object mapper failed!", e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } catch (Exception e) {
                metricRegistry.meter(DATA_EXPORT_REQUESTS.label("success", false)).inc();
                log.error(userId, e);
                updateRequestStatus(dataExportRequest, DataExportRequestStatus.FAILED);
                throw new DataExportException(e);
            } finally {
                exportTimerContext.stop();
            }
        };

        executor.execute(runnable);
    }

    public void storeExport(String userId, String id, InputStream is) throws DataExportException {

        byte[] buffer = new byte[FRAGMENT_CHUNK_BYTE_SIZE];
        int nFragments = 0;
        int size = 0;

        try {
            int readCurrent = is.read(buffer);

            while (readCurrent != -1) {
                ByteBuffer data = ByteBuffer.wrap(buffer, 0, readCurrent);

                dataExportFragmentsRepository
                        .save(new DataExportFragment(UUIDUtils.fromTinkUUID(id), nFragments, data));

                nFragments++;
                size += readCurrent;
                readCurrent = is.read(buffer);
            }
        } catch (Exception e) {
            throw new DataExportException("Couldn't create fragment for id " + id, e);
        }

        dataExportsRepository
                .save(new DataExport(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(id), nFragments, size,
                        "text/plain"));
    }

    public byte[] fetchExport(UUID userId, UUID id) throws IOException, DataExportNotFoundException,
            DataExportFragmentNotFoundException, DataAccessException {

        DataExportRequest request = getValidRequest(UUIDUtils.toTinkUUID(userId), UUIDUtils.toTinkUUID(id));
        String exportId = request.getDataExportId() != null ? request.getDataExportId() : request.getLink();

        DataExport dataExport = dataExportsRepository.findOneByUserIdAndId(userId, UUIDUtils.fromTinkUUID(exportId));

        if (dataExport == null) {
            throw new DataExportNotFoundException("Couldn't find data export by id " + id);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < dataExport.getCount(); i++) {
            DataExportFragment dataExportFragment = dataExportFragmentsRepository
                    .findOneByIdAndIndex(dataExport.getId(), i);

            if (dataExportFragment == null) {
                throw new DataExportFragmentNotFoundException("Fragment doesn't exist for data export with id" + id);
            }
            outputStream.write(dataExportFragment.getByteData());
        }

        // TODO: set/decide mimetype, hardcoded atm
        return outputStream.toByteArray();
    }

    public List<DataExportRequest> getDataExportRequests(String userId) {
        return dataExportRequestRepository.findByUserId(userId);
    }

    public Map<String, Object> getMappedExportObjects(String userId) {
        // Map of objects to populate template file
        Map<String, Object> templateObjectMapper = Maps.newHashMap();

        // Create objects for mapping
        // Todo: Remove this class, and put booleans into relevant objects
        Booleans booleans = exportObjectFactory.createExportBooleans(userId);
        templateObjectMapper.put("booleans", booleans);

        // Todo: Make smaller groups of export objects
        templateObjectMapper.putAll(getMappedUserDetailsExportObjects(userId));
        templateObjectMapper.putAll(getMappedUserFinancialDetailsExportObjects(userId));

        return templateObjectMapper;
    }

    private Map<String, Object> getMappedUserDetailsExportObjects(String userId) throws DataExportException {

        Map<String, Object> objectMap = Maps.newHashMap();

        try {

            UserDetails userDetails = exportObjectFactory.createUserDetails(userId);
            objectMap.put("userDetails", userDetails);

            FacebookDetails facebookDetails = exportObjectFactory.createFacebookDetails(userId);
            objectMap.put("fb", facebookDetails);

            FraudDetails fraudDetails = exportObjectFactory.createFraudDetails(userId);
            objectMap.put("fraudDetails", fraudDetails);

            Consents consents = exportObjectFactory.createUserConsents(userId);
            objectMap.put("consents", consents);

            UserDevices userDevices = exportObjectFactory.createUserDevices(userId);
            objectMap.put("userDevices", userDevices);

            UserEvents userEvents = exportObjectFactory.createUserEvents(userId);
            objectMap.put("userEvents", userEvents);

            UserLocations userLocations = exportObjectFactory.createUserLocations(userId);
            objectMap.put("userLocations", userLocations);

            Applications applications = exportObjectFactory.createApplications(userId);
            objectMap.put("applications", applications);

            ApplicationEvents applicationEvents = exportObjectFactory
                    .createApplicationEvents(userId);
            objectMap.put("applicationEvents", applicationEvents);

            return objectMap;
        } catch (Exception e) {
            log.error(userId, "Export object mapper failed!", e);
            throw new DataExportException(e);
        }
    }

    private Map<String, Object> getMappedUserFinancialDetailsExportObjects(String userId) {
        Map<String, Object> objectMap = Maps.newHashMap();

        try {

            Properties properties = exportObjectFactory.createProperties(userId);
            objectMap.put("properties", properties);

            PropertyEstimates propertyEstimates = exportObjectFactory.createPropertyEstimates(userId);
            objectMap.put("propertyEstimates", propertyEstimates);

            Documents documents = exportObjectFactory.createDocuments(userId);
            objectMap.put("documents", documents);

            Credentials credentials = exportObjectFactory.createCredentials(userId);
            objectMap.put("credentials", credentials);

            Loans loans = exportObjectFactory.createLoans(userId);
            objectMap.put("loans", loans);

            Accounts accounts = exportObjectFactory.createAccounts(userId);
            objectMap.put("accounts", accounts);

            AccountHistory accountHistory = exportObjectFactory.createAccountHistory(userId);
            objectMap.put("accountHistory", accountHistory);

            Transactions userTransactions = exportObjectFactory.createTransactions(userId);
            objectMap.put("userTransactions", userTransactions);

            Transfers transfers = exportObjectFactory.createTransfers(userId);
            objectMap.put("transfers", transfers);

            Portfolios portfolios = exportObjectFactory.createPortfolios(userId);
            objectMap.put("portfolios", portfolios);

            PortfolioHistory portfolioHistory = exportObjectFactory.createPortfolioHistory(userId);
            objectMap.put("portfolioHistory", portfolioHistory);

            Instruments instruments = exportObjectFactory.createInstruments(userId);
            objectMap.put("instruments", instruments);

            InstrumentHistory instrumentHistory = exportObjectFactory
                    .createInstrumentHistory(userId);
            objectMap.put("instrumentHistory", instrumentHistory);

            Budgets budgets = exportObjectFactory.createBudgets(userId);
            objectMap.put("budgets", budgets);

            SavingsGoals savingsGoals = exportObjectFactory.createSavingsGoals(userId);
            objectMap.put("savingsGoals", savingsGoals);

            return objectMap;
        } catch (Exception e) {
            log.error(userId, "Export object mapper failed!", e);
            throw new DataExportException(e);
        }
    }

    private DataExportRequest getValidRequest(String userId, String id) {
        DataExportRequest request = dataExportRequestRepository.findByUserIdAndId(userId, id);
        Preconditions.checkNotNull(request);
        Preconditions.checkArgument(Objects.equals(request.getStatus(), DataExportRequestStatus.COMPLETED));
        return request;
    }

}
