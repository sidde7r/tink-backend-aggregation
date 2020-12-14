package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.pis.filter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter.UkOpenBankingPisRequestFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NationwidePisRequestFilter extends UkOpenBankingPisRequestFilter {

    public NationwidePisRequestFilter(
            UkOpenBankingJwtSignatureHelper jwtSignatureHelper,
            UkOpenBankingPaymentStorage storage,
            RandomValueGenerator randomValueGenerator) {
        super(jwtSignatureHelper, storage, randomValueGenerator);
    }

    @Override
    protected void validateResponse(HttpResponse httpResponse, HttpRequest httpRequest) {
        // Do nothing
    }
}
