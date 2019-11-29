package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.Credentials;
import se.tink.sa.model.auth.Field;
import se.tink.sa.services.auth.ProgressiveAuthAgentServiceGrpc;

public class AuthenticationService {
    private final ProgressiveAuthAgentServiceGrpc.ProgressiveAuthAgentServiceBlockingStub
            progressiveAuthAgentServiceBlockingStub;

    public AuthenticationService(final ManagedChannel channel) {
        progressiveAuthAgentServiceBlockingStub =
                ProgressiveAuthAgentServiceGrpc.newBlockingStub(channel);
    }

    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request) {
        return mapResponse(progressiveAuthAgentServiceBlockingStub.login(mapRequest(request)));
    }

    private AuthenticationRequest mapRequest(final SteppableAuthenticationRequest request) {
        AuthenticationRequest.Builder builder = AuthenticationRequest.newBuilder();
        builder.setCorrelationId(UUID.randomUUID().toString());

        Credentials mappedCredentials = mapCredentials(request.getPayload());
        if (mappedCredentials != null) {
            builder.setCredentials(mapCredentials(request.getPayload()));
        }

        if (request.getPayload().getUserInputs() != null) {
            builder.addAllUserInputs(request.getPayload().getUserInputs());
        }

        if (request.getPayload().getCallbackData() != null) {
            builder.putAllCallbackData(request.getPayload().getCallbackData());
        }

        return builder.build();
    }

    private Credentials mapCredentials(
            final se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest
                    payload) {
        if (payload == null) {
            return Credentials.newBuilder().build();
        }

        Credentials.Builder builder = Credentials.newBuilder();
        se.tink.backend.agents.rpc.Credentials payloadCredentials = payload.getCredentials();

        if (payloadCredentials != null) {

            if (payloadCredentials.getDebugUntil() != null) {
                builder.setDebugUntil(mapToGoogleDate(payloadCredentials.getDebugUntil()));
            }

            if (payloadCredentials.getId() != null) {
                builder.setId(payloadCredentials.getId());
            }

            if (payloadCredentials.getNextUpdate() != null) {
                builder.setNextUpdate(mapToGoogleDate(payloadCredentials.getNextUpdate()));
            }

            if (payloadCredentials.getPayload() != null) {
                builder.setPayload(payloadCredentials.getPayload());
            }

            if (payloadCredentials.getProviderName() != null) {
                builder.setProviderName(payloadCredentials.getProviderName());
            }

            if (payloadCredentials.getSessionExpiryDate() != null) {
                builder.setSessionExpiryDate(
                        mapToGoogleDate(payloadCredentials.getSessionExpiryDate()));
            }

            if (payloadCredentials.getStatus() != null) {
                builder.setStatus(mapToCredentialStatus(payloadCredentials.getStatus()));
            }

            if (payloadCredentials.getStatusPayload() != null) {
                builder.setStatusPayload(payloadCredentials.getStatusPayload());
            }

            if (payloadCredentials.getStatusPrompt() != null) {
                builder.setStatusPrompt(payloadCredentials.getStatusPrompt());
            }

            if (payloadCredentials.getStatusUpdated() != null) {
                builder.setStatusUpdated(mapToGoogleDate(payloadCredentials.getStatusUpdated()));
            }

            if (payloadCredentials.getSupplementalInformation() != null) {
                builder.setSupplementalInformation(payloadCredentials.getSupplementalInformation());
            }

            if (payloadCredentials.getType() != null) {
                builder.setType(mapToCredentialsType(payloadCredentials.getType()));
            }

            if (payloadCredentials.getUpdated() != null) {
                builder.setUpdated(mapToGoogleDate(payloadCredentials.getUpdated()));
            }

            if (payloadCredentials.getUserId() != null) {
                builder.setUserId(payloadCredentials.getUserId());
            }

            builder.setForceManualAuthentication(payloadCredentials.forceManualAuthentication());
            builder.setDataVersion(payloadCredentials.getDataVersion());

            builder.setProviderLatency(payloadCredentials.getProviderLatency());
        }

        return builder.build();
    }

    private Credentials.CredentialsTypes mapToCredentialsType(
            final CredentialsTypes credentialsTypes) {
        return Credentials.CredentialsTypes.values()[credentialsTypes.ordinal()];
    }

    private Credentials.CredentialsStatus mapToCredentialStatus(
            final CredentialsStatus credentialsStatus) {
        return Credentials.CredentialsStatus.values()[credentialsStatus.ordinal()];
    }

    private SteppableAuthenticationResponse mapResponse(
            final AuthenticationResponse authenticationResponse) {

        if (authenticationResponse.getFieldsCount() > 0) {
            return SteppableAuthenticationResponse.finalResponse(
                    se.tink.backend.aggregation.nxgen.controllers.authentication
                            .AuthenticationResponse.fromSupplementalFields(
                            mapFieldList(authenticationResponse.getFieldsList())));
        }

        if (authenticationResponse.getPayload() != null) {
            return SteppableAuthenticationResponse.finalResponse(
                    se.tink.backend.aggregation.nxgen.controllers.authentication
                            .AuthenticationResponse.openThirdPartyApp(
                            mapThirdPartyAppAuthenticationPayload(
                                    authenticationResponse.getPayload())));
        }

        if (authenticationResponse.getSupplementalWaitRequest() != null) {
            return SteppableAuthenticationResponse.finalResponse(
                    se.tink.backend.aggregation.nxgen.controllers.authentication
                            .AuthenticationResponse.requestWaitingForSupplementalInformation(
                            mapSupplementalWaitRequest(
                                    authenticationResponse.getSupplementalWaitRequest())));
        }

        return SteppableAuthenticationResponse.finalResponse(
                se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse
                        .empty());
    }

    private SupplementalWaitRequest mapSupplementalWaitRequest(
            final se.tink.sa.model.auth.SupplementalWaitRequest req) {
        return new SupplementalWaitRequest(
                req.getKey(), req.getWaitFor(), mapTimeUnit(req.getTimeUnit()));
    }

    private TimeUnit mapTimeUnit(
            final se.tink.sa.model.auth.SupplementalWaitRequest.TimeUnit timeUnit) {
        return TimeUnit.values()[timeUnit.getNumber()];
    }

    private ThirdPartyAppAuthenticationPayload mapThirdPartyAppAuthenticationPayload(
            final se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload
                    thirdPartyAppAuthenticationPayload) {
        final ThirdPartyAppAuthenticationPayload resp = new ThirdPartyAppAuthenticationPayload();

        if (thirdPartyAppAuthenticationPayload.getAndroid() != null) {
            resp.setAndroid(mapAndroid(thirdPartyAppAuthenticationPayload.getAndroid()));
        }

        if (thirdPartyAppAuthenticationPayload.getDesktop() != null) {
            resp.setDesktop(mapDesktop(thirdPartyAppAuthenticationPayload.getDesktop()));
        }

        if (thirdPartyAppAuthenticationPayload.getIos() != null) {
            resp.setIos(mapIos(thirdPartyAppAuthenticationPayload.getIos()));
        }

        resp.setDownloadMessage(thirdPartyAppAuthenticationPayload.getDownloadMessage());
        resp.setDownloadTitle(thirdPartyAppAuthenticationPayload.getDownloadTitle());
        resp.setUpgradeMessage(thirdPartyAppAuthenticationPayload.getUpgradeMessage());
        resp.setUpgradeTitle(thirdPartyAppAuthenticationPayload.getUpgradeTitle());

        return resp;
    }

    private ThirdPartyAppAuthenticationPayload.Android mapAndroid(
            final se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload.Android android) {
        ThirdPartyAppAuthenticationPayload.Android resp =
                new ThirdPartyAppAuthenticationPayload.Android();
        resp.setIntent(android.getIntent());
        resp.setPackageName(android.getPackageName());
        resp.setRequiredVersion(android.getRequiredMinimumVersion());
        return resp;
    }

    private ThirdPartyAppAuthenticationPayload.Desktop mapDesktop(
            final se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload.Desktop desktop) {
        ThirdPartyAppAuthenticationPayload.Desktop resp =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        resp.setUrl(desktop.getUrl());
        return resp;
    }

    private ThirdPartyAppAuthenticationPayload.Ios mapIos(
            final se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload.Ios ios) {
        ThirdPartyAppAuthenticationPayload.Ios resp = new ThirdPartyAppAuthenticationPayload.Ios();
        resp.setAppScheme(ios.getScheme());
        resp.setAppStoreUrl(ios.getAppStoreUrl());
        resp.setDeepLinkUrl(ios.getDeepLinkUrl());
        return resp;
    }

    private List<se.tink.backend.agents.rpc.Field> mapFieldList(final List<Field> field) {
        return Optional.ofNullable(field).orElse(Collections.emptyList()).stream()
                .map(this::mapField)
                .collect(Collectors.toList());
    }

    private se.tink.backend.agents.rpc.Field mapField(final Field field) {
        return se.tink.backend.agents.rpc.Field.builder()
                .description(field.getDescription())
                .helpText(field.getHelpText())
                .hint(field.getHint())
                .immutable(field.getImmutable())
                .masked(field.getMasked())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .name(field.getName())
                .numeric(field.getNumeric())
                .optional(field.getOptional())
                .pattern(field.getPattern())
                .patternError(field.getPatternError())
                .value(field.getValue())
                .checkbox(field.getCheckbox())
                .additionalInfo(field.getAdditionalInfo())
                .build();
    }

    private com.google.type.Date mapToGoogleDate(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return com.google.type.Date.newBuilder()
                .setYear(calendar.get(Calendar.YEAR))
                .setMonth(calendar.get(Calendar.MONTH))
                .setDay(calendar.get(Calendar.DAY_OF_MONTH))
                .build();
    }
}
