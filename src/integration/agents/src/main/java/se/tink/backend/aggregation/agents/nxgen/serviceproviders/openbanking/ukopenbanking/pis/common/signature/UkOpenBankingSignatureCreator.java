package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Map;

public interface UkOpenBankingSignatureCreator {

    String createSignature(Map<String, Object> payloadClaims);
}
