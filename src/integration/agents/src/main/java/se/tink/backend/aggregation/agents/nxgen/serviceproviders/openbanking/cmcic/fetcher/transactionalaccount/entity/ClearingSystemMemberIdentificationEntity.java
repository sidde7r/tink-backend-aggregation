package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class ClearingSystemMemberIdentificationEntity {
    @JsonProperty("clearingSystemId")
    private String clearingSystemId = null;

    @JsonProperty("memberId")
    private String memberId = null;
}
