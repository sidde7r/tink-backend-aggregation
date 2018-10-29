package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Account;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Card;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.ClientManager;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.IdentificationLimit;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.NotificationTokens;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Settings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class GetUserDataResponse extends BaseResponse {

	@JsonProperty("settings")
	private Settings settings;

	@JsonProperty("identification")
	private int identification;

	@JsonProperty("cards")
	private List<Card> cards;

	@JsonProperty("client_manager")
	private ClientManager clientManager;

	@JsonProperty("require_data_update")
	private boolean requireDataUpdate;

	@JsonProperty("show_pension")
	private boolean showPension;

	@JsonProperty("identification_limit")
	private IdentificationLimit identificationLimit;

	@JsonProperty("accounts")
	private List<Account> accounts;

	@JsonProperty("notification_tokens")
	private NotificationTokens notificationTokens;

	public Settings getSettings(){
		return settings;
	}

	public int getIdentification(){
		return identification;
	}

	public List<Card> getCards(){
		return cards;
	}

	public ClientManager getClientManager(){
		return clientManager;
	}

	public boolean isRequireDataUpdate(){
		return requireDataUpdate;
	}

	public boolean isShowPension(){
		return showPension;
	}

	public IdentificationLimit getIdentificationLimit(){
		return identificationLimit;
	}

	public Collection<TransactionalAccount> getAccounts(final String currentUser,
														final String currency,
														final int baseCurrencyId) {
		Collection<TransactionalAccount> result = new HashSet<>();
		// TODO use collections.stream here
		for (Account account : accounts) {
            Amount balance = new Amount(currency, account.getBalance(baseCurrencyId));
            if (AsLhvConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(account.getType())) {
                TransactionalAccount transactionalAccount =
                        TransactionalAccount.builder(account.getType(), account.getIban(), balance)
                                .setName(account.getName().isEmpty() ? account.getIban() : account.getName())
                                .setHolderName(new HolderName(currentUser))
                                .setAccountNumber(account.getPortfolioId())
                                .build();
                result.add(transactionalAccount);
            }
        }
		return result;
	}

	public NotificationTokens getNotificationTokens(){
		return notificationTokens;
	}
}