package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class TransactionsEntity {

    private List<TransactionEntity> booked;

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    public List<TransactionEntity> getBooked() {
        return ListUtils.emptyIfNull(booked);
    }
}
