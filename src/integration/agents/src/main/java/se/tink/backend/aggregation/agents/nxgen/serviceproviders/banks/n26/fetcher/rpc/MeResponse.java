package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities.UserInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@SuppressWarnings("unused")
@JsonObject
public class MeResponse {
    private UserInfoEntity userInfo;

    public IdentityData toTinkIdentity() {
        String[] names = userInfo.getBirthNames().split("\\s+");
        String[] surnames = userInfo.getLastNames().split("\\s+");
        LocalDate birthDate = userInfo.getBirthDate();
        if (names.length == 0 || surnames.length == 0) {
            throw new IllegalStateException("Cannot fetch identity data, names are not present");
        }
        IdentityData.FirstNameElementBuilderStep builder =
                IdentityData.builder().addFirstNameElement(names[0]);

        for (int i = 1; i < names.length; i++) {
            builder = builder.addFirstNameElement(names[i]);
        }

        IdentityData.SurnameElementBuilderStep surnameStep = builder.addSurnameElement(surnames[0]);

        for (int i = 1; i < surnames.length; i++) {
            surnameStep = surnameStep.addSurnameElement(surnames[i]);
        }

        return surnameStep.setDateOfBirth(userInfo.getBirthDate()).build();
    }
}
