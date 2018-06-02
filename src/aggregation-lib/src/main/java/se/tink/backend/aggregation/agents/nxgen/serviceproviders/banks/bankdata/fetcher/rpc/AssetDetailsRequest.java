package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetDetailsRequest {

    private String assetType;
    private String depositRegNo;
    private String depositNo;
    private String securityId;

    public AssetDetailsRequest setAssetType(String assetType) {
        this.assetType = assetType;
        return this;
    }

    public AssetDetailsRequest setDepositRegNo(String depositRegNo) {
        this.depositRegNo = depositRegNo;
        return this;
    }

    public AssetDetailsRequest setDepositNo(String depositNo) {
        this.depositNo = depositNo;
        return this;
    }

    public AssetDetailsRequest setSecurityId(String securityId) {
        this.securityId = securityId;
        return this;
    }
}
