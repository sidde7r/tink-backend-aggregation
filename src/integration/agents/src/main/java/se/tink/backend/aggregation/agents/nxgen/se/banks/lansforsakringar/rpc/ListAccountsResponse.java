package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.entity.MainAndCoAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse {
    private List<MainAndCoAccountsEntity> mainAndCoAccounts;

    public List<MainAndCoAccountsEntity> getMainAndCoAccounts() {
        return mainAndCoAccounts;
    }


//    @JsonIgnore
//    public Collection<Optional<TransactionalAccount>> toTinkAccounts() {
//        return mainAndCoAccounts.stream()
//                .map(MainAndCoAccountsEntity::toTinkAccount)
//                .filter(Optional::isPresent)
//                .collect(Collectors.toList());
//    }

    //  private List<DispositionAccountsEntity> dispositionAccounts;
    //  private List<CardsEntity> cards;
    //  private List<ErrorMessagesEntity> errorMessages;

}
