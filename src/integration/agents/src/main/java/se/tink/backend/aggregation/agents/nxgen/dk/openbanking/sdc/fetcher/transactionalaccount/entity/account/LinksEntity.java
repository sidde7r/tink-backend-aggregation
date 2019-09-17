package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.common.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkEntity balances;
    private LinkEntity transactions;
}
