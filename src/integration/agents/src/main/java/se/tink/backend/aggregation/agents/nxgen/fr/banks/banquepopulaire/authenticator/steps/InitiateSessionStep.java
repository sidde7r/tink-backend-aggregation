package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AppConfigDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.BankResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.GeneralConfigurationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

@RequiredArgsConstructor
public class InitiateSessionStep extends AbstractAuthenticationStep {

    public static final String STEP_ID = "initiateSessionStep";

    private final BanquePopulaireApiClient apiClient;

    private final BanquePopulaireStorage banquePopulaireStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request) {
        apiClient.checkUpdateStatus();

        final BankResourceDto bankResourceDto = getBankGeneralConfiguration();
        final AppConfigDto appConfigDto = getAppConfigForTheBank(bankResourceDto);

        apiClient.sendMessageServiceRequest(bankResourceDto);

        banquePopulaireStorage.storeMembershipType(MembershipType.PART);
        banquePopulaireStorage.storeBankResource(bankResourceDto);
        banquePopulaireStorage.storeAppConfig(appConfigDto);

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private BankResourceDto getBankGeneralConfiguration() {
        final String bankShortId = banquePopulaireStorage.getBankShortId();
        final GeneralConfigurationResponseDto generalConfigurationResponseDto =
                apiClient.getGeneralConfig();

        return generalConfigurationResponseDto.getBrand().getBanks().stream()
                .filter(bank -> bank.getId().equals(bankShortId))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Could not find bank configuration for bank id: "
                                                + bankShortId));
    }

    private AppConfigDto getAppConfigForTheBank(BankResourceDto bankResourceDto) {
        return apiClient.getBankConfig(bankResourceDto).getAppConfig();
    }
}
