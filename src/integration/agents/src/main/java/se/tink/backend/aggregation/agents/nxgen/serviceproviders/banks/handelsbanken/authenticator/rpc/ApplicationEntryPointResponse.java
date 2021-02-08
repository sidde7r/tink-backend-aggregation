package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.EmbeddedConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.HandelsbankenClearingNumber;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ApplicationEntryPointResponse extends BaseResponse {

    private String authToken;
    private EmbeddedConfiguration embedded;

    public URL toAccounts() {
        return findLink(HandelsbankenConstants.URLS.Links.ACCOUNTS);
    }

    public URL toCards() {
        return findLink(HandelsbankenConstants.URLS.Links.CARDS);
    }

    public URL toKeepAlive() {
        return findLink(HandelsbankenConstants.URLS.Links.KEEP_ALIVE);
    }

    public URL toLoans() {
        return findLink(HandelsbankenConstants.URLS.Links.LOANS);
    }

    public URL toPaymentContext() {
        return findLink(HandelsbankenConstants.URLS.Links.PAYMENT_CONTEXT);
    }

    public URL toPendingEInvoices() {
        return findLink(HandelsbankenConstants.URLS.Links.PENDING_EINVOICES);
    }

    public URL toPendingTransactions() {
        return findLink(HandelsbankenConstants.URLS.Links.PENDING_TRANSACTIONS);
    }

    public URL toPensionOverview() {
        return findLink(HandelsbankenConstants.URLS.Links.PENSION_OVERVIEW);
    }

    public URL toSecuritiesHoldings() {
        return findLink(HandelsbankenConstants.URLS.Links.SECURITIES_HOLDINGS);
    }

    public URL toTransferContext() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSFER_CONTEXT);
    }

    @JsonIgnore
    public HandelsbankenClearingNumber getClearingNumber() {
        return embedded.getClearingNumber();
    }

    public String getAuthToken() {
        return authToken;
    }
}
