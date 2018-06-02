package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlHeaderEntity {
    private String version;
    private String url;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
