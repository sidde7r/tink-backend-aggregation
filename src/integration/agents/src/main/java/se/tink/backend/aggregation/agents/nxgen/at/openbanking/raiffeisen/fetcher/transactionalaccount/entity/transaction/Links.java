package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.common.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private LinkEntity account;
    private LinkEntity first;
    private LinkEntity next;
    private LinkEntity previous;
    private LinkEntity last;
}
