package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class ReferredDocumentInformationEntity {
    @JsonProperty("type")
    private ReferredDocumentTypeEntity type = null;

    @JsonProperty("number")
    private String number = null;

    @JsonProperty("relatedDate")
    private LocalDate relatedDate = null;

    @JsonProperty("lineDetails")
    private List<LineDetailEntity> lineDetails = null;
}
