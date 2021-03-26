package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SparkassenHeaderValues {
    private final boolean redirect;
    private final String bankCode;
    private final String redirectUrl;
    private final String userIp;

    public SparkassenHeaderValues(String bankCode, String userIp) {
        this.redirect = false;
        this.bankCode = bankCode;
        this.userIp = userIp;
        this.redirectUrl = null;
    }

    public static SparkassenHeaderValues forRedirect(
            String bankCode, String redirectUrl, String userIp) {
        return new SparkassenHeaderValues(true, bankCode, redirectUrl, userIp);
    }

    public static SparkassenHeaderValues forEmbedded(String bankCode, String userIp) {
        return new SparkassenHeaderValues(false, bankCode, null, userIp);
    }
}
