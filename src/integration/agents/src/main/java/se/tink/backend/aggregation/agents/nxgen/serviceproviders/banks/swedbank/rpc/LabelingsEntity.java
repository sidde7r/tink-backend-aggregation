package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LabelingsEntity {
    private LinksEntity links;
    private List<LabelEntity> labels;

    public LinksEntity getLinks() {
        return links;
    }

    public List<LabelEntity> getLabels() {
        return labels;
    }
}
