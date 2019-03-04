package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonObject
public final class RegisterUserResponse {
    private OutputEntity output;

    public Set<String> getMsgcd() {
        return getErrorsEntity()
                .stream()
                .map(ErrorsEntity::getMsgCd)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<ErrorsEntity> getErrorsEntity() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getErrors)
                .map(Stream::of)
                .orElse(Stream.empty())
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public String getSerialNo() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getSerialNo)
                .orElseThrow(IllegalStateException::new);
    }

    public String getXfad() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getXfad)
                .orElseThrow(IllegalStateException::new);
    }

    public String getServerInitialVector() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getServerInitialVector)
                .orElseThrow(IllegalStateException::new);
    }

    public String getEncryptedNonces() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getEncryptedNonces)
                .orElseThrow(IllegalStateException::new);
    }

    public String getEncryptedServerPublicKey() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getEncryptedServerPublicKey)
                .orElseThrow(IllegalStateException::new);
    }

    public boolean isIncorrectResponseError() {
        final String message = AxaConstants.Response.INCORRECT_CHALLENGE_RESPONSE_SUBSTRING;
        return getErrorsEntity()
                .stream()
                .map(ErrorsEntity::getMsgDetails)
                .filter(Objects::nonNull)
                .anyMatch(msg -> msg.contains(message));
    }

    public boolean isRegistrationLimitReachedError() {
        final String code = AxaConstants.Response.DEVICES_LIMIT_REACHED_CODE;
        return getMsgcd().stream().anyMatch(msgcd -> Objects.equals(msgcd, code));
    }

    public boolean isIncorrectCardNumberError() {
        final String code = AxaConstants.Response.INCORRECT_CARD_NUMBER_CODE;
        return getErrorsEntity()
                .stream()
                .map(ErrorsEntity::getMsgCd)
                .filter(Objects::nonNull)
                .anyMatch(msgcd -> msgcd.equalsIgnoreCase(code));
    }

    public boolean isUnrecognizedBankUser() {
        final String code = AxaConstants.Response.NOT_AN_ACTIVE_BANK_USER;
        return getErrorsEntity()
                .stream()
                .map(ErrorsEntity::getMsgCd)
                .filter(Objects::nonNull)
                .anyMatch(msgcd -> msgcd.equalsIgnoreCase(code));
    }

    /** 16-digit string -- unrelated to the 8-digit challenge the server sent initially. */
    public String getRegisterChallenge() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getChallenge)
                .orElseThrow(IllegalStateException::new);
    }
}
