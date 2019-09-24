package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.session.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.session.entity.OfferEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OfferResponse extends BaseResponse {
    private String newOffer;
    private List<OfferEntity> offer;

    @JsonIgnore
    public OfferResponse handleErrors() throws SessionException {
        if (!Strings.isNullOrEmpty(errmsg)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        return this;
    }
}
