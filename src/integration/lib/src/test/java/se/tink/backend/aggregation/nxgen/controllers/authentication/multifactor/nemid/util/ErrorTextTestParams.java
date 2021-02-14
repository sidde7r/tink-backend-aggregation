package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asArray;

import com.google.common.base.CaseFormat;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;

@Ignore
@RequiredArgsConstructor
public class ErrorTextTestParams {

    private final String errorText;
    private final AgentException expectedException;

    public static ErrorTextTestParams of(String errorText, AgentException expectedException) {
        return new ErrorTextTestParams(errorText, expectedException);
    }

    public ErrorTextTestParams changeExpectedError(AgentException newExpectedException) {
        return new ErrorTextTestParams(errorText, newExpectedException);
    }

    public ErrorTextTestParams modifyErrorText(Function<String, String> errorTextModifier) {
        String newErrorText = errorTextModifier.apply(errorText);
        return new ErrorTextTestParams(newErrorText, expectedException);
    }

    public ErrorTextTestParams changeErrorTextCase(CaseFormat caseFormat) {
        String newErrorText = convertToCase(caseFormat).apply(errorText);
        return new ErrorTextTestParams(newErrorText, expectedException);
    }

    public ErrorTextTestParams addErrorTextSuffix(String suffix) {
        String newErrorText = errorText + suffix;
        return new ErrorTextTestParams(newErrorText, expectedException);
    }

    public ErrorTextTestParams addErrorTextPrefix(String prefix) {
        String newErrorText = prefix + errorText;
        return new ErrorTextTestParams(newErrorText, expectedException);
    }

    public ErrorTextTestParams changeErrorTextCompletely(String newErrorText) {
        return new ErrorTextTestParams(newErrorText, expectedException);
    }

    public Object[] toTestParameters() {
        return asArray(errorText, expectedException);
    }

    private static Function<String, String> convertToCase(CaseFormat caseFormat) {
        return text -> CaseFormat.LOWER_CAMEL.to(caseFormat, text.toLowerCase());
    }
}
