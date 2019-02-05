package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class Saml2AcsRequest extends AbstractForm {
    public static Saml2AcsRequest create(String saml2Data) {
        Saml2AcsRequest me = new Saml2AcsRequest();
        me.put(BanquePopulaireConstants.Form.SAML_RESPONSE, saml2Data);
        return me;
    }
}
