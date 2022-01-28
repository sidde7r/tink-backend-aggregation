package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields.MitIdDemoCodeAppField;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields.MitIdDemoCprField;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields.MitIdDemoUserIdField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

@RequiredArgsConstructor
public class DkDemoMitIdAuthenticator {

    private final SupplementalInformationController supplementalInformationController;

    public void authenticate(DkDemoFlow flow) {
        askForLoginScreenFields(flow);
        askForCodeAppAuthentication();
        askForCpr(flow);
    }

    private void askForLoginScreenFields(DkDemoFlow flow) {
        if (flow.isShouldAskMitIdUsername()) {
            Field field = MitIdDemoUserIdField.build();
            // ask & ignore response
            supplementalInformationController.askSupplementalInformationSync(field);
        }
    }

    private void askForCodeAppAuthentication() {
        List<Field> fields = MitIdDemoCodeAppField.build();
        // ask & ignore response
        supplementalInformationController.askSupplementalInformationSync(
                fields.toArray(new Field[0]));
    }

    private void askForCpr(DkDemoFlow flow) {
        if (flow.isShouldAskMitIdCpr()) {
            Field field = MitIdDemoCprField.build();
            // ask & ignore response
            supplementalInformationController.askSupplementalInformationSync(field);
        }
    }
}
