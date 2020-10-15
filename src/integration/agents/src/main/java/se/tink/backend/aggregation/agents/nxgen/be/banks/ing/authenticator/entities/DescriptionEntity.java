package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class DescriptionEntity {

    private String language;

    @JsonInclude(Include.ALWAYS)
    private String title;

    private String summary200;
    private String summary80;
    private List<ItemEntity> items;
    private String summary;
}
