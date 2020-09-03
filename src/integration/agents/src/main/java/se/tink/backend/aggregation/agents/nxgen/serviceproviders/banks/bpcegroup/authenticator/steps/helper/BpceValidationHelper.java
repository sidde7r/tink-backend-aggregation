package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.StepDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidationUnitResponseItemDto;

public class BpceValidationHelper {

    public String getValidationId(AuthTransactionResponseDto authTransactionResponseDto) {
        if (Objects.nonNull(authTransactionResponseDto.getStep())) {
            return getValidationIdFromStep(authTransactionResponseDto.getStep());
        } else {
            return getValidationIdFromStep(authTransactionResponseDto);
        }
    }

    public String getValidationUnitId(
            AuthTransactionResponseDto authTransactionResponseDto, String validationId) {
        if (Objects.nonNull(authTransactionResponseDto.getStep())) {
            return getValidationUnitIdFromStep(authTransactionResponseDto.getStep(), validationId);
        } else {
            return getValidationUnitIdFromStep(authTransactionResponseDto, validationId);
        }
    }

    public String getValidationIdFromStep(StepDto stepDto) {
        final Map<String, List<ValidationUnitResponseItemDto>> validationUnit =
                stepDto.getValidationUnits().get(0);

        return validationUnit.keySet().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Validation unit was not found."));
    }

    public String getValidationUnitIdFromStep(StepDto stepDto, String validationId) {
        final Map<String, List<ValidationUnitResponseItemDto>> validationUnit =
                stepDto.getValidationUnits().get(0);

        return validationUnit.get(validationId).get(0).getId();
    }
}
