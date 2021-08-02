package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
public class DecoupledAuthorizationRequest {
    @Builder.Default
    private List<String> accountList = ImmutableList.of(NordeaBaseConstants.BodyValues.ALL);

    @Builder.Default private int duration = NordeaBaseConstants.BodyValues.DURATION_MINUTES;

    @Builder.Default
    private int maxTxHistory = NordeaBaseConstants.BodyValues.FETCH_NUMBER_OF_MONTHS;

    @Builder.Default private String responseType = NordeaBaseConstants.BodyValuesSe.NORDEA_TOKEN;

    private List<String> scope;
    private String code;
}
