package se.tink.backend.aggregation.agents.creditcards.rikskortet;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.creditcards.rikskortet.soap.AccountDetails;
import se.tink.backend.aggregation.agents.creditcards.rikskortet.soap.ArrayOfTransactionDetails;
import se.tink.backend.aggregation.agents.creditcards.rikskortet.soap.MobileWSV2;
import se.tink.backend.aggregation.agents.creditcards.rikskortet.soap.MobileWSV2Soap;
import se.tink.backend.aggregation.agents.creditcards.rikskortet.soap.TransactionDetails;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.soap.SOAPLoggingHandler;
import se.tink.backend.aggregation.agents.utils.soap.SOAPUserAgentHandler;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

public class RikskortetAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    private static final File WSDL_FILE = new File("data/agents/rikskortet.wsdl");
    private boolean hasRefreshed = false;
    SOAPUserAgentHandler userAgentHandler;

    private static void addSoapHandlers(MobileWSV2Soap service, String aggregator) {
        Binding binding = ((BindingProvider) service).getBinding();

        @SuppressWarnings("rawtypes")
        List<Handler> handlerChain = binding.getHandlerChain();

        if (handlerChain == null) {
            handlerChain = Lists.newArrayList();
        }

        handlerChain.add(new SOAPUserAgentHandler(aggregator));
        handlerChain.add(new SOAPLoggingHandler());
        binding.setHandlerChain(handlerChain);
    }

    private static String cleanAmount(String amount) {
        return amount.replace(" kr", "");
    }

    private MobileWSV2Soap service;
    private Credentials credentials;
    private AccountDetails ad;

    public RikskortetAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        userAgentHandler = new SOAPUserAgentHandler(DEFAULT_USER_AGENT);

        try {
            credentials = request.getCredentials();
            service = new MobileWSV2(WSDL_FILE.toURI().toURL()).getMobileWSV2Soap();

            addSoapHandlers(service, DEFAULT_USER_AGENT);
        } catch (Exception e) {
            log.error("Could not initialize client", e);
        }
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;


        // Construct the account.

        Account account = new Account();

        account.setName("Rikskortet");
        account.setType(AccountTypes.CREDIT_CARD);
        account.setBankId("rikskortet");
        account.setBalance(ad.getBalance().doubleValue());

        // Fetch the latest transactions.

        ArrayOfTransactionDetails transactionDetails = service.getTransactions(credentials.getUsername(),
                credentials.getPassword()).getYear();

        List<Transaction> transactions = Lists.newArrayList();

        for (TransactionDetails td : transactionDetails.getTransactionDetails()) {
            Transaction t = new Transaction();

            t.setDescription(td.getDescription().trim());
            t.setAmount(AgentParsingUtils.parseAmount(cleanAmount(td.getAmount())));
            t.setDate(AgentParsingUtils.parseDate(td.getDate(), true));

            if (t.getAmount() < 0) {
                t.setType(TransactionTypes.CREDIT_CARD);
            }

            transactions.add(t);
        }

        financialDataCacher.updateTransactions(account, transactions);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        // Authenticate.

        ad = service.getAccountDetails(credentials.getUsername(), credentials.getPassword());

        // Handle authentication errors.
        switch (ad.getErrorCode()) {
        case 1:
            return true;
        case -1:
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        default:
            throw new IllegalStateException(String.format("Rikskortet: unknown error code: %d", ad.getErrorCode()));
        }
    }

    @Override
    public void logout() throws Exception {
        // TODO Implement.
    }


}
