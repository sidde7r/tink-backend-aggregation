package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.ActivityRendererContext;
import se.tink.backend.common.workers.activity.renderers.BaseActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.core.Activity;
import se.tink.backend.rpc.SuggestTransactionsResponse;

public class SuggestCategoryActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public SuggestCategoryActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Map<String, Object> params = new HashMap<>();
        params.put("activity", activity);

        ActivityHeader headerData = new ActivityHeader();
        char icon;
        String color;
        if (v2) {
            icon = TinkIconUtils.IconsV2.FINANCIAL;
            color = Icon.IconColorTypes.TINK_ORANGE;
        } else {
            icon = TinkIconUtils.Icons.INFO;
            color = Icon.IconColorTypes.INFO;
        }
        Icon iconSvg = getIconSVG(color, icon);
        headerData.setIcon(iconSvg);

        SuggestTransactionsResponse suggestTransactions = activity.getContent(SuggestTransactionsResponse.class);

        String svg = getSvgChart(suggestTransactions);

        Catalog catalog = context.getCatalog();
        headerData.setLeftHeader(catalog.getString("Improve categorization"));

        int clusterCount = suggestTransactions.getClusters().size();

        String reachableLevel = formatAsPercentageString(suggestTransactions.getCategorizationLevel() +
                suggestTransactions.getCategorizationImprovement(), false);

        String improvementTextFormat = catalog.getPluralString(
                "You can reach {0}% by categorizing one additional transaction.",
                "You can reach {0}% by categorizing {1} additional transactions.",
                clusterCount);

        String improvementText = Catalog.format(improvementTextFormat, reachableLevel, clusterCount);

        headerData.setLeftSubtext(improvementText);
        headerData.setDeepLink(getDeepLink(activity));

        params.put("svg", svg);
        params.put("headerData", headerData);
        params.put("innerTemplate", "suggest-category");
        params.put("activityClass", "suggest-category");

        return render(Template.ACTIVITIES_BASE_LEFT_ONLY_LAYOUT_HTML, params);
    }

    private String getDeepLink(Activity activity) {
        return deepLinkBuilderFactory.suggestCategory().withSource(getTrackingLabel(activity)).build();
    }

    private String getSvgChart(SuggestTransactionsResponse suggestTransactions) {
        Canvas canvas = new Canvas(getSvgInsidePaddingWidth(), 24);

        CategorizedGraph categorizedGraph = new CategorizedGraph(context.getTheme(), context.getUserAgent());

        categorizedGraph.setTextSize(12);
        categorizedGraph.setCategorizationLevel(suggestTransactions.getCategorizationLevel());
        categorizedGraph.setText(formatAsPercentageString(suggestTransactions.getCategorizationLevel(), true));

        categorizedGraph.draw(canvas);

        return canvas.draw();
    }

    /**
     * Will format numbers on the same format that they are formatted on frontend
     */
    private String formatAsPercentageString(Double value, boolean includePercentSign) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance(context.getLocale());
        percentFormat.setRoundingMode(RoundingMode.HALF_DOWN);

        String formattedValue;

        if (value < 0.98) {
            percentFormat.setMaximumFractionDigits(0);
            formattedValue = percentFormat.format(value);
        } else {
            percentFormat.setMaximumFractionDigits(1);
            formattedValue = percentFormat.format(value);
        }

        return includePercentSign ? formattedValue : formattedValue.replace("%", "");
    }
}
