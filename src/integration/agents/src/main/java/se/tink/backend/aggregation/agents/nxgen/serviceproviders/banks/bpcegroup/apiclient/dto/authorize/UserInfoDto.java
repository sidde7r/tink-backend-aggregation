package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Data;

@Data
public class UserInfoDto {

    private String cdetab;

    private String authMethod;

    private String authLevel;
}
