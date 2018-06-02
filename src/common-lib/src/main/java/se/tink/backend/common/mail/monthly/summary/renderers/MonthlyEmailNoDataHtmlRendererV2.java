package se.tink.backend.common.mail.monthly.summary.renderers;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import se.tink.backend.common.mail.monthly.summary.model.EmptyEmailContent;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.common.template.Template;
import se.tink.libraries.i18n.Catalog;

public class MonthlyEmailNoDataHtmlRendererV2 {

    public String renderEmail(PooledRythmProxy pooledRythmProxy, EmptyEmailContent input) {

        Preconditions.checkNotNull(input);

        final Catalog catalog = Catalog.getCatalog(input.getLocale());

        Map<String, Object> context = new HashMap<>();

        addHeadings(context, input, catalog);
        addMisc(context, input, catalog);
        addFooter(context, input, catalog);

        context.put("cdn", "https://cdn.tink.se/email-assets/monthly-summary");

        return pooledRythmProxy.render(Template.MONTHLY_SUMMARY_TEMPLATE_NO_DATA_INLINED_HTML_V2, context);
    }

    private void addMisc(Map<String, Object> context, EmptyEmailContent input, Catalog catalog) {
        context.put("userId", input.getUserId());
        context.put("backgroundImage", input.isAndroidUser() ? "phone-android.png" : "phone-iphone.png");
        context.put("openButtonCaption", catalog.getString("Open Tink"));
    }

    private void addHeadings(Map<String, Object> context, EmptyEmailContent input, Catalog catalog) {

        Locale locale = Catalog.getLocale(input.getLocale());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", locale);

        String month = dateFormat.format(input.getEndDate());

        String titleFormat = catalog.getString("You have a new monthly summary for %s");

        context.put("title", String.format(titleFormat, month));
        context.put("subTitle", catalog.getString("Update your bank connections in Tink to view it!"));
    }

    private void addFooter(Map<String, Object> context, EmptyEmailContent input, Catalog catalog) {

        String link = String.format("https://app.tink.se/subscriptions/%s?locale=%s",
                input.getUnsubscribeToken(), Catalog.getLocale(input.getLocale()).getLanguage());

        String linkFormat = catalog.getString(
                "If you no longer want to receive these emails, please click <a href=\"%s\">here</a>.");

        context.put("footer", String.format(linkFormat, link));
    }

}
