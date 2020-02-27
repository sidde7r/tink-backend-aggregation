package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class AccountTransactionsRequest extends AbstractRequest<List<BPostBankTransactionDTO>> {

    static final String URL_PATH = "/bpb/services/rest/v2/operations";
    private static final String BODY_TEMPLATE =
            "{\"f\": \"%s\",\"l\": \"%s\",\"sort\": \"-bookingDateTime\",\"accountNumber\": \"%s\"}";
    private final int first;
    private final int last;
    private final String accountNumber;

    public AccountTransactionsRequest(
            BPostBankAuthContext authContext, int first, int last, String iban) {
        super(URL_PATH, authContext);
        this.first = first;
        this.last = last;
        this.accountNumber = iban.substring(4);
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, first, last, accountNumber));
    }

    @Override
    public List<BPostBankTransactionDTO> execute(RequestBuilder requestBuilder)
            throws RequestException {
        return Lists.newArrayList(requestBuilder.post(BPostBankTransactionDTO[].class));
    }
}
