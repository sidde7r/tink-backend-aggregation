package se.tink.backend.common.workers.activity.renderers.models;

public class BiggestPurchaseData {
    private String amount;
    private String title;
    private String description;
    private String deeplink;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink)
    {
        this.deeplink = deeplink;
    }

    public String getDeeplinkMethodName()
    {
        return deeplink.replaceAll(":|/|\\?|\\=|\\s|\\-|\\&", "");
    }

}
