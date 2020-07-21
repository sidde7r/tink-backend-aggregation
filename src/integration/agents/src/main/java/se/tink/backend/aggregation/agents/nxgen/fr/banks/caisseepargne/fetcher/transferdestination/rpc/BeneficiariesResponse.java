package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.entity.ItemsItem;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.entity.TransferCreditorIdentity;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BeneficiariesResponse {
    @JsonProperty("items")
    private List<ItemsItem> items;

    @JsonIgnore
    public List<GeneralAccountEntity> getDestinationAccounts() {
        return items.stream()
                .map(ItemsItem::getTransferCreditorIdentity)
                .filter(TransferCreditorIdentity::isActivated)
                .filter(TransferCreditorIdentity::isDestinationAccount)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<TransferCreditorIdentity> getOwnAccounts() {
        return items.stream()
                .map(ItemsItem::getTransferCreditorIdentity)
                .filter(TransferCreditorIdentity::isOwnAccount)
                .collect(Collectors.toList());
    }
}
