package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.authentication.rpc;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APPLICATION_CODE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APPLICATION_CODE_VALUE;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginRequest;

public class BanqueTranatlantiqueLoginRequest extends LoginRequest {
    public BanqueTranatlantiqueLoginRequest(
            String username,
            String password,
            String appVersionKey,
            String appVersion,
            String target) {
        super(username, password, appVersionKey, appVersion, target);
        this.put(APPLICATION_CODE_KEY, APPLICATION_CODE_VALUE);
    }
}
