package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;

    public List<BookedEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList());
    }
}
