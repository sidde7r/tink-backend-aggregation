package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.step;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.AuthenticationParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.SupplementalInformationKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.PinScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors;
import se.tink.libraries.cryptography.LaCaixaPasswordHash;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class BankiaSignatureStep implements AuthenticationStep {
    private final Catalog catalog;
    private final Storage authStorage;
    private final LaCaixaApiClient apiClient;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        if (!request.getUserInputs().containsKey(SupplementalInformationKeys.BANKIA_SIGNATURE)) {
            final List<Field> fields = ImmutableList.of(getBankiaSignatureField());
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder().withFields(fields).build());
        }

        PinScaEntity pinScaEntity =
                authStorage
                        .get(TemporaryStorage.PIN_BANKIA, PinScaEntity.class)
                        .orElseThrow(() -> new IllegalStateException("Missing Pin Sca entity"));

        String signature =
                request.getUserInputs().get(SupplementalInformationKeys.BANKIA_SIGNATURE);

        final String signatureCode =
                LaCaixaPasswordHash.hash(
                        pinScaEntity.getSeed(), pinScaEntity.getIterations(), signature);

        ScaResponse scaResponse = apiClient.authorizeSCA(signatureCode);

        if (AuthenticationParams.SCA_TYPE_SMS.equalsIgnoreCase(scaResponse.getScaType())) {
            authStorage.put(TemporaryStorage.SCA_SMS, scaResponse.getSms());
            return AuthenticationStepResponse.executeStepWithId(OtpStep.class.getName());
        }

        throw new ConnectivityException(TinkSideErrors.AUTHENTICATION_METHOD_NOT_SUPPORTED);
    }

    private Field getBankiaSignatureField() {
        return Field.builder()
                .helpText(
                        catalog.getString(
                                new LocalizableKey("Enter your Bankia digital signature")))
                .description(catalog.getString(new LocalizableKey("Bankia digital signature")))
                .name(SupplementalInformationKeys.BANKIA_SIGNATURE)
                .numeric(false)
                .patternError(catalog.getString("The Bankia signature you entered is not valid"))
                .build();
    }
}
