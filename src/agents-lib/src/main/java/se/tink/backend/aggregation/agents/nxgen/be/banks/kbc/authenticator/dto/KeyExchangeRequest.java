package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class KeyExchangeRequest {
    private TypeValuePair companyId;
    private TypeValuePair appFamily;
    private TypeValuePair appKey;

    private KeyExchangeRequest(TypeValuePair companyId, TypeValuePair appFamily, TypeValuePair appKey) {
        this.companyId = companyId;
        this.appFamily = appFamily;
        this.appKey = appKey;
    }

    public static KeyExchangeRequest create(TypeValuePair companyId, TypeValuePair appFamily, TypeValuePair appKey) {
        return new KeyExchangeRequest(companyId, appFamily, appKey);
    }

    public static KeyExchangeRequest createWithStandardTypes(String companyId, String appFamily, String appKey) {
        return create(
                TypeValuePair.createText(companyId),
                TypeValuePair.createText(appFamily),
                TypeValuePair.createText(appKey));
    }

    public TypeValuePair getCompanyId() {
        return companyId;
    }

    public TypeValuePair getAppFamily() {
        return appFamily;
    }

    public TypeValuePair getAppKey() {
        return appKey;
    }
}
