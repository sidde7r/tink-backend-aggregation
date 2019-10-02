/**
 * A custom agent that would fetch transactions from Nordea instead of Nordea pushing their
 * transaction to Tinks connector.
 *
 * <p>This agent is a temporary solution until Nordea push transactions to Tink connector (using a
 * service they will create named Account Transaction Store)
 *
 * <p>This agent should only be available for Nordea (neston)
 */
package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;
