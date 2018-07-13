package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class HandelsbankenAccount extends BaseResponse {
    public URL toTransactions() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSACTIONS);
    }

    public abstract boolean is(Account account);
}
