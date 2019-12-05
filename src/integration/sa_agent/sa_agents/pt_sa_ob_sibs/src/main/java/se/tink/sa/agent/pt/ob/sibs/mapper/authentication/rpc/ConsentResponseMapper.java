package se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.framework.mapper.ToDomainMapper;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;

@Component
public class ConsentResponseMapper
        implements ToDomainMapper<AuthenticationResponse, ConsentResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Override
    public AuthenticationResponse mapToTransferModel(
            ConsentResponse source, MappingContext mappingContext) {
        AuthenticationResponse.Builder destBuilder = AuthenticationResponse.newBuilder();
        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon =
                requestToResponseCommonMapper.mapToTransferModel(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);

        ThirdPartyAppAuthenticationPayload.Android.Builder android =
                ThirdPartyAppAuthenticationPayload.Android.newBuilder();
        android.setIntent(source.getLinks().getRedirect());

        ThirdPartyAppAuthenticationPayload.Ios.Builder ios =
                ThirdPartyAppAuthenticationPayload.Ios.newBuilder();
        ios.setDeepLinkUrl(source.getLinks().getRedirect());

        ThirdPartyAppAuthenticationPayload.Builder tppPl =
                ThirdPartyAppAuthenticationPayload.newBuilder();
        tppPl.setIos(ios.build());
        tppPl.setAndroid(android.build());

        destBuilder.setPayload(tppPl.build());

        return destBuilder.build();
    }
}
