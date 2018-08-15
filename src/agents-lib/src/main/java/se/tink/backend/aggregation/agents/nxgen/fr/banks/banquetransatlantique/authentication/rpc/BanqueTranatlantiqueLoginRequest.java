package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.authentication.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginRequest;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APPLICATION_CODE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APPLICATION_CODE_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APP_VERSION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique.BanqueTransatlantiqueConstants.FORM_DATA.APP_VERSION_VALUE;

public class BanqueTranatlantiqueLoginRequest extends LoginRequest {
    public BanqueTranatlantiqueLoginRequest(String username, String password, String appVersionKey,  String appVersion, String target) {
        super(username, password, appVersionKey, appVersion, target);
        this.put(APPLICATION_CODE_KEY, APPLICATION_CODE_VALUE);
    }
}
