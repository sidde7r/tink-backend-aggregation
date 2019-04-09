package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.paginated;

import com.google.api.client.util.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.OperationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.OperationListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationSummaryResponse implements TransactionKeyPaginatorResponse<String> {

    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    private Date date;

    @XmlElement(name = "operations_list")
    private OperationListEntity operations;

    public String getReturnCode() {
        return returnCode;
    }

    public Date getDate() {
        return date;
    }

    public OperationListEntity getOperations() {
        return operations;
    }

    @Override
    public String nextKey() {
        return Optional.ofNullable(operations)
                .map(o -> o.getRecoveryKey())
                .orElse(EuroInformationConstants.EMPTY_RECOVERY_KEY);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        List<Transaction> transactions = Lists.newArrayList();

        Optional.ofNullable(operations).map(o -> o.getTransactions())
                .orElse(Collections.emptyList()).stream()
                .map(OperationEntity::toTransaction)
                .forEach(transactions::add);

        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                Optional.ofNullable(operations).map(o -> o.getRecoveryKey()).orElse(null) != null);
    }
}
