package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.QualificationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc.PositionWalletResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BankiaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger log = LoggerFactory.getLogger(BankiaInvestmentFetcher.class);

    private final BankiaApiClient apiClient;

    private static final int PAGE_LIMIT = 3;

    public BankiaInvestmentFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccountEntity> investments = apiClient.getInvestments();
        if (investments != null) {
            return investments.stream()
                    .filter(
                            account -> {
                                if (account.isAccountTypeInvestment()) {
                                    return true;
                                }

                                log.info(
                                        "{} Unknown account type or missing fields: {}",
                                        BankiaConstants.Logging.UNKNOWN_ACCOUNT_TYPE.toString(),
                                        account.getContract().getProductCode(),
                                        SerializationUtils.serializeToString(account));

                                return false;
                            })
                    .map(this::fetchInvestmentAccount)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    InvestmentAccount fetchInvestmentAccount(InvestmentAccountEntity account) {
        ContractEntity contract = account.getContract();

        List<Instrument> instruments = new ArrayList<>();

        // We don't have an ambassador with more than one page of stocks, so this pagination implementation is
        // speculative based on the available fields in the entities
        PositionWalletResponse response = null;
        String resumePoint = BankiaConstants.Default.EMPTY_RESUME_POINT;
        int pagesFetched = 0;

        try {
            do {
                response = apiClient.getPositionsWallet(contract.getIdentifierProductContractInternal(), resumePoint);
                resumePoint = response.getDataRedialExit();
                pagesFetched++;

                instruments.addAll(response.getQualificationList().stream()
                        .map(this::mapQualificationToInstrument)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            } while (response.isHasMoreIndicator() && !Strings.isNullOrEmpty(resumePoint) && (pagesFetched < PAGE_LIMIT));
        } catch (HttpResponseException hre) {
            log.error(BankiaConstants.Logging.INSTRUMENT_FETCHING_ERROR.toString(), hre);
            log.info("{} Instrument fetching error. Last successful page: {}",
                    BankiaConstants.Logging.INSTRUMENT_FETCHING_ERROR.toString(),
                    SerializationUtils.serializeToString(response));
            if (pagesFetched == 0) {
                //no instruments were fetched successfully, rethrow the error
                throw hre;
            }
        }

        if (pagesFetched == PAGE_LIMIT) {
            log.info("{} Instrument fetching error. Too many pages. Last page: {}",
                    BankiaConstants.Logging.INSTRUMENT_FETCHING_ERROR.toString(),
                    SerializationUtils.serializeToString(response));
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setInstruments(instruments);
        portfolio.setTotalValue(account.getAvailableBalance().toTinkAmount().doubleValue());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(account.getContract().getIdentifierProductContractInternal());

        return InvestmentAccount.builder(contract.getIdentifierProductContract())
                .setAccountNumber(contract.getIdentifierProductContract())
                .setPortfolios(Collections.singletonList(portfolio))
                .setName(contract.getAlias())
                .setBankIdentifier(contract.getIdentifierProductContractInternal())
                .setCashBalance(Amount.inEUR(0.0))
                .build();
    }

    private Instrument mapQualificationToInstrument(QualificationEntity qualification) {
        Instrument instrument = new Instrument();
        instrument.setTicker(qualification.getTickerCode());
        instrument.setIsin(qualification.getIsin());
        instrument.setQuantity(qualification.getQuantity());
        instrument.setMarketPlace(qualification.getIdentifierMarketplaceGroup());
        instrument.setPrice(qualification.getValuationUnitEUR().toTinkAmount().doubleValue());
        instrument.setUniqueIdentifier(qualification.getIsin());
        instrument.setName(qualification.getTickerCode());

        if (BankiaConstants.InstrumentTypes.STOCK.equals(qualification.getType())) {
            instrument.setType(Instrument.Type.STOCK);
        } else {
            log.info(
                    "{} Unknown instrument type: {}",
                    BankiaConstants.Logging.UNKNOWN_INSTRUMENT_TYPE.toString(),
                    SerializationUtils.serializeToString(qualification));
            instrument.setType(Instrument.Type.OTHER);
        }
        return instrument;
    }
}
