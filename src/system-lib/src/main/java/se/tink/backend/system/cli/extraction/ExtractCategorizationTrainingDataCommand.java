package se.tink.backend.system.cli.extraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class ExtractCategorizationTrainingDataCommand extends ServiceContextCommand<ServiceConfiguration> {
    public ExtractCategorizationTrainingDataCommand() {
        super("extract-categorization-training-data", "Extracts training data for categorization challenge");
    }

    private static Splitter SPLITTER = Splitter.on("\t");
    private static LogUtils log = new LogUtils(ExtractCategorizationTrainingDataCommand.class);
    private int minNumberOfUniqueUsers;
    private int maxNumberUniqueDescriptions;
    private Map<String, Category> categories = Maps.newHashMap();

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        log.info("Starting extract training data...");

        String outputFile = System.getProperty("outputFile");
        String inputFile = System.getProperty("inputFile");
        minNumberOfUniqueUsers = Integer.getInteger("minNumberOfUniqueUsers", 5);
        maxNumberUniqueDescriptions = Integer.getInteger("maxNumberUniqueDescriptions", 100000);

        if (outputFile == null || inputFile == null) {
            log.error("Missing input or output file");
            System.exit(0);
        }

        CategoryRepository categoryRepository = serviceContext.getRepository(CategoryRepository.class);

        for (Category c : categoryRepository.findAll()) {
            categories.put(c.getId(), c);
        }

        File input = new File(inputFile);
        File output = new File(outputFile);
        FileOutputStream outputStream = new FileOutputStream(output);
        Writer writer = new OutputStreamWriter(new GZIPOutputStream(outputStream), "UTF-8");

        writer.write("userId\tdate\tdescription\tamount\tcategory\n");

        TransactionLineProcessor lineProcessor = new TransactionLineProcessor(writer, categories);

        String result = Files.readLines(input, Charsets.UTF_8, lineProcessor);

        log.info(result);

        writer.close();
        outputStream.close();
    }

    private class TransactionLineProcessor implements LineProcessor<String> {
        private int uniqueDescriptionCount = 0;
        private int count = 0;
        private Map<String, CategorizationData> descMap = Maps.newHashMap();
        private ObjectMapper mapper = new ObjectMapper();
        private Writer writer;
        private Map<String, Category> categories;

        private Map<String, CategorizationData> result = Maps.newHashMap();

        public TransactionLineProcessor(Writer writer, Map<String, Category> categories) {
            this.writer = writer;
            this.categories = categories;
        }

        @Override
        public boolean processLine(String s) throws IOException {
            if (uniqueDescriptionCount < maxNumberUniqueDescriptions) {

                if (s.startsWith("userId")) {
                    return true;
                }

                Iterable<String> data = SPLITTER.split(s);

                if (Iterables.size(data) != 10) {
                    log.info("Invalid data: " + s);
                    return true;
                }

                String userId;
                String date;
                String desc;
                String amount;
                String categoryId;

                try {
                    userId = Iterables.get(data, 0);
                    date = Iterables.get(data, 4);
                    desc = Iterables.get(data, 5).toUpperCase();
                    amount = Iterables.get(data, 7);
                    categoryId = Iterables.get(data, 8);

                } catch (Exception e) {
                    log.error("Could not parse transactions", e);
                    return true;
                }

                Category category = categories.get(categoryId);

                if (category == null || Objects.equals(category, SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED) ||
                        category.getType() == CategoryTypes.TRANSFERS) {
                    return true;
                }

                count++;

                if (count % 10000 == 0) {
                    log.info("Have done " + count + " lines, unique description count is " + uniqueDescriptionCount);
                }

                if (descMap.containsKey(desc)) {
                    CategorizationData entry = descMap.get(desc);
                    entry.addTransaction(userId, desc, categoryId, amount, date);

                    if (entry.isPrintable()) {
                        uniqueDescriptionCount++;
                        result.put(desc, entry);
                    }
                } else {
                    CategorizationData entry = new CategorizationData(desc);
                    entry.addTransaction(userId, desc, categoryId, amount, date);

                    descMap.put(desc, entry);
                }
                return true;
            }
            return false;
        }

        @Override
        public String getResult() {
            int count = 0;
            try {
                for (CategorizationData entry : result.values()) {
                    for (Trans t : entry.getTransactions()) {
                        this.writer.append(String
                                .format("%s\t%s\t%s\t%s\t%s\n",
                                        t.getUserId(),
                                        t.getDate(),
                                        t.getDesc(),
                                        t.getAmount(),
                                        categories.get(t.getCategory())));
                        count++;
                    }

                    writer.flush();
                }
            } catch (IOException e) {
                log.error("Could not write results", e);
            }

            return "Done writing " + count + " lines with " + result.values().size() + " unique descriptions";
        }
    }

    private class CategorizationData {
        private String desc;
        private Set<String> categoryIds;
        private Set<String> userIds;
        private List<Trans> transactions = Lists.newArrayList();
        private boolean printed;

        public CategorizationData(String description) {
            this.desc = description;
        }

        public void addTransaction(String userId, String desc, String categoryId, String amount, String date) {
            addCategoryId(categoryId);
            addUserId(userId);

            Trans t = new Trans(userId, date, desc, amount, categoryId);

            transactions.add(t);
        }

        private void addCategoryId(String categoryId) {
            if (this.categoryIds == null) {
                this.categoryIds = Sets.newHashSet(categoryId);
            } else {
                this.categoryIds.add(categoryId);
            }
        }

        private void addUserId(String userId) {
            if (this.userIds == null) {
                this.userIds = Sets.newHashSet(userId);
            } else {
                this.userIds.add(userId);
            }
        }

        @JsonIgnore
        public boolean isPrintable() {
            boolean printable =
                    this.userIds.size() >= minNumberOfUniqueUsers && this.categoryIds.size() == 1 && !printed;

            if (printable) {
                this.printed = true;
            }

            return printable;
        }

        public Set<String> getCategoryIds() {
            return categoryIds;
        }

        public Set<String> getUserIds() {
            return userIds;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public List<Trans> getTransactions() {
            return transactions;
        }
    }

    private class Trans {
        private String userId;
        private String date;
        private String desc;
        private String amount;
        private String category;

        public Trans(String userId, String date, String desc, String amount, String category) {
            this.userId = userId;
            this.date = date;
            this.desc = desc;
            this.amount = amount;
            this.category = category;
        }

        public String getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getDate() {
            return date;
        }

        public String getDesc() {
            return desc;
        }

        public String getUserId() {
            return userId;
        }
    }
}
