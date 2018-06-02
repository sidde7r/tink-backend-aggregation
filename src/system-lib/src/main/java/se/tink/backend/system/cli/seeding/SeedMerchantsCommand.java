package se.tink.backend.system.cli.seeding;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import jxl.Sheet;
import jxl.Workbook;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Client;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.system.cli.ExcelUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class SeedMerchantsCommand extends ServiceContextCommand<ServiceConfiguration> {
    public SeedMerchantsCommand() {
        super("seed-merchants", "Seed the database with the merchant data-set.");
    }

    private static TreeMap<String, String> categoryMappings = new TreeMap<String, String>();
    private static final LogUtils log = new LogUtils(SeedMerchantsCommand.class);

    private static TransactionProcessorContext transactionProcessorContext;

    private static DefaultHttpClient placesClient = new DefaultHttpClient();

    private static final String PLACES_API_KEY = "AIzaSyBLOe1yJqLO64Pk_ytk4HOTphlD4T7DGTA";
    private static final String PLACES_QUERY_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?sensor=false";

    private static void loadCategories(ServiceContext serviceContext) {
        CategoryRepository categoryRepo = serviceContext.getRepository(CategoryRepository.class);

        Iterable<Category> categories = categoryRepo.findAll();

        for (Category c : categories) {
            if (c.getCode() != null) {
                categoryMappings.put(c.getCode(), c.getId());
            }
        }
    }

    public static void seedMerchants(ServiceContext serviceContext, Client searchClient)
            throws Exception {
        loadCategories(serviceContext);

        MerchantRepository merchantRepo = serviceContext.getRepository(MerchantRepository.class);

        merchantRepo.deleteAllInBatch();

        // Load the industry mappings.

        log.info("Loading industry mappings...");

        List<KVPair<String, String>> industryMappings = new ArrayList<KVPair<String, String>>();

        Workbook workbook = ExcelUtils.openWorkbook(Files.asByteSource(
                new File("data/seeding/SNI-category-mappings.xls")).openStream());

        Sheet sheet = workbook.getSheet(0);

        int rows = sheet.getRows();

        for (int i = 6; i < rows; i++) {
            if (sheet.getCell(8, i).getContents().equals("null") || sheet.getCell(8, i).getContents().equals("")) {
                continue;
            }
            String categoryCode = sheet.getCell(8, i).getContents();
            String categoryId = categoryMappings.get(categoryCode);
            if (categoryId == null) {
                log.error("No category found for " + categoryCode);
                continue;
            }
            String sniCode = sheet.getCell(9, i).getContents();
            if (sniCode == null || sniCode.length() == 0) {
                log.error("No SNI code found for " + categoryCode);
                continue;
            }
            industryMappings.add(new KVPair<String, String>(sniCode, categoryId));
        }

        // Sort the industry mappings so that the ones with most significant
        // specificity comes first (length of SNI-code numbers).

        Collections.sort(industryMappings,
                (mapping1, mapping2) -> mapping2.getKey().length() - mapping1.getKey().length());

        workbook.close();

        // Load the merchants data-set.

        log.info("Seeding merchants...");

        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.asByteSource(
                new File("data/seeding/merchants.txt")).openStream(), Charsets.UTF_8));

        int count = 0;
        String line = null;

        reader.readLine(); // Skip header.

        ArrayList<Merchant> merchants = new ArrayList<Merchant>();

        while ((line = reader.readLine()) != null) {
            String[] data = line.split("\t");

            String industryCode = data[5];
            String categoryId = null;

            for (KVPair<String, String> industryMapping : industryMappings) {
                if (industryCode.startsWith(industryMapping.getKey())) {
                    categoryId = industryMapping.getValue();
                    break;
                }
            }

            if (categoryId == null) {
                continue;
            }

            Merchant merchant = new Merchant();

            merchant.setOrganizationId(data[0]);
            merchant.setName(StringUtils.formatHuman(data[1]));
            merchant.setFormattedAddress(
                    StringUtils.formatHuman(data[2] + ", " + data[3]) + " " + StringUtils.formatCity(data[4]));
            merchant.setCategoryId(categoryId);
            merchant.setSniCode(industryCode);
            merchant.setCreated(new Date());
            merchant.setSource(MerchantSources.FILE);

            merchants.add(merchant);

            count++;
            if (count % 5000 == 0) {
                log.info("Have seeded " + count + " merchants.");
            }

        }

        merchantRepo.save(merchants);
        log.info("Inserted " + count + " merchants.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        try {
            transactionProcessorContext = new TransactionProcessorContext(
                    null,
                    serviceContext.getDao(ProviderDao.class).getProvidersByName(),
                    null
            );

            seedMerchants(serviceContext, serviceContext.getSearchClient());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Merchant> lookupMerchants(String query) throws Exception {
        List<Merchant> merchants = Lists.newArrayList();

        StringBuilder urlBuilder = new StringBuilder(PLACES_QUERY_URL);
        urlBuilder.append("&key=" + PLACES_API_KEY);
        urlBuilder.append("&query=" + URLEncoder.encode(query + ", Sweden", "utf8"));

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
        JSONArray resultsJsonArray = jsonObject.getJSONArray("results");

        for (int i = 0; i < resultsJsonArray.length(); i++) {
            Merchant merchant = new Merchant();

            merchant.setReference(resultsJsonArray.getJSONObject(i).getString("reference"));
            merchant.setName(resultsJsonArray.getJSONObject(i).getString("name"));
            merchant.setAddress(resultsJsonArray.getJSONObject(i).getString("formatted_address"));

            try {
                JSONArray typesJsonArray = resultsJsonArray.getJSONObject(i).getJSONArray("types");
                List<String> types = Lists.newArrayList();

                for (int j = 0; j < typesJsonArray.length(); j++) {
                    types.add(typesJsonArray.getString(j));
                }

                merchant.setTypes(types);

                merchants.add(merchant);
            } catch (JSONException e) {
                // NOOP. Could not find types.
            }
        }
        return merchants;
    }
}
