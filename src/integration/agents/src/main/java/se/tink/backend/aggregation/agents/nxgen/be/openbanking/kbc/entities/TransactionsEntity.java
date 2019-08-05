package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    @JsonProperty("booked")
    private List<BookedItemEntity> booked;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    public List<BookedItemEntity> getBooked() {
        return booked;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }
}
