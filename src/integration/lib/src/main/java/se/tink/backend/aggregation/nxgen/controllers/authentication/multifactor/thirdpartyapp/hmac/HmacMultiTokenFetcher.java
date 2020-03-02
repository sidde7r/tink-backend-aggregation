package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.MultiTokenFetcher;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

public class HmacMultiTokenFetcher extends MultiTokenFetcher<HmacToken, HmacMultiToken> {

    public HmacMultiTokenFetcher(
            AccessTokenFetchHelper<HmacToken> accessTokenFetchHelper,
            HmacMultiTokenStorage accessTokenStorage,
            AccessCodeStorage accessCodeStorage) {
        super(accessTokenFetchHelper, accessTokenStorage, accessCodeStorage, HmacMultiToken::new);
    }
}
