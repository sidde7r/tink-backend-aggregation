package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SignPaymentResponse {

    private String paymentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private List<TppMessage> tppMessages;

    private String transactionStatus;

    @JsonIgnore
    public String getScaRedirect() {
        return this.links.getScaRedirect().getHref();
    }

    @JsonObject
    @Getter
    public static class TppMessage {
        private TppMessageCategory category;
        private String code;
        private String path;
        private String text;

        public enum TppMessageCategory {
            ERROR,
            WARNING
        }
    }
}
