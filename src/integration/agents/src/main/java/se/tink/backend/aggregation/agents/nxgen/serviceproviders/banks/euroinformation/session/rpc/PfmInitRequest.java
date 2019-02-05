package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class PfmInitRequest extends AbstractForm {
    public PfmInitRequest() {
        this.put(RequestBodyValues.ACTION, RequestBodyValues.INIT);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
    }
}
