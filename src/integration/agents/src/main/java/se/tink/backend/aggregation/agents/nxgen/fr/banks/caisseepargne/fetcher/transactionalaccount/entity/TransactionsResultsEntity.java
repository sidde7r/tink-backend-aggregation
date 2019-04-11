package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@JacksonXmlRootElement
public class TransactionsResultsEntity implements TransactionKeyPaginatorResponse<String> {

    @JacksonXmlProperty(localName = "IndicateurNav")
    private String navIndicator;

    @JacksonXmlProperty(localName = "BufferSuite")
    private String paginationKey;

    @JacksonXmlProperty(localName = "Nb_Op")
    private String nbOp;

    @JacksonXmlProperty(localName = "Nb_Op_Tot")
    private String nbOpTot;

    @JacksonXmlProperty(localName = "IndiceExaustif")
    private String exaustifIndex;

    @JacksonXmlElementWrapper(localName = "ListHistoCpt")
    private List<TransactionEntity> transactions;

    @JacksonXmlProperty(localName = "MttSoldeCompte")
    private String mttBalanceAccount;

    @JacksonXmlProperty(localName = "SensSoldeCompte")
    private String accountBalanceAccount;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!Strings.isNullOrEmpty(paginationKey));
    }

    @Override
    public String nextKey() {
        return paginationKey;
    }
}
