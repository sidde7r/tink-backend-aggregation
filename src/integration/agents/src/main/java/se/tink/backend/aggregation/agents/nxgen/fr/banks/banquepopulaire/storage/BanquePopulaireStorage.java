package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AppConfigDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.BankResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity.UserIdentityDataDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BanquePopulaireStorage extends BpceStorage {

    private static final String BANK_SHORT_ID = "BANK_SHORT_ID";
    private static final String BANK_RESOURCE = "BANK_RESOURCE";
    private static final String APP_CONFIG = "APP_CONFIG";
    private static final String USER_IDENTITY_DATA = "USER_IDENTITY_DATA";
    private static final String AUX_ACCESS_TOKEN = "AUX_ACCESS_TOKEN";

    public BanquePopulaireStorage(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    public String getBankShortId() {
        return getOrThrowException(BANK_SHORT_ID, String.class);
    }

    public void storeBankShortId(String bankShortId) {
        persistentStorage.put(BANK_SHORT_ID, bankShortId);
    }

    public BankResourceDto getBankResource() {
        return getOrThrowException(BANK_RESOURCE, BankResourceDto.class);
    }

    public void storeBankResource(BankResourceDto bankResourceDto) {
        persistentStorage.put(BANK_RESOURCE, bankResourceDto);
    }

    public AppConfigDto getAppConfig() {
        return getOrThrowException(APP_CONFIG, AppConfigDto.class);
    }

    public void storeAppConfig(AppConfigDto appConfigDto) {
        persistentStorage.put(APP_CONFIG, appConfigDto);
    }

    public UserIdentityDataDto getUserIdentityDataDto() {
        return getOrThrowException(USER_IDENTITY_DATA, UserIdentityDataDto.class);
    }

    public void storeUserIdentityDataDto(UserIdentityDataDto userIdentityDataDto) {
        persistentStorage.put(USER_IDENTITY_DATA, userIdentityDataDto);
    }

    @SuppressWarnings("unused")
    public OAuth2Token getAuxAccessToken() {
        return getOrThrowException(AUX_ACCESS_TOKEN, OAuth2Token.class);
    }

    public void storeAuxAccessToken(OAuth2Token oAuth2Token) {
        persistentStorage.put(AUX_ACCESS_TOKEN, oAuth2Token);
    }
}
