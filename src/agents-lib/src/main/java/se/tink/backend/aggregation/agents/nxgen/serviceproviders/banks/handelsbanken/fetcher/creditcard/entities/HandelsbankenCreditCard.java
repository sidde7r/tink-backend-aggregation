package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class HandelsbankenCreditCard extends BaseResponse {
    @JsonIgnore
    public abstract URL getCardTransactionsUrl();

    public abstract boolean is(Account account);
}
