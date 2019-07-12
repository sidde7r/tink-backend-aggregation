package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.SebPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class PaymentStatusResponse {
    private String transactionStatus;
    private List<ScaMethodEntity> scaMethods;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getAuthenticationMethodId() {
        return Optional.ofNullable(scaMethods)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        ErrorMessages.AUTHENTICATION_METHOD_ID_MISSING))
                .stream()
                .findFirst()
                .map(ScaMethodEntity::getAuthenticationMethodId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        ErrorMessages.AUTHENTICATION_METHOD_ID_MISSING));
    }

    public boolean hasMethodSelectionEntity() {
        return links != null && links.hasMethodSelectionEntity();
    }

    public boolean isReadyForSigning() {
        return hasMethodSelectionEntity()
                && transactionStatus.equals(SebPaymentStatus.RCVD.getText());
    }
}
