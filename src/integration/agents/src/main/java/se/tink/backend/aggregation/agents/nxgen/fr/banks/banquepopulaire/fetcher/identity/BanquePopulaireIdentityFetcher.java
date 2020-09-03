package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.identity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity.PersonIdDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity.UserIdentityDataDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class BanquePopulaireIdentityFetcher implements IdentityDataFetcher {

    private final BanquePopulaireStorage banquePopulaireStorage;

    @Override
    public IdentityData fetchIdentityData() {
        final UserIdentityDataDto userIdentityDataDto =
                banquePopulaireStorage.getUserIdentityDataDto();
        final PersonIdDto personIdDto = userIdentityDataDto.getPersonId();

        return IdentityData.builder()
                .addFirstNameElement(personIdDto.getFirstName())
                .addSurnameElement(personIdDto.getSurname())
                .setDateOfBirth(null)
                .build();
    }
}
