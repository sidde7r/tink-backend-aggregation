package se.tink.backend.rpc;

import io.protostuff.Tag;

public class HtmlHeadResponse {

    @Tag(1)
    private String css;
    @Tag(2)
    private String metaData;
    @Tag(3)
    private String scripts;

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getScripts() {
        return scripts;
    }

    public void setScripts(String scripts) {
        this.scripts = scripts;
    }
}
