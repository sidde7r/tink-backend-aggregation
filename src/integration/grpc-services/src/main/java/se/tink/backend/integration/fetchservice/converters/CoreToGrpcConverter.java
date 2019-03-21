package se.tink.backend.integration.fetchservice.converters;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.integration.api.models.AgentAccountState;
import se.tink.backend.integration.api.models.Amount;
import se.tink.backend.integration.api.models.HolderName;
import se.tink.backend.integration.api.models.IntegrationAccount;
import se.tink.backend.integration.api.rpc.CheckingAccountsResponse;

public class CoreToGrpcConverter {
    public static CheckingAccountsResponse convert(FetchAccountsResponse fetchAccountResponse) {

        CheckingAccountsResponse.Builder builder = CheckingAccountsResponse.newBuilder();

        fetchAccountResponse.getAccounts().forEach(
                coreAccount -> {
                    builder.addAccounts(IntegrationAccount
                            .newBuilder()
                            .setName(coreAccount.getName())
                            .setAccountNumber(coreAccount.getAccountNumber())
                            .setBalance(Amount.getDefaultInstance())
                            .setAvailableCredit(Amount.getDefaultInstance())
                            .setUniqueIdentifier(coreAccount.getBankId()) // CHECK: is this the unique idenfier
                            .setHolderName(HolderName.getDefaultInstance())
                            .setAgentAccountState(AgentAccountState.getDefaultInstance())
                            .setAccountIdentifiers(coreAccount.getIdentifiersDeserialized())
                            .build());
                }
        );

        return builder.build();
    }
}
