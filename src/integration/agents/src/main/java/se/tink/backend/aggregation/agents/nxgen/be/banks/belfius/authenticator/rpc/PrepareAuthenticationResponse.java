package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Valid;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareAuthenticationResponse extends BelfiusResponse {

    public void validate() throws AuthenticationException {
        Widget widget =
                ScreenUpdateResponse.findWidget(this, BelfiusConstants.Widget.PAN).orElse(null);

        if (widget == null) {
            return;
        }

        boolean isValid = widget.getProperties(Valid.class).isValid();

        if (!isValid) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    public boolean isCredentialsOk() {
        Widget widget =
                ScreenUpdateResponse.findWidget(this, BelfiusConstants.Widget.PAN).orElse(null);

        if (widget == null) {
            return true;
        }

        return widget.getProperties(Valid.class).isValid();
    }

    public String getChallenge() {
        return ScreenUpdateResponse.findWidgetOrElseThrow(
                        this, BelfiusConstants.Widget.IWS_LOGIN_SIGNATURE_CHALLENGE)
                .getProperties(Text.class)
                .getText();
    }
}
