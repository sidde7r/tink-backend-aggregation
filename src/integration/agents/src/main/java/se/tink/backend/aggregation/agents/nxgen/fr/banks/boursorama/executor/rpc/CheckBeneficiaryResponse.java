package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CheckBeneficiaryResponse {
    private String bic;
    private List<AdditionalMessageEntity> additionalMessages;
}
