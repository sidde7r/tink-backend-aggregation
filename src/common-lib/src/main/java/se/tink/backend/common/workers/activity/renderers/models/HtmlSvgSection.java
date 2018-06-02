package se.tink.backend.common.workers.activity.renderers.models;

public class HtmlSvgSection {
    private String deeplink;
    private String svg;

    public String getDeeplinkMethodName()
    {
        return deeplink.replaceAll(":|/|\\?|\\=|\\s|\\-|\\&", "");
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }
}