package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.PropertiesEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class SignProtocolResponse extends BelfiusResponse {
    public boolean cardReaderAllowed() {
        Widget w = ScreenUpdateResponse.widgetContains(this, BelfiusConstants.Response.CARD_READER_ALLOWED);
        return w.getProperties(PropertiesEntity.class).cardReaderAllowed();
    }

    public boolean signOk() {
        Widget widget = ScreenUpdateResponse.widgetContains(this,
                BelfiusConstants.Widget.TRANSFER_SIGN_OK);
        if (widget == null) {
            return false;
        }

        String ret = widget.getProperties(Text.class).getText();
        return ret.equals("Y");
    }

    public String getSignType() {
        List<Widget> widgets = ScreenUpdateResponse.widgetsContains(this,
                BelfiusConstants.Response.REUSE_SIGNATURE);

        return widgets.stream()
                .filter(widget ->
                        widget.getProperties(PropertiesEntity.class).getSignType().length() > 0)
                .map(widget -> widget.getProperties(PropertiesEntity.class).getSignType())
                .findFirst()
                .orElse("");
    }

    public String getChallenge() {
        return ScreenUpdateResponse.widgetContains(this,
                BelfiusConstants.Response.RESPONSE_CHALLENGE)
                .getProperties(Text.class).getText();
    }

    public boolean signError() {
        return MessageResponse.transferSignFailed(this);
    }

    public boolean isError() {
        return MessageResponse.isError(this);
    }

    public boolean signTempError() {
        return MessageResponse.transferSignTempError(this);
    }

    public boolean weeklyCardLimitReached() {
        return MessageResponse.weeklyCardLimitCode(this);
    }

    public boolean invalidBeneficiarySign() {
        return MessageResponse.invalidBeneficiarySign(this);
    }

    public boolean requireSignWeeklyLimit() {
        return MessageResponse.requireSignOfWeeklyLimit(this);
    }

    public String getErrorMessage() {
        return MessageResponse.getErrorMessage(this);
    }

    public boolean requireSignDailyLimit() {
        return MessageResponse.requireSignOfDailyLimit(this);
    }
}
