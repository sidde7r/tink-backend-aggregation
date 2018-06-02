package se.tink.backend.grpc.v1.converter.credential;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import se.tink.backend.common.payloads.MobileBankIdAuthenticationPayload;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.core.Field;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.Credential;
import se.tink.grpc.v1.models.Credentials;

public class FirehoseCredentialToGrpcCredentialConverter {

    private final String locale;

    public FirehoseCredentialToGrpcCredentialConverter(String locale) {
        this.locale = locale;
    }

    public Credentials convertFrom(List<se.tink.backend.firehose.v1.models.Credential> input) {
        Credentials.Builder credentialsBuilder = Credentials.newBuilder();
        for (se.tink.backend.firehose.v1.models.Credential credential : input) {
            Credential.Builder builder = Credential.newBuilder();
            ConverterUtils.setIfPresent(credential::getId, builder::setId);
            ConverterUtils.setIfPresent(credential::getProviderName, builder::setProviderName);
            ConverterUtils.setIfPresent(credential::getType, builder::setType,
                    type -> EnumMappers.FIREHOSE_CREDENTIALS_TYPE_TO_GRPC_MAP
                            .getOrDefault(type, Credential.Type.TYPE_UNKNOWN));
            ConverterUtils.setIfPresent(credential::getStatus, builder::setStatus,
                    status -> EnumMappers.FIREHOSE_CREDENTIALS_STATUS_TO_GRPC_MAP
                            .getOrDefault(status, Credential.Status.STATUS_UNKNOWN));
            ConverterUtils.setIfPresent(credential::getStatusPayload, builder::setStatusPayload);
            ConverterUtils.setIfPresent(credential::getStatusUpdated, builder::setStatusUpdated,
                    ProtobufModelUtils::toProtobufTimestamp);
            ConverterUtils
                    .setIfPresent(credential::getUpdated, builder::setUpdated, ProtobufModelUtils::toProtobufTimestamp);
            ConverterUtils.setIfPresent(credential::getFields, builder::putAllFields);

            final String supplementalInformation = credential.getSupplementalInformation();

            switch (credential.getStatus()) {
                case STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION:
                    ThirdPartyAppAuthenticationPayload mobileBankIdPayload = MobileBankIdAuthenticationPayload
                            .create(supplementalInformation, credential.getId(), new Locale(locale));
                    builder.setThirdPartyAppAuthentication(
                            ThirdPartyAppPayloadConverter.convertFrom(mobileBankIdPayload));
                    break;
                case STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
                    if (!Strings.isNullOrEmpty(supplementalInformation)) {
                        ThirdPartyAppAuthenticationPayload appPayload = ThirdPartyAppPayloadConverter
                                .deserializeThirdPartyAppPayload(supplementalInformation);
                        builder.setThirdPartyAppAuthentication(ThirdPartyAppPayloadConverter.convertFrom(appPayload));
                    }
                    break;
                case STATUS_AWAITING_SUPPLEMENTAL_INFORMATION:
                    if (!Strings.isNullOrEmpty(supplementalInformation)) {
                        List<Field> fields = ProviderFieldConverter.deserializeFields(supplementalInformation);
                        builder.addAllSupplementalInformationFields(
                                ProviderFieldConverter.convertProviderFields(fields));
                    }
                    break;
                default:
                    break;
                }

            credentialsBuilder.addCredential(builder);
        }
        return credentialsBuilder.build();
    }
}
