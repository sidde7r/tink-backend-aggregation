package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;

public class DkbSupplementalDataProvider {

    private static final Pattern EXTRACT_STARTCODE_PATTERN = Pattern.compile("Startcode (\\d+)");

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public DkbSupplementalDataProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    String getTanCode(List<String> challengeData) throws SupplementalInfoException {
        return getTanCode(null, challengeData);
    }

    String getTanCode(String scaMethodName, List<String> challengeData)
            throws SupplementalInfoException {
        List<Field> fields = getSupplementalFields(scaMethodName, challengeData);
        return supplementalInformationHelper
                .askSupplementalInformation(fields.toArray(new Field[0]))
                .get(fields.get(fields.size() - 1).getName());
    }

    List<Field> getSupplementalFields(String scaMethodName, List<String> challengeData) {
        List<Field> fields = new LinkedList<>();

        extractStartCode(challengeData)
                .ifPresent(s -> fields.add(GermanFields.Startcode.build(catalog, s)));
        fields.add(GermanFields.Tan.build(catalog, scaMethodName));

        return fields;
    }

    private Optional<String> extractStartCode(List<String> challengeData) {
        return challengeData.stream()
                .filter(s -> EXTRACT_STARTCODE_PATTERN.matcher(s).find())
                .map(
                        s -> {
                            Matcher matcher = EXTRACT_STARTCODE_PATTERN.matcher(s);
                            return matcher.find() ? matcher.group(1) : null;
                        })
                .findFirst();
    }

    String selectAuthMethod(List<? extends SelectableMethod> methods)
            throws SupplementalInfoException {
        Field field =
                CommonFields.Selection.build(
                        catalog,
                        methods.stream()
                                .map(SelectableMethod::getName)
                                .collect(Collectors.toList()));

        if (methods.size() > 1) {
            Map<String, String> supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(field);
            return getSelectedAuthMethodId(supplementalInformation.get(field.getName()), methods);
        }
        return methods.get(0).getIdentifier();
    }

    private String getSelectedAuthMethodId(
            String selectedIndex, List<? extends SelectableMethod> methods) {
        int index = Integer.parseInt(selectedIndex) - 1;
        return methods.get(index).getIdentifier();
    }
}
