package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class RemittanceInformationEntity {
    @JsonProperty("unstructured")
    private List<String> unstructured = null;

    @JsonProperty("structured")
    private List<StructuredRemittanceInformationEntity> structured = null;
}
