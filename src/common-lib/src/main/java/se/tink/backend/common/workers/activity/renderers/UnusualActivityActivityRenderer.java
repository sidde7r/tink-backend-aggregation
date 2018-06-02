package se.tink.backend.common.workers.activity.renderers;

import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.Versions;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.models.ActivityHeader;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.KVPairBarChart;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.UnusualCategoryActivityData;
import se.tink.libraries.date.DateUtils;

public class UnusualActivityActivityRenderer extends BaseActivityRenderer {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public UnusualActivityActivityRenderer(ActivityRendererContext context, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(context);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public String renderHtml(Activity activity) {
        Catalog catalog = context.getCatalog();
        UnusualCategoryActivityData dataCollection = activity.getContent(UnusualCategoryActivityData.class);
        Ordering<KVPair<String, Double>> valueOrder = new Ordering<KVPair<String, Double>>() {
            @Override
            public int compare(KVPair<String, Double> left, KVPair<String, Double> right) {
                return left.getKey().compareTo(right.getKey());
            }
        };

        dataCollection.setData(valueOrder.sortedCopy(dataCollection.getData()));
        Category category = context.getCategory(dataCollection.getCategoryId());

        List<KVPair<String, Double>> list = dataCollection.getData();
        String svg = getSvgChart(dataCollection);
        ActivityHeader headerData = new ActivityHeader();

        String markPeriod = dataCollection.getPeriod();
        KVPair<String, Double> markPair = null;
        for (KVPair<String, Double> kvpair : list) {
            if (kvpair.getKey().equals(markPeriod)) {
                markPair = kvpair;
                break;
            }
        }
        Icon icon;
        if (v2) {
            icon = getIconSVG(Icon.IconColorTypes.WARNING, TinkIconUtils.getV2CategoryIcon(category));
        } else {
            icon = getIconSVG(Icon.IconColorTypes.WARNING, TinkIconUtils.getV1CategoryIcon(category));
        }

        headerData.setIcon(icon);
        headerData.setRightHeader(I18NUtils.formatCurrency(Math.abs(markPair.getValue()), context.getUserCurrency(), context.getLocale()));
        headerData.setLeftHeader(activity.getType().equals(Activity.Types.UNUSUAL_CATEGORY_HIGH) ? catalog
                .getString("More than usual") : catalog.getString("Less than usual"));
        headerData.setLeftSubtext(category.getDisplayName());
        headerData.setDeepLink(getDeepLink(activity, category, markPeriod));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("activity", activity);
        params.put("headerData", headerData);
		params.put("innerTemplate", "inner-bar-chart");
        params.put("svg", svg);

        return render(Template.ACTIVITIES_BASE_LAYOUT_HTML, params);
    }

    private String getDeepLink(Activity activity, Category category, String period) {
        String categoryRef;

        if (Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster())) {
            categoryRef = category.getCode();
        } else {
            categoryRef = category.getId();
        }

        return deepLinkBuilderFactory.category(categoryRef).withPeriod(period).withSource(getTrackingLabel(activity)).build();
    }

    private String getSvgChart(UnusualCategoryActivityData dataCollection) {
        Canvas c = getStandardChartAreaCanvas();

        Ordering<KVPair<String, Double>> valueOrder = new Ordering<KVPair<String, Double>>() {
            @Override
            public int compare(KVPair<String, Double> left, KVPair<String, Double> right) {
                return left.getKey().compareTo(right.getKey());
            }
        };

        dataCollection.setData(valueOrder.sortedCopy(dataCollection.getData()));
        List<KVPair<String, Double>> list = dataCollection.getData();
        String markPeriod = dataCollection.getPeriod();
        KVPair<String, Double> markPair = null;

        List<KVPair<String, Double>> positiveValuesList = new ArrayList<KVPair<String, Double>>();
        double max = 0;
        for (KVPair<String, Double> kvpair : list) {
            String period = kvpair.getKey();
            kvpair = new KVPair<String, Double>(I18NUtils.getMonthShortName(context.getCatalog(), context.getLocale(),
                    kvpair.getKey()), Math.abs(kvpair.getValue()));
            positiveValuesList.add(kvpair);
            max = Math.max(kvpair.getValue(), max);

            if (period.equals(markPeriod)) {
                markPair = kvpair;
            }
        }
        LinkedList<KVPair<String, Double>> markedPairs = new LinkedList<KVPair<String, Double>>();
        markedPairs.add(markPair);

        KVPairBarChart chart = new KVPairBarChart(context.getTheme(), context.getCatalog(), context.getUserCurrency(),
                context.getLocale(), DateUtils.getCalendar(context.getLocale()), context.getUserAgent());

        chart.setBarColor(context.getTheme().getColor(ColorTypes.EXPENSES_COMPARISON));
        chart.setMarkedBarColor(context.getTheme().getColor(ColorTypes.EXPENSES));
        chart.setLabelColor(context.getTheme().getColor(ColorTypes.CHART_AXIS_Y_LABEL));
        chart.setMarkedValues(markedPairs);
        chart.setXAxisLabelTextSize(12);
        chart.setValues(positiveValuesList);
        chart.setMaxValue(max);
        chart.setMinValue(0);
        chart.setBarMargin(16f);
        chart.setHorizontalPadding(16f);
        chart.draw(c);
        

        return c.draw();
    }
}
