package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.CollectionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.OperationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ParametersEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.PublicKeyEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;

final class RequestFactory {

    private RequestFactory() {
        throw new UnsupportedOperationException();
    }

    static DeviceOperationRequest create(
            MetroProcessState processState,
            MetroAuthenticationData authenticationData,
            String tulipReference) {
        PublicKeyEntity rsaKey =
                PublicKeyEntity.builder()
                        .key(authenticationData.getRSAPublicKey())
                        .type("rsa")
                        .build();
        PublicKeyEntity encryptedKey =
                PublicKeyEntity.builder()
                        .key(authenticationData.getSigningPublicKey())
                        .type("ec")
                        .build();

        String fixedSecuredNumber = getFixedSecuredNumber(processState, authenticationData);

        OperationDataEntity requestData =
                OperationDataEntity.builder()
                        .collectionResult(CollectionResultEntity.getDefault())
                        .publicKey(encryptedKey)
                        .encryptionPublicKey(rsaKey)
                        .parameters(
                                ParametersEntity.builder()
                                        .appVersion(GlobalConstants.APP_VERSION.getValue())
                                        .bindPurpose("REGISTER")
                                        .password(authenticationData.getPassword())
                                        .securityNumber(fixedSecuredNumber)
                                        .seedPositions(processState.getSeedPosition())
                                        .deviceId(authenticationData.getInternalDeviceId())
                                        .tulipReference(tulipReference)
                                        .isDeviceSlotAvailable(true)
                                        .isIbRegistered(true)
                                        .magicWord("")
                                        .tncAcceptedVersion("2.0")
                                        .build())
                        .build();

        return new DeviceOperationRequest(
                new UserIdHeaderEntity(authenticationData.getUserId()), requestData);
    }

    private static String getFixedSecuredNumber(
            MetroProcessState processState, MetroAuthenticationData authenticationData) {
        return processState.getSeedPosition().stream()
                .map(
                        integer ->
                                String.valueOf(
                                        authenticationData.getSecuredNumber().charAt(integer - 1)))
                .collect(Collectors.joining());
    }
}
