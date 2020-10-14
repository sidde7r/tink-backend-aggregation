package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity;

import com.nimbusds.jose.JWSObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity.entities.IdentityEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class BancoPostaIdentityFetcher implements IdentityDataFetcher {
    private final BancoPostaApiClient apiClient;

    @SneakyThrows
    @Override
    public IdentityData fetchIdentityData() {
        String identityDataJweResponse = apiClient.fetchIdentityData();
        JSONObject json = JWSObject.parse(identityDataJweResponse).getPayload().toJSONObject();
        IdentityEntity identityData =
                SerializationUtils.deserializeFromString(json.toJSONString(), IdentityEntity.class);

        return IdentityData.builder()
                .setSsn(identityData.getClaims().getSsn())
                .addFirstNameElement(identityData.getClaims().getFirstName())
                .addSurnameElement(identityData.getClaims().getSurname())
                .setDateOfBirth(identityData.getClaims().getBirthDate())
                .build();
    }
}
