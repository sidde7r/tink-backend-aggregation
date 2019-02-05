package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.CreditMutuelConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class CreditCardRequest extends AbstractForm {
    public CreditCardRequest() {
        this.put(CreditMutuelConstants.RequestBodyValues.SPID,
                CreditMutuelConstants.RequestBodyValues.SPID_VALUE);
    }
}
