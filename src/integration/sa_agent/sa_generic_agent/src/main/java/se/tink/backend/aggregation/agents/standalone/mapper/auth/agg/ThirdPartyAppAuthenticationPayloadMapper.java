package se.tink.backend.aggregation.agents.standalone.mapper.auth.agg;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.AuthenticationResponse;

// TODO: refactor me when ready
public class ThirdPartyAppAuthenticationPayloadMapper
        implements Mapper<ThirdPartyAppAuthenticationPayload, AuthenticationResponse> {

    @Override
    public ThirdPartyAppAuthenticationPayload map(
            AuthenticationResponse source, MappingContext mappingContext) {
        return mapResponse(source);
    }

    private ThirdPartyAppAuthenticationPayload mapResponse(
            final AuthenticationResponse authenticationResponse) {
        final SupplementInformationRequester.Builder supplementInformationRequesterBuilder =
                new se.tink.backend.aggregation.nxgen.controllers.authentication
                        .SupplementInformationRequester.Builder();
        if (authenticationResponse.getFieldsCount() > 0) {
            supplementInformationRequesterBuilder.withFields(
                    mapFieldList(authenticationResponse.getFieldsList()));
        }

        if (authenticationResponse.getPayload() != null) {
            supplementInformationRequesterBuilder.withThirdPartyAppAuthenticationPayload(
                    mapThirdPartyAppAuthenticationPayload(authenticationResponse.getPayload()));
        }

        if (authenticationResponse.getSupplementalWaitRequest() != null) {
            supplementInformationRequesterBuilder.withSupplementalWaitRequest(
                    mapSupplementalWaitRequest(
                            authenticationResponse.getSupplementalWaitRequest()));
        }

        return supplementInformationRequesterBuilder.build().getThirdPartyAppPayload().get();
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

    private List<Field> mapFieldList(final List<se.tink.sa.model.auth.Field> field) {
        return Optional.ofNullable(field).orElse(Collections.emptyList()).stream()
                .map(this::mapField)
                .collect(Collectors.toList());
    }

    private se.tink.backend.agents.rpc.Field mapField(final se.tink.sa.model.auth.Field field) {
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
}
