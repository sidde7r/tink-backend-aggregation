package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiScaMethodSelectable;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PaymentAuthorizationResponse implements CbiScaMethodSelectable {

    private List<ScaMethodEntity> scaMethods;
    private String transactionStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @Override
    public String getSelectAuthenticationMethodLink() {
        return links.getSelectAuthenticationMethod();
    }
}
