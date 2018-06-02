package se.tink.backend.grpc.v1.converter.consent;

import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.rpc.GiveConsentRequest;

public class CoreConsentRequestConverter {

    public static ConsentRequest convert(GiveConsentRequest input) {
        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setKey(input.getKey());
        consentRequest.setChecksum(input.getChecksum());
        consentRequest.setVersion(input.getVersion());
        consentRequest.setAction(EnumMappers.CORE_CONSENT_ACTION_TO_GRPC_MAP.inverse().get(input.getAction()));

        return consentRequest;
    }
}
