package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.step;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.CodeCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.cryptography.LaCaixaPasswordHash;
import se.tink.libraries.i18n_aggregation.Catalog;

public class CodeCardStep implements AuthenticationStep {

    private final Catalog catalog;
    private final Storage authStorage;

    public CodeCardStep(Catalog catalog, Storage authStorage) {
        this.catalog = catalog;
        this.authStorage = authStorage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final CodeCardEntity codeCard =
                authStorage
                        .get(TemporaryStorage.CODE_CARD, CodeCardEntity.class)
                        .orElseThrow(() -> new IllegalStateException("Code card entity not found"));
        if (request.getUserInputs().isEmpty()) {
            final List<Field> fields =
                    ImmutableList.of(getKeyCardIndexField(codeCard), getKeyCardValueField());
            return AuthenticationStepResponse.requestForSupplementInformation(
                    new SupplementInformationRequester.Builder().withFields(fields).build());
        }
        final String keyCardValue = request.getUserInputs().get("keyCardValue");
        final String enrolmentCode =
                LaCaixaPasswordHash.hash(
                        codeCard.getSeed(), codeCard.getIterations(), keyCardValue);
        authStorage.put(TemporaryStorage.ENROLMENT_CODE, enrolmentCode);
        return AuthenticationStepResponse.executeStepWithId("finalizeEnrolment");
    }

    private Field getKeyCardIndexField(CodeCardEntity codeCard) {
        final String keyCardId = "Tarjeta Línea Abierta *** *** " + codeCard.getCardId();
        final String keyCardCodeIndex = codeCard.getCardIndex();

        return Field.builder()
                .description(catalog.getString("Key card index"))
                .name("keyCardIndex")
                .helpText(
                        catalog.getString("Input the code from your code card")
                                + String.format(" (%s)", keyCardId))
                .value(keyCardCodeIndex)
                .immutable(true)
                .build();
    }

    private Field getKeyCardValueField() {
        return Field.builder()
                .description(catalog.getString("Key card code"))
                .name("keyCardValue")
                .numeric(true)
                .minLength(4)
                .maxLength(4)
                .hint("NNNN")
                .pattern("([0-9]{4})")
                .patternError(catalog.getString("The code you entered is not valid"))
                .build();
    }
}
