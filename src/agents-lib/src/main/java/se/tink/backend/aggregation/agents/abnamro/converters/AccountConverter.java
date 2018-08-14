package se.tink.backend.aggregation.agents.abnamro.converters;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.common.mapper.CoreAccountTypesMapper;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;

public class AccountConverter {
    public List<Account> convert(List<PfmContractEntity> contracts) {
        return contracts.stream().map(this::toAccount).collect(Collectors.toList());
    }

    private Account toAccount(PfmContractEntity contract) {
        Account account = new Account();
        account.setBankId(contract.getContractNumber());
        account.setName(contract.getName());
        account.setType(CoreAccountTypesMapper.toAggregation(AbnAmroUtils.getAccountType(contract.getProductGroup())));

        if (account.getType() == AccountTypes.CREDIT_CARD) {
            return toCreditCardAccount(account, contract);
        } else {
            return toNonCreditCardAccount(account, contract);
        }
    }

    private Account toCreditCardAccount(Account account, PfmContractEntity contract) {
        account.setName(contract.getName().replaceFirst("ABN AMRO\\s*", ""));
        account.setAccountNumber(AbnAmroUtils.maskCreditCardContractNumber(contract.getContractNumber()));

        return account;
    }

    private Account toNonCreditCardAccount(Account account, PfmContractEntity contract) {
        account.setName(contract.getName());
        account.setBalance(contract.getBalance().getAmount());
        account.setAccountNumber(AbnAmroUtils.prettyFormatIban(contract.getAccountNumber()));

        return account;
    }
}
