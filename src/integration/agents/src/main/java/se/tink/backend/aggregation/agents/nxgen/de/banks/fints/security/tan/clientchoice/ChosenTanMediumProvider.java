package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception.ClientAnswerException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.utils.RangeRegex;

public class ChosenTanMediumProvider {
    private static final String TAN_MEDIUM_KEY = "tanMedium";
    private final SupplementalInformationHelper supplementalInformationHelper;

    public ChosenTanMediumProvider(SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public String getTanMedium(FinTsDialogContext context) {
        Map<String, String> supplementalInformation;
        List<String> tanMediumList = context.getTanMediumList();
        try {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            getFieldForGeneratedTan(tanMediumList));
        } catch (SupplementalInfoException e) {
            throw new ClientAnswerException("Could not get Tan Medium selection", e);
        }

        int index = Integer.parseInt(supplementalInformation.get(TAN_MEDIUM_KEY));
        return tanMediumList.get(index);
    }

    private Field getFieldForGeneratedTan(List<String> tanMediumList) {
        int maxNumber = tanMediumList.size() - 1;
        int length = Integer.toString(maxNumber).length();
        String description =
                IntStream.range(0, tanMediumList.size())
                        .mapToObj(i -> String.format("(%d) %s", i, tanMediumList.get(i)))
                        .collect(Collectors.joining("\n"));
        String regexForRangePattern = RangeRegex.regexForRange(0, maxNumber);

        return Field.builder()
                .description("Choose TAN Medium you want to use")
                .helpText("TAN Mediums:\n" + description)
                .name(TAN_MEDIUM_KEY)
                .numeric(true)
                .minLength(1)
                .maxLength(length)
                .hint(String.format("Select from 0 to %d", maxNumber))
                .pattern(regexForRangePattern)
                .patternError("Invalid selection")
                .build();
    }
}
