package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInfo;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInformationListItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.core.Amount;

public class GetAccountInformationListResponse {
    private Envelope envelope;

    public GetAccountInformationListResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    public AccountInfo getAccountInfoByAccountNumber(final String accountNumber) {
        final List<AccountInfo> accountNumbers = envelope.getBody()
                .getGetAccountInformationListResponseEntity()
                .getOk()
                .getAccountInformationListItemList().stream()
                .map(AccountInformationListItem::getAccountInfo)
                .filter(accountInfo -> accountInfo.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
        return accountNumbers.get(0); // TODO Assert one and only one element
    }

    public Amount getBalanceFromAccountNumber(final String accountNumber) {
        final AccountInfo accountInfo = getAccountInfoByAccountNumber(accountNumber);
        final double balance = accountInfo.getAmountEntity().getAmount();
        return new Amount(accountInfo.getAccountCurrency().trim(), balance);
    }
}
