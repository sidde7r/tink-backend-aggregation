package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml;

import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

@XmlRootElement
public class XmlRequestEntity {
    private String name;
    private String url;
    private XmlParamListEntity params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public XmlParamListEntity getParams() {
        return params;
    }

    public void setParams(XmlParamListEntity params) {
        this.params = params;
    }

    public boolean isExecuteTransfer() {
        return nameEquals(IngConstants.RequestNames.EXECUTE_INTERNAL_TRANSFER);
    }

    private boolean nameEquals(String name) {
        return name.equalsIgnoreCase(this.name);
    }

    public URL asSSORequest() {
        return new URL(IngConstants.Urls.BASE_SSO_REQUEST + StringEscapeUtils.unescapeHtml(this.url));
    }
}
