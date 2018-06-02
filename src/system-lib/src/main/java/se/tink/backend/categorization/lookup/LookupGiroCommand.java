package se.tink.backend.categorization.lookup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.utils.giro.GiroParser;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.Giro;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Provider;
import se.tink.backend.core.SwedishGiroType;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Looks up information about Giro account numbers by scraping the BGC account query page. Caches found Giro information
 * locally and uses the resolved organization id to lookup the correct category.
 * <p/>
 * Resets: type
 * <p/>
 * Sets: description categoryId type TransactionPayloadTypes.GIRO
 */
public class LookupGiroCommand implements TransactionProcessorCommand {
    private static final LogUtils log = new LogUtils(LookupGiroCommand.class);

    private static final String BG_GIRO_PREFIX = "BG ";
    private static final String PG_GIRO_PREFIX = "PG ";
    private static final String BG_FOUND_ONE_HIT_PATTERN = "(1)";
    private static final String BG_LOOKUP_URL = "http://www.bankgirot.se/sok-bankgironummer/?bgnr=";
    private static final MetricId DETECTED_TRANSACTIONS = MetricId.newId("transactions_giro_detected");
    private static final MetricId FOUND_LOOKUP = MetricId.newId("transactions_giro_found_lookup");
    private static final MetricId FOUND_CACHE = MetricId.newId("transactions_giro_found_cache");
    private static final MetricId CATEGORIZED_TRANSACTIONS = MetricId.newId("transactions_giro_categorized");

    private static final GiroParser parser = new GiroParser();
    private final CategoryChangeRecordDao categoryChangeRecordDao;

    private MerchantRepository merchantRepository;
    private GiroRepository giroRepository;
    private Counter detectedTransactionMeter;
    private Counter foundLookupTransactionMeter;
    private Counter foundCachedTransactionMeter;
    private Counter categorizedTransactionMeter;

    private HttpClient client;

    @VisibleForTesting
    /*package*/ LookupGiroCommand(MerchantRepository merchantRepository, GiroRepository giroRepository,
            MetricRegistry registry, CategoryChangeRecordDao categoryChangeRecordDao) {

        detectedTransactionMeter = registry.meter(DETECTED_TRANSACTIONS);
        foundLookupTransactionMeter = registry.meter(FOUND_LOOKUP);
        foundCachedTransactionMeter = registry.meter(FOUND_CACHE);
        categorizedTransactionMeter = registry.meter(CATEGORIZED_TRANSACTIONS);

        client = new DefaultHttpClient();

        this.merchantRepository = merchantRepository;
        this.giroRepository = giroRepository;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
    }

    // TODO: Refactor this method into submethods to make it more maintainable and readable.
    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        if (transaction.isUserModifiedCategory()) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        // lookup giro

        Giro giro = null;

        String giroPayload = transaction.getPayloadValue(TransactionPayloadTypes.GIRO);

        if (giroPayload != null) {
            giro = parser.parse(giroPayload);
        }

        if (giro == null) {
            giro = parser.parse(transaction.getOriginalDescription());
        }

        if (giro == null) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        detectedTransactionMeter.inc();

        // No categorization mapping for PG, but we can still save the giro number. 

        if (giro.getType() != SwedishGiroType.BG) {
            transaction.setType(TransactionTypes.PAYMENT);
            transaction.setPayload(TransactionPayloadTypes.GIRO, PG_GIRO_PREFIX + giro.getAccountNumber());
            return TransactionProcessorCommandResult.CONTINUE;
        }

        try {
            // Look in our cache for the giro number.

            Giro existingGiro = giroRepository.findOneByAccountNumber(giro.getAccountNumber());

            if (existingGiro != null) {
                giro = existingGiro;

                foundCachedTransactionMeter.inc();
            } else {
                // Fetch the giro information if we don't have it locally.

                @SuppressWarnings("deprecation")
                HttpGet request = new HttpGet(BG_LOOKUP_URL + URLEncoder.encode(giro.getAccountNumber()));
                HttpResponse response = client.execute(request);

                giro.setInserted(new Date());

                String content = EntityUtils.toString(response.getEntity());

                // Zero or multiple results doesn't cut it.

                Document doc = Jsoup.parse(content);

                // Verify that we only got one result from giro lookup
                if (!doc.select(".num-of-hits").text().equalsIgnoreCase(BG_FOUND_ONE_HIT_PATTERN)) {
                    giroRepository.save(giro);

                    return TransactionProcessorCommandResult.CONTINUE;
                }

                Element detailsRow = doc.select(".search-result").select(".result-container").get(0);

                try {
                    String name = StringUtils.formatHuman(detailsRow.select(".title").get(0).text());
                    String organizationId = null;

                    for (Element section : detailsRow.select(".subtitle")) {

                        // Find section that has organization number
                        if (section.text().equalsIgnoreCase("Organisationsnummer")) {
                            organizationId = section.parent().select("li").get(1).text();
                            break;
                        }
                    }

                    giro.setName(name);
                    giro.setOrganizationId(organizationId);

                    foundLookupTransactionMeter.inc();

                } catch (Exception e) {
                    log.error(transaction.getUserId(), transaction.getCredentialsId(),
                            "Could not parse response information, setting an empty giro cache entry: " + detailsRow
                                    .toString(), e);
                }

                giroRepository.save(giro);
            }

            // Found an entry, but no organizational id.

            if (Strings.isNullOrEmpty(giro.getOrganizationId())) {
                if (!Strings.isNullOrEmpty(giro.getName())) {
                    transaction.setDescription(StringUtils.formatHuman(giro.getName()));
                }

                return TransactionProcessorCommandResult.CONTINUE;
            }

            Merchant merchant = merchantRepository.findOneByOrganizationId(giro.getOrganizationId());

            // Found an entry, but not mapped to a category.

            if (merchant == null) {
                log.info(transaction.getUserId(), transaction.getCredentialsId(),
                        "Found giro organization, but no merchant:" + giro.getOrganizationId());

                if (!Strings.isNullOrEmpty(giro.getName())) {
                    transaction.setDescription(StringUtils.formatHuman(giro.getName()));
                }

                if (!Strings.isNullOrEmpty(giro.getAccountNumber())) {
                    transaction.setPayload(TransactionPayloadTypes.GIRO, BG_GIRO_PREFIX + giro.getAccountNumber());
                }

                return TransactionProcessorCommandResult.CONTINUE;
            }

            // Set the category and description based on the merchant we found.
            Optional<String> oldCategory = Optional.ofNullable(transaction.getCategoryId());

            transaction.setDescription(StringUtils.formatHuman(merchant.getName()));
            transaction.setCategoryId(merchant.getCategoryId());

            transaction.setType(TransactionTypes.PAYMENT);
            transaction.setPayload(TransactionPayloadTypes.GIRO, BG_GIRO_PREFIX + giro.getAccountNumber());

            // Save that we changed category here instead of in TransactionProcessing
            categoryChangeRecordDao.save(CategoryChangeRecord.createChangeRecord(transaction,
                    oldCategory, this.toString()), categoryChangeRecordDao.CATEGORY_CHANGE_RECORD_TTL_DAYS,
                    TimeUnit.DAYS);

            categorizedTransactionMeter.inc();
        } catch (Exception e) {
            log.error(transaction.getUserId(), transaction.getCredentialsId(), "Could not lookup giro information", e);
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    public static Optional<LookupGiroCommand> build(Provider provider, MerchantRepository merchantRepository,
            GiroRepository giroRepository, MetricRegistry registry, CategoryChangeRecordDao categoryChangeRecordDao,
            CategorizationConfiguration categorizationConfiguration) {

        if (!Objects.equal(provider.getMarket(), "SE") || !categorizationConfiguration.isEnableGiroLookup()) {
            return Optional.empty();
        }

        return Optional
                .of(new LookupGiroCommand(merchantRepository, giroRepository, registry, categoryChangeRecordDao));
    }
}
