package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.OtpFormat;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.TanBuilder;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n_aggregation.Catalog;

public class DkbSupplementalDataProvider {

    private static final Pattern EXTRACT_STARTCODE_PATTERN = Pattern.compile("Startcode (\\d+)");

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    public DkbSupplementalDataProvider(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
    }

    String getTanCode(List<String> data) throws SupplementalInfoException {
        return getTanCode(null, data);
    }

    String getTanCode(Authorization.ScaMethod scaMethod, List<String> data)
            throws SupplementalInfoException {
        List<Field> fields = getSupplementalFields(scaMethod, data);
        String otp =
                supplementalInformationHelper
                        .askSupplementalInformation(fields.toArray(new Field[0]))
                        .get(fields.get(fields.size() - 1).getName());
        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        } else {
            return otp;
        }
    }

    List<Field> getSupplementalFields(Authorization.ScaMethod scaMethod, List<String> data) {
        List<Field> fields = new LinkedList<>();

        extractStartCode(data).ifPresent(s -> fields.add(GermanFields.Startcode.build(catalog, s)));

        TanBuilder tanBuilder = sixDigitsTanBuilder();

        if (scaMethod != null) {
            tanBuilder.authenticationType(scaMethod.getAuthenticationType());
            tanBuilder.authenticationMethodName(scaMethod.getName());
        }

        fields.add(tanBuilder.build());
        return fields;
    }

    private TanBuilder sixDigitsTanBuilder() {
        TanBuilder tanBuilder = GermanFields.Tan.builder(catalog);
        tanBuilder.otpMinLength(6);
        tanBuilder.otpMaxLength(6);
        tanBuilder.otpFormat(OtpFormat.INTEGER);
        return tanBuilder;
    }

    private Optional<String> extractStartCode(List<String> challengeData) {
        return challengeData.stream()
                .filter(Objects::nonNull)
                .filter(s -> EXTRACT_STARTCODE_PATTERN.matcher(s).find())
                .map(
                        s -> {
                            Matcher matcher = EXTRACT_STARTCODE_PATTERN.matcher(s);
                            return matcher.find() ? matcher.group(1) : null;
                        })
                .findFirst();
    }

    SelectableMethod selectAuthMethod(List<? extends SelectableMethod> methods)
            throws SupplementalInfoException {
        if (methods.size() > 1) {
            Field field =
                    CommonFields.Selection.build(
                            catalog,
                            null,
                            GermanFields.SelectOptions.prepareSelectOptions(methods));
            Map<String, String> supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(field);
            return getSelectedAuthMethod(supplementalInformation.get(field.getName()), methods);
        }
        return methods.get(0);
    }

    private SelectableMethod getSelectedAuthMethod(
            String selectedValue, List<? extends SelectableMethod> methods) {
        if (StringUtils.isNumeric(selectedValue)) {
            int index = Integer.parseInt(selectedValue) - 1;
            if (index >= 0 && index < methods.size()) {
                return methods.get(index);
            }
        }
        throw SupplementalInfoError.NO_VALID_CODE.exception(
                "Could not map user input to list of available options.");
    }
}
