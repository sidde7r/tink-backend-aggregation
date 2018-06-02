package se.tink.backend.grpc.v1.converter.account;

import java.util.List;
import se.tink.backend.core.Account;
import se.tink.grpc.v1.models.Accounts;

public class AccountsGrpcConverter {
    private CoreAccountToGrpcAccountConverter coreAccountToGrpcAccountConverter;

    public AccountsGrpcConverter(
            CoreAccountToGrpcAccountConverter coreAccountToGrpcAccountConverter) {
        this.coreAccountToGrpcAccountConverter = coreAccountToGrpcAccountConverter;
    }

    public Accounts convertFrom(List<Account> input) {
        List<se.tink.grpc.v1.models.Account> accounts = coreAccountToGrpcAccountConverter.convertFrom(input);
        return Accounts.newBuilder().addAllAccount(accounts).build();
    }
}
