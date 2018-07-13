package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.EmbeddedConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.HandelsbankenClearingNumber;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ApplicationEntryPointResponse extends BaseResponse {

    private String authToken;
    private EmbeddedConfiguration embedded;

    public URL toKeepAlive() {
        return findLink(HandelsbankenConstants.URLS.Links.KEEP_ALIVE);
    }

    public URL toAccounts() {
        return findLink(HandelsbankenConstants.URLS.Links.ACCOUNTS);
    }

    public URL toPendingTransactions() {
        return findLink(HandelsbankenConstants.URLS.Links.PENDING_TRANSACTIONS);
    }

    public URL toCardsV3() {
        return findLink(HandelsbankenConstants.URLS.Links.CARDS_V3);
    }

    public URL toCards() {
        return findLink(HandelsbankenConstants.URLS.Links.CARDS);
    }

    public URL toLoans() {
        return findLink(HandelsbankenConstants.URLS.Links.LOANS);
    }

    public URL toSecuritiesHoldings() {
        return findLink(HandelsbankenConstants.URLS.Links.SECURITIES_HOLDINGS);
    }

    public URL toTransferContext() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSFER_CONTEXT);
    }

    public URL toPaymentContext() {
        return findLink(HandelsbankenConstants.URLS.Links.PAYMENT_CONTEXT);
    }

    @JsonIgnore
    public HandelsbankenClearingNumber getClearingNumber() {
        return embedded.getClearingNumber();
    }

}
