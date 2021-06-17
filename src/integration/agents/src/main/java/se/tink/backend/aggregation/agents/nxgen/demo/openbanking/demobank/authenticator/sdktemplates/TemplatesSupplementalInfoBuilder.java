package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.sdktemplates;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.AppCodeTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.dto.AppCodeData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.CardReaderTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.dto.CardReaderData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledWithChangeMethodTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledWithChangeMethodData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.IdCompletionTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.dto.IdCompletionData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.SmsCodeTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto.SmsCodeData;

@UtilityClass
public class TemplatesSupplementalInfoBuilder {

    public static Field createTemplateSelectOption(String message, String name) {
        return Field.builder()
                .description("Available 2FA options are: ")
                .helpText(message)
                .immutable(true)
                .masked(false)
                .name(name)
                .numeric(true)
                .selectOptions(getSelectOptions())
                .build();
    }

    private static List<SelectOption> getSelectOptions() {
        List<SelectOption> selectOptions = new ArrayList<>();
        selectOptions.add(new SelectOption("2FA App Code", "2fa-appcode"));
        selectOptions.add(new SelectOption("2FA Card Reader", "2fa-cardreader"));
        selectOptions.add(new SelectOption("2FA Decoupled", "2fa-decoupled"));
        selectOptions.add(new SelectOption("2FA Decoupled With Change", "2fa-decoupled-change"));
        selectOptions.add(new SelectOption("2FA ID Completion", "2fa-idcompletion"));
        selectOptions.add(new SelectOption("2FA SMS Code", "2fa-smscode"));
        return selectOptions;
    }

    public static List<Field> createTemplateSupplementalInfo(
            String chosen2faOption, String otpCode) {
        List<Field> fields;
        switch (chosen2faOption) {
            case "2fa-appcode":
                AppCodeData appCodeData = TemplatesDataBuilder.prepareAppCodeData(otpCode);
                fields = AppCodeTemplate.getTemplate(appCodeData);
                break;
            case "2fa-cardreader":
                CardReaderData cardReaderData = TemplatesDataBuilder.prepareCardReaderData(otpCode);
                fields = CardReaderTemplate.getTemplate(cardReaderData);
                break;
            case "2fa-decoupled":
                DecoupledData decoupledData = TemplatesDataBuilder.prepareDecoupledData();
                fields = DecoupledTemplate.getTemplate(decoupledData);
                break;
            case "2fa-decoupled-change":
                DecoupledWithChangeMethodData decoupledWithChangeMethodData =
                        TemplatesDataBuilder.prepareDecoupledWithChangeMethodData();
                fields =
                        DecoupledWithChangeMethodTemplate.getTemplate(
                                decoupledWithChangeMethodData);
                break;
            case "2fa-idcompletion":
                IdCompletionData idCompletionData = TemplatesDataBuilder.prepareIdCompletionData();
                fields = IdCompletionTemplate.getTemplate(idCompletionData);
                break;
            case "2fa-smscode":
                SmsCodeData smsCodeData = TemplatesDataBuilder.prepareSmsCodeData(otpCode);
                fields = SmsCodeTemplate.getTemplate(smsCodeData);
                break;
            default:
                // should not reach here
                throw new IllegalStateException("No valid 2fa method was selected");
        }
        return fields;
    }
}
