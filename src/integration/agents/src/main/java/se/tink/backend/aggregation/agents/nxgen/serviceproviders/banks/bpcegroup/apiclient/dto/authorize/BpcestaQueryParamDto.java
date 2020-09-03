package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Builder
public class BpcestaQueryParamDto {

    private String cdetab;

    private String enseigne;

    private String csid;

    private String termId;

    private String typSrv;

    @Builder.Default private String typApp = "rest";

    @Builder.Default private String typAct = "sso";

    @Builder.Default private String typSp = "out-band";
}
