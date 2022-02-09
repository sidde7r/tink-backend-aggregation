package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields.NemIdDemoPasswordField;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields.NemIdDemoUserIdField;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdChoose2FAMethodField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
public class DkDemoNemIdAuthenticator {

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticate(DkDemoFlow flow) {
        askForLoginScreenFields(flow);
        askToChooseFromAllAvailable2FAMethods();
        askForCodeAppAuthentication();
    }

    private void askForLoginScreenFields(DkDemoFlow flow) {
        List<Field> fields = new ArrayList<>();

        if (flow.isShouldAskNemIdUsername()) {
            fields.add(NemIdDemoUserIdField.build());
        }
        if (flow.isShouldAskNemIdPassword()) {
            fields.add(NemIdDemoPasswordField.build());
        }

        // ask & ignore response
        supplementalInformationController.askSupplementalInformationSync(
                fields.toArray(new Field[0]));
    }

    private void askToChooseFromAllAvailable2FAMethods() {
        Field field = NemIdChoose2FAMethodField.build(catalog, asList(NemId2FAMethod.values()));
        // ask & ignore response
        supplementalInformationController.askSupplementalInformationSync(field);
    }

    private void askForCodeAppAuthentication() {
        Field field = DanishFields.NemIdInfo.build(catalog);
        // ask & ignore response
        supplementalInformationController.askSupplementalInformationSync(field);
    }
}
