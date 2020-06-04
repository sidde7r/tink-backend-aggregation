package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DefaultResponse {
    protected List<ErrorEntity> errors;
    protected List<WarningEntity> warnings;
    protected List<InfoEntity> infos;

    public List<ErrorEntity> getErrors() {
        return errors;
    }

    public List<WarningEntity> getWarnings() {
        return warnings;
    }

    public List<InfoEntity> getInfos() {
        return infos;
    }

    public boolean isResponseOK() {
        return Optional.ofNullable(infos).orElse(Collections.emptyList()).stream()
                .map(InfoEntity::getMessage)
                .anyMatch("OK"::equals);
    }

    public List<String> getAllErrorCodes() {
        return Optional.ofNullable(errors).orElse(Collections.emptyList()).stream()
                .map(ErrorEntity::getCode)
                .collect(Collectors.toList());
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public String getErrorString() {
        return errors.stream().map(ErrorEntity::toString).collect(Collectors.joining("\n"));
    }
}
