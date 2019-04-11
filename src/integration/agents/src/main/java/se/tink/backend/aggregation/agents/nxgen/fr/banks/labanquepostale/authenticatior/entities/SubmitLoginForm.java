package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.authenticatior.entities;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class SubmitLoginForm extends AbstractForm {

    public SubmitLoginForm(String username, String password) {
        Preconditions.checkState(!Strings.isNullOrEmpty(username));
        Preconditions.checkState(!Strings.isNullOrEmpty(password));

        put(
                LaBanquePostaleConstants.QueryParams.URL_BACKEND,
                LaBanquePostaleConstants.QueryDefaultValues.MOBILE_AUTH_BACKEND);
        put(
                LaBanquePostaleConstants.QueryParams.ORIGIN,
                LaBanquePostaleConstants.QueryDefaultValues.TACTILE);
        put(
                LaBanquePostaleConstants.QueryParams.CV,
                LaBanquePostaleConstants.QueryDefaultValues.TRUE);
        put(LaBanquePostaleConstants.QueryParams.CVVS, "");
        put(LaBanquePostaleConstants.QueryParams.PASSWORD, password);
        put(LaBanquePostaleConstants.QueryParams.USERNAME, username);
    }
}
