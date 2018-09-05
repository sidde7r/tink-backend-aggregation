package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class InvokeResponse {
    private Boolean isSuccessful;
    private Boolean securityServiceLoginRequired;
    private String legitimation;
    private List<String> errors;
    private List<String> warnings;
    private List<String> info;
    private String securityServiceLoginError;

    /**
     * @return true if there is evidence that the server claims the request was successful
     */
    public boolean getIsSuccessful() {
        return Optional.ofNullable(isSuccessful).orElse(false);
    }

    public Boolean getSecurityServiceLoginRequired() {
        return securityServiceLoginRequired;
    }

    public String getLegitimation() {
        return legitimation;
    }

    /**
     * @return true if there is evidence that the server claims the client is authenticated
     */
    public boolean isLegit() {
        try {
            return Integer.parseInt(legitimation) >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Optional<String> getMessages() {
        return Optional.of(
                Stream.of(
                        Optional.ofNullable(errors).orElse(Collections.emptyList()),
                        Optional.ofNullable(warnings).orElse(Collections.emptyList()),
                        Optional.ofNullable(info).orElse(Collections.emptyList()),
                        Optional.ofNullable(securityServiceLoginError)
                                .map(Collections::singletonList)
                                .orElse(Collections.emptyList())
                )
                        .flatMap(Collection::stream)
                        .collect(Collectors.joining("; "))).filter(s -> !s.isEmpty());
    }

    /**
     * @return true if there is evidence that the password is incorrect
     */
    public boolean isPasswordIncorrect() {
        // Observed error message:
        // Ihre Eingaben waren ungültig ! Bitte geben Sie neu ein oder beenden Sie den Dialog (Z1110)
        return Optional.ofNullable(securityServiceLoginError)
                .orElse("")
                .replace("\n", " ")
                .matches(WLConstants.Regex.INCORRECT_PASSWORD);
    }

    /**
     * @return true iff there is evidence that account is locked
     */
    public boolean isAccountLocked() {
        return isAccountTemporarilyLocked() || isAccountPermanentlyLocked();
    }

    /**
     * @return true iff the account is temporarily locked but can be unlocked with a TAN number
     */
    private boolean isAccountTemporarilyLocked() {
        // Observed error message:
        // Der Zugang zu Ihren Online-Konten ist zur Zeit gesperrt.
        // Bitte geben Sie Ihr Kennwort und eine gültige Transaktionsnummer ein, um den Zugang zu entsperren. (Z9909)
        return Optional.ofNullable(securityServiceLoginError)
                .orElse("")
                .replace("\n", " ")
                .matches(WLConstants.Regex.ACCOUNT_LOCKED_TEMPORARILY);
    }

    /**
     * @return true iff the account is locked "permanently", i.e. in a way that necessitates contacting the bank
     */
    private boolean isAccountPermanentlyLocked() {
        // Observed error message:
        // Der Zugang zu Ihren Online-Konten ist gesperrt. Bitte wenden Sie sich an Ihre Filiale. (Z1159)
        return Optional.ofNullable(securityServiceLoginError)
                .orElse("")
                .replace("\n", " ")
                .matches(WLConstants.Regex.ACCOUNT_LOCKED_PERMANENTLY);
    }
}
