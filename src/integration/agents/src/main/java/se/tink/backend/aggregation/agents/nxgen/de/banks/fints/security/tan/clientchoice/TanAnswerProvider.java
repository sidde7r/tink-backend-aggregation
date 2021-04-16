package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception.ClientAnswerException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;

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
        List<Field> fields = new LinkedList<>();
        try {
            fields.add(GermanFields.Tan.build(catalog, null, tanMedium, null, null, null));
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            fields.toArray(new Field[0]));
        } catch (SupplementalInfoException e) {
            throw new ClientAnswerException("Could not get TAN Answer", e);
        }

        return supplementalInformation.get(fields.get(fields.size() - 1).getName());
    }
}
