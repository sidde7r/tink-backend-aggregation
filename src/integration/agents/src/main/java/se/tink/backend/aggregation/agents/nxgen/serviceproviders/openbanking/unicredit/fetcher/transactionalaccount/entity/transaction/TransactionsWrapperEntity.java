package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsWrapperEntity {

    private List<TransactionEntity> booked;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Lists.emptyList());
    }
}
