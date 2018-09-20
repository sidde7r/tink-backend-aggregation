package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class CreditCardRequest extends AbstractForm {
    public CreditCardRequest() {
        this.put(EuroInformationConstants.RequestBodyValues.SPID,
                EuroInformationConstants.RequestBodyValues.SPID_VALUE);
    }
}
