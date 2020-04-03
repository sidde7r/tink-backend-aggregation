package se.tink.backend.aggregation.agents.standalone.mapper.auth.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.AuthenticationRequest;

public class AuthenticationRequestMapper
        implements Mapper<AuthenticationRequest, SteppableAuthenticationRequest> {

    private RequestCommonMapper requestCommonMapper;

    public void setRequestCommonMapper(RequestCommonMapper requestCommonMapper) {
        this.requestCommonMapper = requestCommonMapper;
    }

    @Override
    public AuthenticationRequest map(
            SteppableAuthenticationRequest source, MappingContext mappingContext) {
        AuthenticationRequest.Builder builder = AuthenticationRequest.newBuilder();
        builder.setRequestCommon(requestCommonMapper.map(null, mappingContext));
        return builder.build();
    }

    //    private AuthenticationRequest mapRequest(final SteppableAuthenticationRequest request) {
    //        AuthenticationRequest.Builder builder = AuthenticationRequest.newBuilder();
    //        builder.setRequestCommon(mapRequestCommon());
    //
    //        Credentials mappedCredentials = mapCredentials(request.getPayload());
    //        if (mappedCredentials != null) {
    //            builder.setCredentials(mapCredentials(request.getPayload()));
    //        }
    //
    //        if (request.getPayload().getUserInputs() != null) {
    //            builder.addAllUserInputs(request.getPayload().getUserInputs().values());
    //        }
    //
    ////        SecurityInfo securityInfo = SecurityInfo.newBuilder()
    ////                .setState().build();
    ////
    ////        builder.setRequestCommon()
    //
    //        //        if (request.getPayload().getCallbackData() != null) {
    //        //            builder.putAllCallbackData(request.getPayload().getCallbackData());
    //        //        }
    //        Map<String, String> cb = new HashMap<>();
    //        builder.putAllCallbackData(cb);
    //        return builder.build();
    //    }

    //    private RequestCommon mapRequestCommon() {
    //        RequestCommon.Builder builder = RequestCommon.newBuilder();
    //        builder.setCorrelationId(UUID.randomUUID().toString());
    //        return builder.build();
    //    }
    //
    //    private Credentials mapCredentials(
    //            final
    // se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest
    //                    payload) {
    //        if (payload == null) {
    //            return Credentials.newBuilder().build();
    //        }
    //
    //        Credentials.Builder builder = Credentials.newBuilder();
    //        se.tink.backend.agents.rpc.Credentials payloadCredentials = payload.getCredentials();
    //
    //        if (payloadCredentials != null) {
    //
    //            if (payloadCredentials.getDebugUntil() != null) {
    //                builder.setDebugUntil(mapToGoogleDate(payloadCredentials.getDebugUntil()));
    //            }
    //
    //            if (payloadCredentials.getId() != null) {
    //                builder.setId(payloadCredentials.getId());
    //            }
    //
    //            if (payloadCredentials.getNextUpdate() != null) {
    //                builder.setNextUpdate(mapToGoogleDate(payloadCredentials.getNextUpdate()));
    //            }
    //
    //            if (payloadCredentials.getPayload() != null) {
    //                builder.setPayload(payloadCredentials.getPayload());
    //            }
    //
    //            if (payloadCredentials.getProviderName() != null) {
    //                builder.setProviderName(payloadCredentials.getProviderName());
    //            }
    //
    //            if (payloadCredentials.getSessionExpiryDate() != null) {
    //                builder.setSessionExpiryDate(
    //                        mapToGoogleDate(payloadCredentials.getSessionExpiryDate()));
    //            }
    //
    //            if (payloadCredentials.getStatus() != null) {
    //                builder.setStatus(mapToCredentialStatus(payloadCredentials.getStatus()));
    //            }
    //
    //            if (payloadCredentials.getStatusPayload() != null) {
    //                builder.setStatusPayload(payloadCredentials.getStatusPayload());
    //            }
    //
    //            if (payloadCredentials.getStatusPrompt() != null) {
    //                builder.setStatusPrompt(payloadCredentials.getStatusPrompt());
    //            }
    //
    //            if (payloadCredentials.getStatusUpdated() != null) {
    //
    // builder.setStatusUpdated(mapToGoogleDate(payloadCredentials.getStatusUpdated()));
    //            }
    //
    //            if (payloadCredentials.getSupplementalInformation() != null) {
    //
    // builder.setSupplementalInformation(payloadCredentials.getSupplementalInformation());
    //            }
    //
    //            if (payloadCredentials.getType() != null) {
    //                builder.setType(mapToCredentialsType(payloadCredentials.getType()));
    //            }
    //
    //            if (payloadCredentials.getUpdated() != null) {
    //                builder.setUpdated(mapToGoogleDate(payloadCredentials.getUpdated()));
    //            }
    //
    //            if (payloadCredentials.getUserId() != null) {
    //                builder.setUserId(payloadCredentials.getUserId());
    //            }
    //
    //
    // builder.setForceManualAuthentication(payloadCredentials.forceManualAuthentication());
    //            builder.setDataVersion(payloadCredentials.getDataVersion());
    //
    //            builder.setProviderLatency(payloadCredentials.getProviderLatency());
    //        }
    //
    //        return builder.build();
    //    }
    //
    //    private Credentials.CredentialsTypes mapToCredentialsType(
    //            final CredentialsTypes credentialsTypes) {
    //        return Credentials.CredentialsTypes.values()[credentialsTypes.ordinal()];
    //    }
    //
    //    private Credentials.CredentialsStatus mapToCredentialStatus(
    //            final CredentialsStatus credentialsStatus) {
    //        return Credentials.CredentialsStatus.values()[credentialsStatus.ordinal()];
    //    }
}
