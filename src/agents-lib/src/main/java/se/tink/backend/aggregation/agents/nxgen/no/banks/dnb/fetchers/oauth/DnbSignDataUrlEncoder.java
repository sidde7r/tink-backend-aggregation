package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;

public class DnbSignDataUrlEncoder {

    private StringBuilder content;

    public DnbSignDataUrlEncoder(String method, String Url) throws UnsupportedEncodingException {
        content = new StringBuilder();
        content.append(method);
        content.append("&");
        content.append(URLEncoder.encode(Url, DnbConstants.CHARSET));
        content.append("&");
    }

    public void putPair(String key, String value) throws UnsupportedEncodingException {
        content.append(DnbOAuthEncoder.encode(key + "=" + value + "&"));
    }

    @Override
    public String toString() {
        // xiacheng NOTE: remove the last "%26"
        return content.substring(0, content.length() - 3);
    }
}
