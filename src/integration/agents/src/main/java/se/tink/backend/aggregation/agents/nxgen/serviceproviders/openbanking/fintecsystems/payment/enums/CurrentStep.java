package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CurrentStep {
    WIZARD_LOGIN_STEP("WizardLoginStep"),
    WIZARD_LOGIN_NEXT_STEP("WizardLoginNextStep"),
    WIZARD_ACCOUNTS_STEP("WizardAccountsStep"),
    WIZARD_DATA_STEP("WizardDataStep"),
    WIZARD_TAN_STEP("WizardTanStep"),
    WIZARD_SCA_STEP("WizardScaStep"),
    WIZARD_IRPA_STEP("WizardIrpaStep"),
    WIZARD_AUTO_SUBMIT_STEP("WizardAutoSubmitStep"),
    WIZARD_FINISH_STEP("WizardFinishStep"),
    WIZARD_START_STEP("WizardStartStep"),
    WIZARD_SHOWCASE_STEP("WizardShowcaseStep"),
    WIZARD_PRE_ACCOUNTS_STEP("WizardPreAccountsStep"),
    WIZARD_PRE_DATA_STEP("WizardPreDataStep"),
    WIZARD_PRE_LOGIN_STEP("WizardPreLoginStep"),
    UNKNOWN("Unknown");

    private String step;

    public static CurrentStep fromString(String text) {
        return Arrays.stream(CurrentStep.values())
                .filter(s -> s.step.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return step;
    }
}
