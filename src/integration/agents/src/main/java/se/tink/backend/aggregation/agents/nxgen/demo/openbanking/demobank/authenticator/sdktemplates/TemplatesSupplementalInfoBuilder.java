package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.sdktemplates;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum TemplateType {
        APP_CODE("2FA App Code") {
            @Override
            List<Field> getTemplate(String otpCode) {
                AppCodeData appCodeData = TemplatesDataBuilder.prepareAppCodeData(otpCode);
                return AppCodeTemplate.getTemplate(appCodeData);
            }
        },
        CARD_READER("2FA Card Reader") {
            @Override
            List<Field> getTemplate(String otpCode) {
                CardReaderData cardReaderData = TemplatesDataBuilder.prepareCardReaderData(otpCode);
                return CardReaderTemplate.getTemplate(cardReaderData);
            }
        },
        DECOUPLED("2FA Decoupled") {
            @Override
            List<Field> getTemplate(String otpCode) {
                DecoupledData decoupledData = TemplatesDataBuilder.prepareDecoupledData();
                return DecoupledTemplate.getTemplate(decoupledData);
            }
        },
        DECOUPLED_CHANGE("2FA Decoupled With Change") {
            @Override
            List<Field> getTemplate(String otpCode) {
                DecoupledWithChangeMethodData decoupledWithChangeMethodData =
                        TemplatesDataBuilder.prepareDecoupledWithChangeMethodData();
                return DecoupledWithChangeMethodTemplate.getTemplate(decoupledWithChangeMethodData);
            }
        },
        ID_COMPLETION("2FA ID Completion") {
            @Override
            List<Field> getTemplate(String otpCode) {
                IdCompletionData idCompletionData = TemplatesDataBuilder.prepareIdCompletionData();
                return IdCompletionTemplate.getTemplate(idCompletionData);
            }
        },
        SMS_CODE("2FA SMS Code") {
            @Override
            List<Field> getTemplate(String otpCode) {
                SmsCodeData smsCodeData = TemplatesDataBuilder.prepareSmsCodeData(otpCode);
                return SmsCodeTemplate.getTemplate(smsCodeData);
            }
        };

        @Getter private final String displayName;

        abstract List<Field> getTemplate(String otpCode);
    }

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
        return Stream.of(TemplateType.values())
                .map(tt -> new SelectOption(tt.getDisplayName(), tt.name()))
                .collect(Collectors.toList());
    }

    public static List<Field> createTemplateSupplementalInfo(
            TemplatesSupplementalInfoBuilder.TemplateType chosen2faOption, String otpCode) {
        return chosen2faOption.getTemplate(otpCode);
    }
}
