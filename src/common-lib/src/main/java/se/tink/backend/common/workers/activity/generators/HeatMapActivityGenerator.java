package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.location.UserLocationEstimator;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.HtmlActivityIconData;
import se.tink.backend.common.workers.activity.generators.models.ShareableDetailsHtmlActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantHeatMapActivityData;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserLocation;
import se.tink.backend.core.enums.FeatureFlags;

public class HeatMapActivityGenerator extends CustomHtmlActivtyGenerator {
    private static Ordering<String> descendingCountOrdering(final Multiset<String> multiset) {
        return new Ordering<String>() {
            @Override
            public int compare(String left, String right) {
                return Ints.compare(multiset.count(right), multiset.count(left));
            }
        };
    }

    public HeatMapActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(HeatMapActivityGenerator.class, 60, deepLinkBuilderFactory);
        minIosVersion = "1.6.2";
        minAndroidVersion = "1.8.6";
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {

        if (!context.getUser().getFlags().contains(FeatureFlags.MERCHANTIFICATION_CLUSTER)) {
            return;
        }

        final MerchantRepository merchantRepository = context.getServiceContext().getRepository(
                MerchantRepository.class);

        UserLocationEstimator locationEstimator = new UserLocationEstimator();

        final List<String> mapCategories = Lists.newArrayList(Iterables.transform(
                Iterables.filter(context.getCategories(),
                        c -> context.getCategoryConfiguration().getHeatmapActivityCodes().contains(c.getCode())),
                Category::getId));

        // Find transactions with merchant and correct category.

        final ImmutableListMultimap<String, Transaction> transactionsByMerchantId = Multimaps.index(
                Iterables.filter(context.getTransactions(), t -> t.getMerchantId() != null
                        && mapCategories.contains(t.getCategoryId())), Transaction::getMerchantId);

        if (transactionsByMerchantId.size() == 0) {
            return;
        }

        // Index merchants by id.

        final Map<String, Merchant> merchantsById = Maps.newHashMap();

        for (String merchantId : transactionsByMerchantId.keySet()) {
            Merchant merchant = merchantRepository.findOne(merchantId);
            if (merchant != null) {
                merchantsById.put(merchantId, merchant);
            }
        }

        // Find focus on map based on merchant cities.

        UserLocation mapFocus = findMapFocus(locationEstimator, transactionsByMerchantId, merchantsById);

        // If no merchants have city, don't show any map.

        if (mapFocus == null) {
            return;
        }

        // Loop transactions and find merchant. Also find the latest transaction location modification date
        // to use as activity date.

        Date latestLocationModificationDate = null;

        List<MerchantPoint> merchantPoints = Lists.newArrayList();

        for (String merchantId : transactionsByMerchantId.keySet()) {

            Merchant merchant = merchantsById.get(merchantId);

            if (merchant == null || merchant.getCoordinates() == null) {
                continue;
            }

            double sumTransactionAmount = 0;

            for (Transaction t : transactionsByMerchantId.get(merchantId)) {
                if (t.getAmount() < 0) {
                    sumTransactionAmount += t.getAmount();
                }

                if (t.isUserModifiedLocation()) {
                    if (latestLocationModificationDate == null) {
                        latestLocationModificationDate = t.getLastModified();
                    } else {
                        if (t.getLastModified().after(latestLocationModificationDate)) {
                            latestLocationModificationDate = t.getLastModified();
                        }
                    }
                }
            }

            MerchantPoint point = new MerchantPoint(merchant.getCoordinates().getLatitude(), merchant.getCoordinates()
                    .getLongitude(), transactionsByMerchantId.get(merchantId).size(), sumTransactionAmount);
            merchantPoints.add(point);
        }

        if (merchantPoints.size() == 0 || latestLocationModificationDate == null) {
            return;
        }

        // Create HTML activity with reference to MapBox

        StringBuffer buffer = new StringBuffer();

        // Create data object on format:

        for (int i = 0; i < merchantPoints.size(); i++) {
            MerchantPoint point = merchantPoints.get(i);
            buffer.append("{lat: " + point.getUserLocation().getLatitude() + ", lon: "
                    + point.getUserLocation().getLongitude() + ", value: " + point.getAmountSum() + "}");
            if (i != (merchantPoints.size() - 1)) {
                buffer.append(",");
            }
        }

        context.addActivities(createActivities(context.getUser().getId(), "eating-out",
                context.getCatalog().getString("Show map"), context.getCatalog().getString("Eating out map"),
                latestLocationModificationDate, context.getCatalog().getString("This is where you eat out."),
                buffer.toString(), mapFocus));
    }

    private List<Activity> createActivities(String userId, String trackingName, String buttonLabel, String title,
            Date date, String description, String arrayData, UserLocation mapFocus) {

        List<Activity> activities = Lists.newLinkedList();

        // TODO: Fix the layout issues.

        if (false) {
            activities.add(createNativeActivity(userId, trackingName, buttonLabel, title, date,
                    description, arrayData, mapFocus));
        }

        activities.add(createHTMLActivity(userId, trackingName, buttonLabel, title, date,
                description, arrayData, mapFocus));

        return activities;

    }

    private Activity createNativeActivity(String userId, String trackingName, String buttonLabel, String title, Date date,
            String description, String arrayData, UserLocation mapFocus) {

        MerchantHeatMapActivityData activityData = new MerchantHeatMapActivityData();
        activityData.setButtonLabel(buttonLabel);
        activityData.setTitle(title);
        activityData.setDescription(description);
        activityData.setMapArrayData(arrayData);
        activityData.setMapFocusCoordinates(mapFocus.getLatitude() + ", " + mapFocus.getLongitude());

        char icon = TinkIconUtils.getV1CategoryIcon(SECategories.Codes.EXPENSES_FOOD);
        activityData.setCategoryIcon(icon);

        return createActivity(userId, date, Activity.Types.MERCHANT_HEAT_MAP, null, null, activityData);
    }

    private Activity createHTMLActivity(String userId, String trackingName, String buttonLabel, String title, Date date,
            String description, String arrayData, UserLocation mapFocus) {

        char icon = TinkIconUtils
                    .getV1CategoryIcon(SECategories.Codes.EXPENSES_FOOD);

        String foodIcon = StringEscapeUtils.escapeHtml(Character.toString(icon));

        String mapFocusCoordinates = mapFocus.getLatitude() + ", " + mapFocus.getLongitude();
        ShareableDetailsHtmlActivityData data = new ShareableDetailsHtmlActivityData();

        String feedHead = TemplateUtils.getTemplate("data/templates/maps/head-feed.html");
        String detailsHead = TemplateUtils.getTemplate("data/templates/maps/head-details.html");
        String feedBody = String.format(TemplateUtils.getTemplate("data/templates/maps/feed.html"), foodIcon, title,
                description,
                arrayData, mapFocusCoordinates);

        String detailsBody = String.format(TemplateUtils.getTemplate("data/templates/maps/details.html"), foodIcon,
                title, description,
                arrayData, mapFocusCoordinates);

        data.setTrackingName(String.format("Maps (%s)", trackingName));
        data.setIcon(new HtmlActivityIconData("info", "pink"));
        data.setActivityHtml(createHtml(feedHead, feedBody));
        data.setDetailsHtml(createHtml(detailsHead, detailsBody));
        data.setButtonLabel(buttonLabel);

        return createActivity(userId, date, "html/shareable-details", null, null, data);
    }

    /**
     * Returns where the map should be focused on, from the cities from the merchants.
     */
    private UserLocation findMapFocus(UserLocationEstimator locationEstimator,
            final ImmutableListMultimap<String, Transaction> transactionsByMerchantId,
            final Map<String, Merchant> merchantsById) {

        ImmutableListMultimap<String, String> transactionsByCity = Multimaps.index(transactionsByMerchantId.keySet(),
                new Function<String, String>() {
                    @Override
                    @Nullable
                    public String apply(String merchantId) {
                        if (merchantsById.get(merchantId) == null) {
                            return "none";
                        }
                        if (merchantsById.get(merchantId).getCity() == null) {
                            return "none";
                        }
                        return merchantsById.get(merchantId).getCity().trim();
                    }
                });

        // Sort multimap on descending keys.size

        ImmutableMultimap<String, String> sortedMerchantIdsByCity = ImmutableMultimap.<String, String>builder()
                .orderKeysBy(descendingCountOrdering(transactionsByCity.keys())).putAll(transactionsByCity).build();

        String mapFocusCity = null;

        for (String city : sortedMerchantIdsByCity.keySet()) {
            if (Objects.equals("none", city)) {
                continue;
            }

            mapFocusCity = city;
            break;
        }

        if (mapFocusCity == null) {
            return null;
        }

        // Find the midpoint of the merchants with the focus city.

        List<UserLocation> locations = Lists.newArrayList();
        List<Double> weights = Lists.newArrayList();

        for (String merchantId : transactionsByMerchantId.keySet()) {
            Merchant merchant = merchantsById.get(merchantId);

            if (merchant == null || merchant.getCoordinates() == null || merchant.getCity() == null
                    || !Objects.equals(merchant.getCity().trim(), mapFocusCity)) {
                continue;
            }

            UserLocation location = new UserLocation();
            location.setLatitude(merchant.getCoordinates().getLatitude());
            location.setLongitude(merchant.getCoordinates().getLongitude());
            locations.add(location);

            weights.add(1D);
        }

        if (locations.size() == 0) {
            return null;
        }

        return locationEstimator.getWeightedGeographicalMidpoint(locations, weights);
    }

    private class MerchantPoint {
        private final UserLocation userLocation;
        private final double amountSum;

        public MerchantPoint(Double latitude, Double longitude, int size, double amountSum) {
            this.userLocation = new UserLocation();
            userLocation.setLatitude(latitude);
            userLocation.setLongitude(longitude);
            this.amountSum = Math.abs(amountSum);
        }

        public UserLocation getUserLocation() {
            return userLocation;
        }

        public double getAmountSum() {
            return amountSum;
        }

        @Override
        public String toString() {
            return "[" + userLocation.getLatitude() + ", " + userLocation.getLongitude() + "]";
        }
    }

    /**
     * Since we are constructing both native (new) and html (old), only generate notification for native.
     */
    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        if (!Objects.equals(activity.getType(), Activity.Types.MERCHANT_HEAT_MAP)) {
            return Lists.newArrayList();
        }

        return super.generateNotifications(activity, context);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
