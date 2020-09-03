package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BpceConstants {

    private static final String API_OAUTH2_BASE_PATH = "/api/oauth/v2";
    public static final String AUTHORIZE_PATH = API_OAUTH2_BASE_PATH + "/authorize";
    public static final String STEP_PATH = "/step";
    public static final String BENEFICIARIES_PATH = "/bapi/transfer/v2/transferCreditors";
}
