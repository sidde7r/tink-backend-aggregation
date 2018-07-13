package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth;

import java.io.UnsupportedEncodingException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;

public class DnbOAuthHeaderFormatter {
    private StringBuilder content;

    public DnbOAuthHeaderFormatter() throws UnsupportedEncodingException {
        content = new StringBuilder();
        content.append(DnbConstants.OAuth.OAUTH_HEADER_PREFIX);
    }

    public void putPair(String key, String value) throws UnsupportedEncodingException {
        content.append(key + "=\"");
        content.append(value + "\", ");
    }

    @Override
    public String toString() {
        // xiacheng NOTE: remove the last ", "
        return content.substring(0, content.length() - 2);
    }
}
