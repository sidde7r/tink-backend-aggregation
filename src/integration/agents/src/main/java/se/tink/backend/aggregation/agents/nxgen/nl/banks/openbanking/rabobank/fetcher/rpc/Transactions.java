package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transactions {

    @JsonProperty("booked")
    private List<TransactionItem> booked;

    @JsonProperty("pending")
    private List<TransactionItem> pending;

    @JsonProperty("_links")
    private Links links;

    public Optional<List<TransactionItem>> getBooked() {
        return Optional.ofNullable(booked);
    }

    public Optional<List<TransactionItem>> getPending() {
        return Optional.ofNullable(pending);
    }

    public Links getLinks() {
        return links;
    }
}
