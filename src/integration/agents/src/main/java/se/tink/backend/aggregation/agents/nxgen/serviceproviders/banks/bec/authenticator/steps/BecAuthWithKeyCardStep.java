package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.KeyCardEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BecAuthWithKeyCardStep {

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticate() {
        KeyCardEntity keyCardEntity = fetchKeyCardValues();

        String code = askUserForCode(keyCardEntity);

        String token = exchangeCodeForToken(code, keyCardEntity);
        storage.saveScaToken(token);
    }

    private KeyCardEntity fetchKeyCardValues() {
        return apiClient
                .postKeyCardValuesAndDecryptResponse(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        storage.getDeviceId())
                .getKeycard();
    }

    private String askUserForCode(KeyCardEntity keyCardEntity) {
        Field keyCardInfoField =
                CommonFields.KeyCardInfo.build(
                        catalog, keyCardEntity.getKeycardNo(), keyCardEntity.getNemidChallenge());
        Field keyCardCodeField = CommonFields.KeyCardCode.build(catalog, 6);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        keyCardInfoField, keyCardCodeField);

        return Optional.ofNullable(supplementalInfoResponse.get(keyCardCodeField.getName()))
                .orElseThrow(SupplementalInfoError.NO_VALID_CODE::exception);
    }

    private String exchangeCodeForToken(String code, KeyCardEntity keyCardEntity) {
        return apiClient
                .authKeyCard(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        code,
                        keyCardEntity.getNemidChallenge(),
                        storage.getDeviceId())
                .getScaToken();
    }
}
