package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.generators.models.ApplicationResumeData;
import se.tink.backend.core.Activity;
import se.tink.libraries.application.ApplicationType;

public class ApplicationActivityRenderer extends BaseActivityRenderer {
    private final String S3_PATH = "https://s3-eu-west-1.amazonaws.com/tink-web/activities/applications/";
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public ApplicationActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        String html = null;
        if (activity.getKey().contains(ApplicationType.OPEN_SAVINGS_ACCOUNT.toString())) {
            html = getHtmlForOpenSavingsAccount(activity);
        } else if (activity.getKey().contains(ApplicationType.SWITCH_MORTGAGE_PROVIDER.toString())) {
            html = getHtmlForSwitchMortgageProvider(activity);
        } else if (Objects.equals(Activity.Types.APPLICATION_RESUME_MORTGAGE, activity.getType())) {
            html = getHtmlForResumeSwitchMortgageProvider(activity);
        } else if (Objects.equals(Activity.Types.APPLICATION_RESUME_SAVINGS, activity.getType())) {
            html = getHtmlForResumeOpenSavingsAccount(activity);
        }

        return html;
    }

    private String getHtmlForOpenSavingsAccount(Activity activity) {
        Catalog catalog = context.getCatalog();

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", getDeepLinkForOpenSavingsAccount());
        params.put("title", activity.getTitle());
        params.put("description", activity.getMessage());
        params.put("buttonLabel", catalog.getString("Show alternatives"));
        params.put("items", Lists.newArrayList(
                catalog.getString("Choose a savings account you like"),
                catalog.getString("Answer a few simple questions"),
                catalog.getString("The account is opened")
        ));

        return render(Template.ACTIVITIES_OPEN_SAVINGS_ACCOUNT_HTML, params);
    }

    private String getHtmlForResumeOpenSavingsAccount(Activity activity) {
        ApplicationResumeData data = activity.getContent(ApplicationResumeData.class);

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", deepLinkBuilderFactory.application(data.getApplicationId()).build());
        params.put("title", activity.getTitle());
        params.put("message", activity.getMessage());
        // TODO: Change this image when we get something from design.
        params.put("imageUrl", S3_PATH  + "application-resume-mortgage.gif");

        return render(Template.ACTIVITIES_APPLICATION_RESUME_HTML, params);
    }

    private String getHtmlForSwitchMortgageProvider(Activity activity) {
        Catalog catalog = context.getCatalog();

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", getDeepLinkForSwitchMortgageProvider());
        params.put("title", activity.getTitle());
        params.put("description", activity.getMessage());
        params.put("buttonLabel", catalog.getString("Start wizard"));
        params.put("items", Lists.newArrayList(
                catalog.getString("Answer 4 simple questions"),
                catalog.getString("See what interest you can get"),
                catalog.getString("Answer questions and submit application")
        ));

        return render(Template.ACTIVITIES_SWITCH_MORTGAGE_PROVIDER_HTML, params);
    }

    private String getHtmlForResumeSwitchMortgageProvider(Activity activity) {
        ApplicationResumeData data = activity.getContent(ApplicationResumeData.class);

        Map<String, Object> params = Maps.newHashMap();
        params.put("activity", activity);
        params.put("deeplink", deepLinkBuilderFactory.application(data.getApplicationId()).build());
        params.put("title", activity.getTitle());
        params.put("message", activity.getMessage());
        if (v2) {
            params.put("imageUrl", S3_PATH + "application-resume-mortgage-v2.gif");
        } else {
            params.put("imageUrl", S3_PATH + "application-resume-mortgage.gif");
        }

        return render(Template.ACTIVITIES_APPLICATION_RESUME_HTML, params);
    }

    private String getDeepLinkForOpenSavingsAccount() {
        return deepLinkBuilderFactory.createApplication(ApplicationType.OPEN_SAVINGS_ACCOUNT.toString()).build();
    }

    private String getDeepLinkForSwitchMortgageProvider() {
        return deepLinkBuilderFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER.toString()).build();
    }
}
