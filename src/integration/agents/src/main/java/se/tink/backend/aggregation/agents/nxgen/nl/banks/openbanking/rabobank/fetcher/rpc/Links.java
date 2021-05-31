package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private String next;
    private LinkEntity nextLink;
    private LinkEntity balances;

    // Change this setter to JsonProperty for nextLink as soon as Abnamro has migrated to v.4
    @JsonSetter("next")
    public void setNext(JsonNode next) {
        if (next.isObject()) {
            this.nextLink = new ObjectMapper().convertValue(next, LinkEntity.class);
        } else {
            this.next = next.asText();
        }
    }

    public String getNextKey() {
        return Optional.ofNullable(nextLink).map(LinkEntity::getHref).orElse(next);
    }

    public boolean hasBalancesLink() {
        return this.balances != null;
    }
}
