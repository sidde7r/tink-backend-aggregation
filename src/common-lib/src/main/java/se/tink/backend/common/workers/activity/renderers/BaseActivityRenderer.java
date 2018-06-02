package se.tink.backend.common.workers.activity.renderers;

import java.awt.Color;
import java.util.Map;
import se.tink.backend.common.Versions;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.tracking.Tracking;
import se.tink.backend.common.workers.activity.renderers.models.Icon;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.svg.charts.FeedIcon;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Activity;

public abstract class BaseActivityRenderer implements ActivityRenderer {
    protected ActivityRendererContext context;

    protected static final int CONTENT_PADDING = 8;
    protected boolean v2 = false;

    public BaseActivityRenderer(ActivityRendererContext context) {
        v2 = Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster());
        this.context = context;
    }

    protected Icon getIconSVG(String backgroundColor, char icon) {
        Icon icons = new Icon();
        icons.setColorType(backgroundColor);
        icons.setChar(icon);

        return icons;
    }

    protected String getIconSVG(Color backgroundColor, char icon, float radius, int iconSize) {
        Canvas canvas = new Canvas((int) (radius * 2), (int) (radius * 2));
        FeedIcon iconView = new FeedIcon();
        setupFeedIcon(iconView, backgroundColor, icon, radius, iconSize);

        iconView.draw(canvas, context.getTheme().isV2());
        return canvas.draw();
    }

    protected void setupFeedIcon(FeedIcon iconView, Color backgroundColor, char icon, float radius, int iconSize) {
        iconView.setRadius(radius);
        iconView.setIconSize(iconSize);

        iconView.setIcon(icon);
        iconView.setIconColor(Theme.Colors.WHITE);
        iconView.setBackgroundColor(backgroundColor);
    }

    protected Canvas getStandardChartAreaCanvas() {
        return getStandardChartAreaCanvas(120);
    }

    protected Canvas getStandardChartAreaCanvas(int height) {
        return new Canvas(getSvgWidth(), height);
    }

    protected Canvas getChartAreaCanvasInsidePadding(int height) {
        return new Canvas(getSvgInsidePaddingWidth(), height);
    }

    public int getSvgWidth() {
        double baseResolution = 160;

        int borderWidth = 1;

        return (int) Math.ceil((context.getScreenWidth() / (context.getScreenResolution() / baseResolution)
                - (CONTENT_PADDING + borderWidth) * 2));
    }

    protected String getTrackingLabel(Activity activity) {
        return Catalog.format("Activity: {0}", getTrackingName(activity));
    }

    protected String getTrackingName(Activity a) {
        String s;

        switch (a.getType()) {
        case Activity.Types.MONTHLY_SUMMARY:
        case Activity.Types.MONTHLY_SUMMARY_ABNAMRO:
            s = Tracking.FeedActivity.MONTHLY_SUMMARY;
            break;
        case Activity.Types.WEEKLY_SUMMARY:
            s = Tracking.FeedActivity.WEEKLY_SUMMARY;
            break;
        case Activity.Types.FRAUD:
            s = Tracking.FeedActivity.FRAUD;
            break;
        case Activity.Types.SUGGEST_MERCHANTS:
            s = Tracking.FeedActivity.MERCHANTIZATION;
            break;
        case Activity.Types.MERCHANT_HEAT_MAP:
            s = Tracking.FeedActivity.MERCHANT_HEAT_MAP;
            break;
        case Activity.Types.LOOKBACK:
            s = Tracking.FeedActivity.LOOKBACK;
            break;
        case Activity.Types.BADGE:
            s = Tracking.FeedActivity.BADGE;
            break;
        case Activity.Types.SUGGEST:
            s = Tracking.FeedActivity.SUGGEST;
            break;
        case Activity.Types.UNUSUAL_ACCOUNT:
            s = Tracking.FeedActivity.UNUSUAL_ACTIVITY_ACCOUNT;
            break;
        case Activity.Types.UNUSUAL_CATEGORY_HIGH:
            s = Tracking.FeedActivity.UNUSUAL_ACTIVITY_CATEGORY_HIGH;
            break;
        case Activity.Types.UNUSUAL_CATEGORY_LOW:
            s = Tracking.FeedActivity.UNUSUAL_ACTIVITY_CATEGORY_LOW;
            break;
        case Activity.Types.FOLLOW_EXPENSES:
            s = Tracking.FeedActivity.FOLLOW;
            break;
        case Activity.Types.LEFT_TO_SPEND:
            s = Tracking.FeedActivity.LEFT_TO_SPEND;
            break;
        case Activity.Types.TRANSFER:
            s = Tracking.FeedActivity.TRANSFER;
            break;
        case Activity.Types.TRANSACTION:
            s = Tracking.FeedActivity.TRANSACTION;
            break;
        case Activity.Types.TRANSACTION_MULTIPLE:
            s = Tracking.FeedActivity.TRANSACTION_MULTIPLE;
            break;
        case Activity.Types.INCOME_MULTIPLE:
            s = Tracking.FeedActivity.INCOME_MULTIPLE;
            break;
        case Activity.Types.INCOME:
            s = Tracking.FeedActivity.INCOME;
            break;
        case Activity.Types.DOUBLE_CHARGE:
            s = Tracking.FeedActivity.DOUBLE_CHARGE;
            break;
        case Activity.Types.LARGE_EXPENSE:
            s = Tracking.FeedActivity.LARGE_EXPENSE;
            break;
        case Activity.Types.LARGE_EXPENSE_MULTIPLE:
            s = Tracking.FeedActivity.LARGE_EXPENSE_MULTIPLE;
            break;
        case Activity.Types.BALANCE_LOW:
            s = Tracking.FeedActivity.ACCOUNT_BALANCE_LOW;
            break;
        case Activity.Types.BALANCE_HIGH:
            s = Tracking.FeedActivity.ACCOUNT_BALANCE_HIGH;
            break;
        case Activity.Types.SUGGEST_PROVIDER:
            s = Tracking.FeedActivity.SUGGEST_PROVIDER;
            break;
        case Activity.Types.DISCOVER_BUDGETS:
            s = Tracking.FeedActivity.DISCOVER_BUDGETS;
            break;
        case Activity.Types.EINVOICES:
            s = Tracking.FeedActivity.E_INVOICE;
            break;
        case Activity.Types.APPLICATION_MORTGAGE:
            s = Tracking.FeedActivity.APPLICATION_MORTGAGE;
            break;
        case Activity.Types.APPLICATION_SAVINGS:
            s = Tracking.FeedActivity.APPLICATION_SAVINGS;
            break;
        case Activity.Types.APPLICATION_RESUME_MORTGAGE:
            s = Tracking.FeedActivity.APPLICATION_RESUME_MORTGAGE;
            break;
        case Activity.Types.APPLICATION_RESUME_SAVINGS:
            s = Tracking.FeedActivity.APPLICATION_RESUME_SAVINGS;
            break;
        default:
            s = "";
        }
        return s;
    }

    public int getSvgInsidePaddingWidth() {
        return getSvgWidth() - CONTENT_PADDING * 2;
    }

    protected String render(Template template, Map<String, Object> params) {
        if (isVersion2()) {
            String v2TemplateName = template.name().concat("_V2");
            template = Template.valueOf(v2TemplateName);
        }

        return context.getTemplateRenderer().render(template, params);
    }

    private Boolean isVersion2() {
        return Versions.shouldUseNewFeed(context.getUserAgent(), context.getCluster());
    }
}
