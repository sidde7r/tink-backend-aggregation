package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.SigningAlgorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;

@RequiredArgsConstructor
public class UkOpenBankingJwtSignatureHelper {

    @VisibleForTesting static final String DATA = "Data";
    @VisibleForTesting static final String RISK = "Risk";

    private final ObjectMapper objectMapper;
    private final UkOpenBankingPaymentStorage paymentStorage;
    private final UkOpenBankingRs256SignatureCreator rs256SignatureCreator;
    private final UkOpenBankingPs256SignatureCreator ps256SignatureCreator;

    public String createJwtSignature(Object request) {
        final SigningAlgorithm preferredAlgorithm = paymentStorage.getPreferredSigningAlgorithm();
        final Map<String, Object> payloadClaims = createPayloadClaims(request);

        switch (preferredAlgorithm) {
            case PS256:
                return ps256SignatureCreator.createSignature(payloadClaims);
            case RS256:
            default:
                return rs256SignatureCreator.createSignature(payloadClaims);
        }
    }

    public void setSoftwareId(String softwareId) {
        rs256SignatureCreator.setSoftwareId(softwareId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createPayloadClaims(Object request) {
        final Map<String, Object> requestBody = objectMapper.convertValue(request, Map.class);

        return ImmutableMap.<String, Object>builder()
                .put(DATA, requestBody.get(DATA))
                .put(RISK, requestBody.get(RISK))
                .build();
    }
}
