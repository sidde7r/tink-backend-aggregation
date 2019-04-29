package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.entity.BankIdErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class BankIdInitResponse {
    private String orderRef;
    private String autoStartToken;
    private URL collectUrl;
    private BankIdErrorEntity error;

    public String getOrderRef() {
        return orderRef;
    }

    public URL getCollectUrl() {
        return collectUrl;
    }

    public BankIdErrorEntity getError() {
        return error;
    }

    public boolean isError() {
        return Objects.nonNull(error)
                || Objects.isNull(collectUrl)
                || Strings.isNullOrEmpty(collectUrl.toString())
                || Strings.isNullOrEmpty(orderRef);
    }
}
