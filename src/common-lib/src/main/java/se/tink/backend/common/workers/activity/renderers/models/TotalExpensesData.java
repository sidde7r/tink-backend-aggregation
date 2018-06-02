package se.tink.backend.common.workers.activity.renderers.models;

public class TotalExpensesData {

    private String total;
    private String average;
    private String title;
    private String averageColor;
    private String deeplink;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAverageColor() {
        return averageColor;
    }

    public void setAverageColor(String averageColor) {
        this.averageColor = averageColor;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getDeeplinkMethodName()
    {
        return deeplink.replaceAll(":|/|\\?|\\=|\\s|\\-|\\&", "");
    }
}
