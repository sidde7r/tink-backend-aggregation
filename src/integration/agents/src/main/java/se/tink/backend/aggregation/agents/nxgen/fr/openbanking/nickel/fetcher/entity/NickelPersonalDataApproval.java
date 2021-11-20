package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelPersonalDataApproval {

    private Boolean advertising;
    private Boolean communication;
    private Boolean partners;
    private Boolean survey;
}
