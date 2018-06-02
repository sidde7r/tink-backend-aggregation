package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.cassandra.MerchantWizardSkippedTransactionRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.SuggestMerchantsActivityData;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.merchants.MerchantWizardSkippedTransaction;
import se.tink.backend.rpc.SuggestMerchantizeRequest;
import se.tink.backend.rpc.SuggestMerchantizeResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class SuggestMerchantsActivityGenerator extends ActivityGenerator {

    private static final LogUtils log = new LogUtils(SuggestMerchantsActivityGenerator.class);

    /**
     * Holder class to link a suggest and a transaction date.
     */
    private class SuggestAndDateHolder {
        Date date;
        SuggestMerchantizeResponse suggest;

        public SuggestAndDateHolder(SuggestMerchantizeResponse suggest, Date date) {
            this.suggest = suggest;
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public SuggestMerchantizeResponse getSuggest() {
            return suggest;
        }
    }

    /**
     * Holder of category title and message for activity.
     */
    private class CategoryInformation {
        private final String message;
        private final String titel;

        public CategoryInformation(String title, String message) {
            this.titel = title;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getTitel() {
            return titel;
        }
    }

    private static double MERCHANTIFICATION_IMPROVEMENT_THRESHOLD = 0.01;

    private static double MERCHANTIFICATION_LEVEL_THRESHOLD = 0.80;

    private Map<String, CategoryInformation> categoryInformationByCategoryCode;

    private final Ordering<SuggestAndDateHolder> suggesetOrdering = new Ordering<SuggestAndDateHolder>() {
        @Override
        public int compare(SuggestAndDateHolder left, SuggestAndDateHolder right) {
            return ComparisonChain
                    .start()
                    .compare(left.getSuggest().getMerchantificationLevel(),
                            right.getSuggest().getMerchantificationLevel())
                    .compare(right.getSuggest().getMerchantificationImprovement(),
                            left.getSuggest().getMerchantificationImprovement()).result();
        }
    };

    public SuggestMerchantsActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(SuggestMerchantsActivityGenerator.class, 70, 90, deepLinkBuilderFactory);
    }

    public void constructCategoryInformation(ActivityGeneratorContext context) {
        categoryInformationByCategoryCode = Maps.newHashMap();

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getRestaurantsCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getRestaurantsCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants and see {0}% on the eating out map.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getCoffeeCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getCoffeeCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants and see {0}% on the eating out map.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getGroceriesCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getGroceriesCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants and see {0}% on the eating out map.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getBarsCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getBarsCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants and see {0}% on the eating out map.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getAlcoholTobaccoCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale()
                                        .get(context.getCategoryConfiguration().getAlcoholTobaccoCode()).getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants and see {0}% on the eating out map.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getClothesCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getClothesCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants to reach {0}% merchantized.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getElectronicsCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale()
                                        .get(context.getCategoryConfiguration().getElectronicsCode()).getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants to reach {0}% merchantized.")));

        categoryInformationByCategoryCode
                .put(context.getCategoryConfiguration().getShoppingHobbyCode(),
                        new CategoryInformation(
                                context.getCategoriesByCodeForLocale().get(context.getCategoryConfiguration().getShoppingHobbyCode())
                                        .getDisplayName(),
                                context.getCatalog()
                                        .getString(
                                                "Pick a location for another {1} merchants to reach {0}% merchantized.")));
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        if (!context.getUser().getFlags().contains(FeatureFlags.MERCHANTIFICATION_CLUSTER)) {
            return;
        }

        // Get categoryIds that are merchantizable.

        constructCategoryInformation(context);

        ImmutableMap<String, Category> merchantizeCategoriesById = Maps.uniqueIndex(
                Iterables.filter(context.getCategories(),
                        c -> categoryInformationByCategoryCode.containsKey(c.getCode())), Category::getId);

        // Loop over 10 days transactions and create activities for relevant categories.

        Map<String, SuggestAndDateHolder> suggests = Maps.newHashMap();
        Date todayMinusTenDays = DateUtils.addDays(new Date(), -10);

        // Remove transactions that the user has skipped before
        final HashSet<String> skippedTransactions = getSkippedTransactions(context);

        log.info(context.getUser().getId(),
                String.format("Found %d transactions to skip.", skippedTransactions.size()));

        Iterable<Transaction> filteredTransaction = Iterables.filter(context.getTransactions(),
                input -> !skippedTransactions.contains(input.getId()));

        for (Transaction t : context.getTransactions()) {

            if (t.getDate().before(todayMinusTenDays)) {
                continue;
            }

            if (!merchantizeCategoriesById.containsKey(t.getCategoryId())) {
                continue;
            }

            if (suggests.containsKey(t.getCategoryId())) {
                continue;
            }

            try {
                SuggestMerchantizeRequest merchantSuggestRequest = new SuggestMerchantizeRequest();
                merchantSuggestRequest.setCategoryId(t.getCategoryId());
                merchantSuggestRequest.setNumberOfClusters(7);

                log.info(context.getUser().getId(), "Merchant suggest for category: " + t.getCategoryId());

                SuggestMerchantizeResponse merchantSuggest = context.getMerchantSearcher().suggestFromTransactions(
                        context.getUser(), merchantSuggestRequest, false, context.getMarket().getDefaultLocale(),
                        context.getMarket().getCodeAsString(), filteredTransaction);

                suggests.put(t.getCategoryId(), new SuggestAndDateHolder(merchantSuggest, t.getDate()));
            } catch (Exception e) {
                log.error(context.getUser().getId(), "Caught exception while querying merchant suggest", e);
            }
        }

        // Only show the suggest with lowest current merchantification value.

        List<SuggestAndDateHolder> topGainSuggests = suggesetOrdering.leastOf(suggests.values(), 1);

        // Evaluate merchantize responses.

        for (SuggestAndDateHolder suggestHolder : topGainSuggests) {
            if (suggestHolder.getSuggest().getMerchantificationLevel() > MERCHANTIFICATION_LEVEL_THRESHOLD
                    || suggestHolder.getSuggest().getMerchantificationImprovement()
                    < MERCHANTIFICATION_IMPROVEMENT_THRESHOLD) {
                continue;
            }

            SuggestMerchantsActivityData data = new SuggestMerchantsActivityData();
            data.setClusterCategoryId(suggestHolder.getSuggest().getClusterCategoryId());
            data.setMerchantificationImprovement(suggestHolder.getSuggest().getMerchantificationImprovement());
            data.setMerchantificationLevel(suggestHolder.getSuggest().getMerchantificationLevel());

            Category category = merchantizeCategoriesById.get(suggestHolder.getSuggest().getClusterCategoryId());
            CategoryInformation categoryInformation = categoryInformationByCategoryCode.get(category.getCode());

            int percentageLevel = (int) ((suggestHolder.getSuggest().getMerchantificationLevel()
                    + suggestHolder.getSuggest().getMerchantificationImprovement()) * 100);

            String key = getKey(category, suggestHolder);

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            suggestHolder.getDate(),
                            Activity.Types.SUGGEST_MERCHANTS,
                            categoryInformation.getTitel(),
                            Catalog.format(
                                    categoryInformation.getMessage(),
                                    percentageLevel,
                                    suggestHolder.getSuggest().getClusters().size()),
                            data,
                            null,
                            key));
        }
    }

    private HashSet<String> getSkippedTransactions(ActivityGeneratorContext context) {
        List<MerchantWizardSkippedTransaction> entities = context.getServiceContext().getRepository(MerchantWizardSkippedTransactionRepository.class).findAllByUserId(context.getUser().getId());

        return Sets.newHashSet(Iterables.transform(entities, input -> UUIDUtils.toTinkUUID(input.getTransactionId())));
    }

    private String getKey(Category category, SuggestAndDateHolder suggestHolder) {
        return StringUtils.hashAsStringSHA1("Suggest-merchant" + category.getCode()
                + suggestHolder.getSuggest().getMerchantificationLevel());
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
