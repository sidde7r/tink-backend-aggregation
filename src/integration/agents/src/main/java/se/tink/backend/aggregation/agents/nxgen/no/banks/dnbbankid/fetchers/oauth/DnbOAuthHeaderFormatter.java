package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.fetchers.oauth;

import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;

public class DnbOAuthHeaderFormatter {
    private StringBuilder content;

    public DnbOAuthHeaderFormatter() {
        content = new StringBuilder();
        content.append(DnbConstants.OAuth.OAUTH_HEADER_PREFIX);
    }

    public void putPair(String key, String value) {
        content.append(key).append("=\"");
        content.append(value).append("\", ");
    }

    @Override
    public String toString() {
        // xiacheng NOTE: remove the last ", "
        return content.substring(0, content.length() - 2);
    }
}
