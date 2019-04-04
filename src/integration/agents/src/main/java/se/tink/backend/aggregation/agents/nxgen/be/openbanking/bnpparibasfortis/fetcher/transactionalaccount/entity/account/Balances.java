
package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;


@JsonObject
public class Balances {

    private String href;

    public String getHref() {
        return href;
    }
}
