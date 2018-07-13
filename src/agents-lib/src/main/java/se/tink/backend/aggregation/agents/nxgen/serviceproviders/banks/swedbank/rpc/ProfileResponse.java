package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBasePredicates;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileResponse {
    private String userId;
    private boolean hasSwedbankProfile;
    private boolean hasSavingbankProfile;
    private List<BankEntity> banks;

    public String getUserId() {
        return userId;
    }

    public boolean isHasSwedbankProfile() {
        return hasSwedbankProfile;
    }

    public boolean isHasSavingbankProfile() {
        return hasSavingbankProfile;
    }

    public List<BankEntity> getBanks() {
        return banks;
    }

    @JsonIgnore
    public LinkEntity getNext(String bankId) {
        List<BankEntity> bankList = Optional.ofNullable(banks).orElseThrow(IllegalStateException::new);
        return bankList.stream()
                .filter(SwedbankBasePredicates.filterBankId(bankId))
                .map(BankEntity::getPrivateProfile)
                .map(PrivateProfileEntity::getLinks)
                .map(LinksEntity::getNext)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
