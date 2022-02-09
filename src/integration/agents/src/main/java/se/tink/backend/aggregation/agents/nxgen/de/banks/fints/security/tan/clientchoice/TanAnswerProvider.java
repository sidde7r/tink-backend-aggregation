package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception.ClientAnswerException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n_aggregation.Catalog;

public class TanAnswerProvider {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public TanAnswerProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    public String getTanAnswer(String tanMedium) {
        Map<String, String> supplementalInformation;
        Field field = GermanFields.Tan.builder(catalog).authenticationMethodName(tanMedium).build();
        try {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(field);
        } catch (SupplementalInfoException e) {
            throw new ClientAnswerException("Could not get TAN Answer", e);
        }
        return supplementalInformation.get(field.getName());
    }
}
