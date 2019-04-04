package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity.PayeeAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc.PayeesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingTransferDestinationFetcher implements TransferDestinationFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(StarlingTransferDestinationFetcher.class);


    private final StarlingApiClient apiClient;

    public StarlingTransferDestinationFetcher(StarlingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        LOGGER.info("Calling fetchTransferDestinationsFor");

//        PayeesResponse payeesRespose = apiClient.fetchPayees();

        Map<Account, List<TransferDestinationPattern>> destinations = new TransferDestinationPatternBuilder()
                .setTinkAccounts(accounts)
//                .setDestinationAccounts(getDestinationAccounts(payeesRespose.getPayees()))
                .setDestinationAccounts(getSourceAccounts(accounts))
                .setSourceAccounts(getSourceAccounts(accounts))
                .addMultiMatchPattern(AccountIdentifier.Type.SORT_CODE, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SORT_CODE, SortCodeIdentifier.class)
                .build();

        return new TransferDestinationsResponse(destinations);
    }

    private static List<PayeeAccountEntity.PayeeGeneralAccount> getDestinationAccounts(Collection<PayeeEntity> payeeEntities) {
        return payeeEntities.stream()
                .map(PayeeEntity::streamAccounts)
                .map(StarlingTransferDestinationFetcher::getDestinationAccounts) // gets stream<list<>>
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);  // flatten to list<>
    }

    private static List<PayeeAccountEntity.PayeeGeneralAccount> getDestinationAccounts(Stream<PayeeAccountEntity> payeeAccounts) {
        return payeeAccounts
                .map(PayeeAccountEntity::toGeneralAccountEntity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static List<? extends GeneralAccountEntity> getSourceAccounts(Collection<Account> accounts) {
        LOGGER.info("Getting source accounts from " + SerializationUtils.serializeToString(accounts));
        return accounts.stream()
                .map(GeneralAccountEntityImpl::createFromCoreAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
