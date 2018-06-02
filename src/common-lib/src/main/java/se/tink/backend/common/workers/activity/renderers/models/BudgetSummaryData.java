package se.tink.backend.common.workers.activity.renderers.models;

import com.google.common.base.Strings;

public class BudgetSummaryData {
    private String pieChart;
    private String deeplink;

    public String getPieChart() {
        return pieChart;
    }

    public void setPieChart(String pieChart) {
        this.pieChart = pieChart;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getDeeplinkMethodName() {
        if (Strings.isNullOrEmpty(deeplink)) {
            return deeplink;
        }

        return deeplink.replaceAll(":|/|\\?|\\=|\\s|\\-|\\&", "");
    }
}
