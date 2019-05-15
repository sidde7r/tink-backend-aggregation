package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.transaction;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account.BookedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transactions {

    private List<BookedEntity> booked;
    private TransactionLinksEntity links;

    public List<BookedEntity> getBooked() {
        return Optional.ofNullable(booked).orElseGet(Lists::emptyList);
    }
}
