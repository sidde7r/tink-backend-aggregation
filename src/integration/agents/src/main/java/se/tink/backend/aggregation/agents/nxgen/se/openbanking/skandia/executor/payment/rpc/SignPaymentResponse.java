package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinkDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Getter
public final class SignPaymentResponse {

    private final String paymentId;

    private final LinksEntity links;

    private final List<TppMessage> tppMessages;

    private final String transactionStatus;

    @JsonCreator
    public SignPaymentResponse(
            @JsonProperty("paymentId") String paymentId,
            @JsonProperty("_links") LinksEntity links,
            @JsonProperty("tppMessages") List<TppMessage> tppMessages,
            @JsonProperty("transactionStatus") String transactionStatus) {
        this.paymentId = paymentId;
        this.links = links;
        this.tppMessages = tppMessages;
        this.transactionStatus = transactionStatus;
    }

    @JsonIgnore
    public Optional<URL> getScaRedirect() {
        return Optional.ofNullable(this.links)
                .map(LinksEntity::getScaRedirect)
                .map(LinkDetailsEntity::getHref)
                .map(URL::new);
    }

    @JsonObject
    @Getter
    public static class TppMessage {
        private final TppMessageCategory category;
        private final String code;
        private final String path;
        private final String text;

        public TppMessage(
                @JsonProperty("category") TppMessageCategory category,
                @JsonProperty("code") String code,
                @JsonProperty("path") String path,
                @JsonProperty("text") String text) {
            this.category = category;
            this.code = code;
            this.path = path;
            this.text = text;
        }

        public enum TppMessageCategory {
            ERROR,
            WARNING
        }
    }
}
