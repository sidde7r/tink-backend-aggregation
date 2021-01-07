package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestsDetailsEntity {
    private List<AccruedEntity> accrued;
    private List<PostedEntity> posted;
}
