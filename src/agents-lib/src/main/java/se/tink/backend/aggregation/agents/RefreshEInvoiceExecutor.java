package se.tink.backend.aggregation.agents;

/**
 * Interface for executing refreshes of eInvoices.
 * Agent which supports EInvoices shall implement this.
 */
public interface RefreshEInvoiceExecutor {

    /**
     * Fetch eInvoices.
     *
     * @return fetch eInvoices response
     */
    FetchEInvoicesResponse fetchEInvoices();
}
