package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataErrorResponse {

    private TppMessagesEntity tppMessages;

    @JsonProperty("_links")
    private LinksEntity links;

    public void checkError(Throwable cause) throws PaymentException {
        if (tppMessages == null) {
            throw new IllegalStateException("Got an error without failures.", cause);
        } else {
            throw tppMessages.buildRelevantException(cause);
        }
    }
}
