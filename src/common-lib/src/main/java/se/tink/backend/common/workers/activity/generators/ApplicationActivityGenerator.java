package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.ApplicationResumeData;
import se.tink.backend.common.workers.activity.generators.utils.SwitchMortgageHelper;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Application;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Functions;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationActivityGenerator extends ActivityGenerator {
    
    public ApplicationActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(ApplicationActivityGenerator.class, 100, deepLinkBuilderFactory);

        minAndroidVersion = "999.9.9"; // Disabled for Android.
        minIosVersion = "2.5.10";
    }

    private static final Predicate<Application> RESUMABLE_APPLICATION = application ->
            Objects.equals(ApplicationStatusKey.CREATED, application.getStatus().getKey())
                    || Objects.equals(ApplicationStatusKey.IN_PROGRESS, application.getStatus().getKey())
                    || Objects.equals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey())
                    || Objects.equals(ApplicationStatusKey.DISQUALIFIED, application.getStatus().getKey())
                    || Objects.equals(ApplicationStatusKey.ERROR, application.getStatus().getKey());

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        if (!FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(context.getUser().getFlags())) {
            return;
        }

        ApplicationDAO applicationDAO = context.getServiceContext().getDao(ApplicationDAO.class);
        ProductDAO productDAO = context.getServiceContext().getDao(ProductDAO.class);

        UUID userId = UUIDUtils.fromTinkUUID(context.getUser().getId());

        ListMultimap<ProductType, ProductArticle> productsByType = FluentIterable
                .from(productDAO.findAllActiveArticlesByUserId(userId))
                .index(ProductArticle::getType);

        ListMultimap<ApplicationType, Application> applicationsByType = FluentIterable
                .from(applicationDAO.findByUserId(UUIDUtils.fromTinkUUID(context.getUser().getId())))
                .index(Application::getType);

        boolean haveCreatedMortgageActivity = createSwitchMortgageActivities(context,
                productsByType.get(ProductType.MORTGAGE),
                applicationsByType.get(ApplicationType.SWITCH_MORTGAGE_PROVIDER));

        // Don't show activity for savings account if we're displaying an activity for mortgage.
        if (haveCreatedMortgageActivity) {
            return;
        }

        createOpenSavingsAccountActivities(context,
                productsByType.get(ProductType.SAVINGS_ACCOUNT),
                applicationsByType.get(ApplicationType.OPEN_SAVINGS_ACCOUNT));
    }

    private boolean createSwitchMortgageActivities(ActivityGeneratorContext context, List<ProductArticle> products,
            List<Application> applications) {

        Activity activity = null;
                
        if (!applications.isEmpty()) {
            activity = getActivityForResumingSwitchMortgageProvider(context, applications);
        } else if (!products.isEmpty()) {
            activity = getActivityForSwitchMortgageProvider(context, products);
        }

        if (activity != null) {
            context.addActivity(activity);
            return true;
        } else {
            return false;
        }
    }

    private void createOpenSavingsAccountActivities(ActivityGeneratorContext context, List<ProductArticle> products,
            List<Application> applications) {

        if (!applications.isEmpty()) {
            Activity activity = getActivityForResumingOpenSavingsAccount(context, applications);
            if (activity != null) {
                context.addActivity(activity);
            }
            return;
        }

        if (!products.isEmpty()) {
            context.addActivity(getActivityForOpenSavingsAccount(context, products));
        }
    }

    private Optional<Application> getResumableApplication(List<Application> applications) {
        List<Application> resumableApplications = FluentIterable
                .from(applications)
                .filter(RESUMABLE_APPLICATION)
                .toList();

        if (resumableApplications.isEmpty()) {
            return Optional.empty();
        }

        Application application = resumableApplications.stream().max(Orderings.APPLICATION_BY_CREATED).get();

        // Give the user ten minutes to choose a product.
        if (DateTime.now().minusMinutes(10).isAfter(application.getCreated().getTime())) {
            // Don't show resume activity if the user hasn't chosen a product yet.
            if (Functions.APPLICATION_TO_PRODUCT_INSTANCE_ID.apply(application) == null) {
                return Optional.empty();
            }
        }
        
        return Optional.of(application);
    }
    
    private Activity getActivityForResumingSwitchMortgageProvider(ActivityGeneratorContext context,
            List<Application> applications) {
        
        Optional<Application> application = getResumableApplication(applications);
        
        if (!application.isPresent()) {
            return null;
        }

        String applicationId = UUIDUtils.toTinkUUID(application.get().getId());
        String key = String.format("%s.%s", Activity.Types.APPLICATION_RESUME_MORTGAGE, applicationId);

        return createActivity(
                context.getUser().getId(),
                application.get().getCreated(),
                Activity.Types.APPLICATION_RESUME_MORTGAGE,
                "Fortsätt där du slutade",
                "Du är snart klar med att flytta ditt bolån.",
                new ApplicationResumeData(applicationId),
                key,
                StringUtils.hashAsStringSHA1(key));
    }

    private Activity getActivityForResumingOpenSavingsAccount(ActivityGeneratorContext context,
            List<Application> applications) {

        Optional<Application> application = getResumableApplication(applications);
        
        if (!application.isPresent()) {
            return null;
        }

        String applicationId = UUIDUtils.toTinkUUID(application.get().getId());
        String key = String.format("%s.%s", Activity.Types.APPLICATION_RESUME_SAVINGS, applicationId);

        return createActivity(
                context.getUser().getId(),
                application.get().getCreated(),
                Activity.Types.APPLICATION_RESUME_SAVINGS,
                "Fortsätt där du slutade",
                "Du är snart klar att börja med ditt sparande.",
                new ApplicationResumeData(applicationId),
                key,
                StringUtils.hashAsStringSHA1(key));
    }

    private Activity getActivityForOpenSavingsAccount(ActivityGeneratorContext context, List<ProductArticle> products) {
        Catalog catalog = context.getCatalog();
        Date date = getActivityDate(products.get(0));
        String key = getKey(ApplicationType.OPEN_SAVINGS_ACCOUNT, date);
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);
        String title = catalog.getString("Open a savings account in Tink");
        String description = catalog.getString("Start saving on a savings account with interest.");

        return createActivity(
                context.getUser().getId(),
                date,
                Activity.Types.APPLICATION_SAVINGS,
                title,
                description,
                null,
                key,
                feedActivityIdentifier);
    }

    private Activity getActivityForSwitchMortgageProvider(ActivityGeneratorContext context,
            List<ProductArticle> products) {

        SwitchMortgageHelper switchMortgageHelper = new SwitchMortgageHelper(context);

        // Generate the feed activity only if the user can save money by switching mortgage provider.
        if (!switchMortgageHelper.canSaveMoneyBySwitchingMortgageProvider(
                context.getAccounts(),
                context.getCredentials(),
                products)) {
            return null;
        }

        Catalog catalog = context.getCatalog();
        Date date = getActivityDate(products.get(0));
        String key = getKey(ApplicationType.SWITCH_MORTGAGE_PROVIDER, date);
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);
        String title = catalog.getString("Better interest rate on your mortgage");
        String description = catalog
                .getString("See what interest rate you can get and move your mortgage directly in Tink.");

        return createActivity(
                context.getUser().getId(),
                date,
                Activity.Types.APPLICATION_MORTGAGE,
                title,
                description,
                null,
                key,
                feedActivityIdentifier);
    }
    
    private static Date getActivityDate(ProductArticle article) {
        if (article.getValidTo() != null) {
            // On top until the expiration date.
            return article.getValidTo();
        } else if (article.getValidFrom() != null) {
            // On top until 7 days after being targeted.
            return DateUtils.addDays(article.getValidFrom(), 7);
        } else {
            // Always on top, but updated daily.
            return DateUtils.getToday();
        }
    }

    private static String getKey(ApplicationType type, Date date) {
        return String.format("%s.%s", type, ThreadSafeDateFormat.FORMATTER_DAILY.format(date));
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
