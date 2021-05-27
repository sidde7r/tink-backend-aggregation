package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BankverlagHeaderValues {
    private final boolean redirect;
    private final String aspspId;
    private final String redirectUrl;
    private final String userIp;

    public BankverlagHeaderValues(String aspspId, String userIp) {
        this.redirect = false;
        this.aspspId = aspspId;
        this.userIp = userIp;
        this.redirectUrl = null;
    }

    public static BankverlagHeaderValues forRedirect(
            String bankCode, String redirectUrl, String userIp) {
        return new BankverlagHeaderValues(true, bankCode, redirectUrl, userIp);
    }

    public static BankverlagHeaderValues forEmbedded(String bankCode, String userIp) {
        return new BankverlagHeaderValues(false, bankCode, null, userIp);
    }
}
