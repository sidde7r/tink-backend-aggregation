package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public abstract class HandelsbankenAccount extends BaseResponse {

    @JsonIgnore
    public URL getAccountTransactionsUrl() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSACTIONS);
    }

    @JsonIgnore
    public URL getCardTransactionsUrl() {
        return findLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS);
    }

    public abstract boolean is(Account account);
}
