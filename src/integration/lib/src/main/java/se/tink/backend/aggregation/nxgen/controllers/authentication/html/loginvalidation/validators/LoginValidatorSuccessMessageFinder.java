package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;

public class LoginValidatorSuccessMessageFinder implements LoginValidator<String> {

    private final List<String> successMessages;
    private final ConnectivityException exceptionToThrowWhenSuccessMessageNotFound;

    public LoginValidatorSuccessMessageFinder(
            ConnectivityException exceptionToThrowWhenSuccessMessageNotFound,
            String... successMessage) {
        Preconditions.checkArgument(successMessage.length > 0, "At least one message is required");
        this.exceptionToThrowWhenSuccessMessageNotFound =
                exceptionToThrowWhenSuccessMessageNotFound;
        this.successMessages = Arrays.asList(successMessage);
    }

    @Override
    public void validate(String response) {
        successMessages.stream()
                .filter(response::contains)
                .findAny()
                .orElseThrow(() -> exceptionToThrowWhenSuccessMessageNotFound);
    }
}
