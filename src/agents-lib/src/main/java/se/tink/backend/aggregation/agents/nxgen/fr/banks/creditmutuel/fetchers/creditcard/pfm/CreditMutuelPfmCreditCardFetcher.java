package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.CreditMutuelApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities.ValueEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.utils.CreditMutuelPmfPredicates;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.utils.CreditMututelPmfCreditCardStringParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditMutuelPfmCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    public static final String SUBTITLE = "subtitle";
    public static final String TITLE = "title";
    public static final String AMOUNT = "AMOUNT";
    public static final String MAX_PAYMENTS_LIMIT = "secondaryValueTitle";
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CreditMutuelPfmCreditCardFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            CreditMutuelPfmCreditCardFetcher.class);
    private final CreditMutuelApiClient apiClient;

    private CreditMutuelPfmCreditCardFetcher(CreditMutuelApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static CreditMutuelPfmCreditCardFetcher create(EuroInformationApiClient apiClient) {
        return new CreditMutuelPfmCreditCardFetcher((CreditMutuelApiClient) apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        CreditCardResponse creditCardResponse = apiClient.requestCreditCardAccountsEndpoint();

        String returnCode = creditCardResponse.getReturnCode();
        if (!EuroInformationUtils.isSuccess(returnCode)) {
            LOGGER.info("Error while fetching credit cards by PFM endpoint: " + EuroInformationErrorCodes
                    .getByCodeNumber(returnCode) + " " + SerializationUtils.serializeToString(creditCardResponse));
            return Collections.emptyList();
        }

        //TODO: Need to double check how multiple cards are handled in the response

        // Parsing Card number
        List<ValueEntity> valueEntityStream = CreditMutuelPmfPredicates.getItemEntitiesFromResponse
                .apply(creditCardResponse).collect(Collectors.toList());
        Optional<ValueEntity> subtitle = valueEntityStream.stream()
                .filter(CreditMutuelPmfPredicates.filterValueEntityByName(SUBTITLE))
                .findFirst();

        String cardNumber = subtitle.map(s -> s.getValue())
                .map(s -> CreditMututelPmfCreditCardStringParsingUtils.parseCreditCardNumber(s))
                .orElseThrow(IllegalStateException::new);

        // Parsing card name
        Stream<ValueEntity> valueEntityForTitleStream = CreditMutuelPmfPredicates.getItemEntitiesFromResponse
                .apply(creditCardResponse);
        Optional<ValueEntity> title = valueEntityForTitleStream
                .filter(CreditMutuelPmfPredicates.filterValueEntityByName(TITLE))
                .findFirst();

        // Parsing card limits
        Stream<SubItemsEntity> subItemsEntityStream = CreditMutuelPmfPredicates.getSubItemsValueStreamFromResponse
                .apply(creditCardResponse);
        Stream<ValueEntity> outputsEntityStream = subItemsEntityStream.flatMap(s -> s.stream())
                .flatMap(s -> s.getOutputs().stream());
        String maximalLimitString = outputsEntityStream
                //TODO: Double check if it should be secondaryValueTitle which contains limit for payments
                //TODO: or valueTitle which contains limit for withdrawals
                .filter(CreditMutuelPmfPredicates.filterValueEntityByName(MAX_PAYMENTS_LIMIT))
                .findFirst().orElseThrow(IllegalStateException::new).getValue();

        Amount maxAmount = CreditMututelPmfCreditCardStringParsingUtils.extractAmountFromString(maximalLimitString);

        // Parsing card balance
        Stream<SubItemsEntity> subItemsEntityStream2 = CreditMutuelPmfPredicates.getSubItemsValueStreamFromResponse
                .apply(creditCardResponse);
        Stream<ValueEntity> outputsEntityStream2 = subItemsEntityStream2.flatMap(s -> s.stream())
                .flatMap(s -> s.getOutputs().stream());
        String amount = outputsEntityStream2
                .filter(CreditMutuelPmfPredicates.filterValueEntityByType(AMOUNT))
                .findFirst().orElseThrow(IllegalStateException::new).getValue();
        Amount balance = EuroInformationUtils.parseAmount(amount);

        CreditCardAccount build = CreditCardAccount.builder(cardNumber)
                .setAccountNumber(cardNumber)
                .setBalance(balance)
                .setAvailableCredit(balance.add(maxAmount))
                .setName(title.map(t -> t.getValue()).orElse("")).build();

        return Collections.singleton(build);
    }
}
