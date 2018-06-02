package se.tink.backend.common.search.cluster;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionCluster;
import se.tink.backend.utils.GlobMatch;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

/**
 * Implementation of the Lundgren-Hedberg clustering algorithm.
 * 
 */
public class LundgrenHedbergClusterer {
    private static Comparator<Transaction> ABSOLUTE_COMPARATOR = (t1, t2) -> {
        // Minus sign for descending order.
        return -Double.compare(Math.abs(t1.getAmount()), Math.abs(t2.getAmount()));
    };

    private static final LogUtils log = new LogUtils(LundgrenHedbergClusterer.class);
    private static final GlobMatch matcher = new GlobMatch();
    private static final int MAXIMUM_TRANSACTIONS_TO_CLUSTER = 75;

    /**
     * Calculate the actual distance between two transactions.
     * 
     * @param transaction1
     * @param transaction2
     * @return
     */
    private static double calculateTransactionDistance(Transaction transaction1, Transaction transaction2) {
        if ((transaction1.getAmount() > 0 && transaction2.getAmount() < 0)
                || (transaction1.getAmount() < 0 && transaction2.getAmount() > 0)) {
            return Double.POSITIVE_INFINITY;
        }

        return 1 / StringUtils.getJaroWinklerDistance(transaction1.getDescription().toLowerCase(), transaction2
                .getDescription().toLowerCase());
    }

    /**
     * Calculate the distance matrix for all the transactions. Use an inverted
     * Jaro-Winker value as distance function in order to fit the clustering
     * algorithm.
     * 
     * @param transactionIds
     *            The transaction IDs.
     * @param transactionsById
     *            The transactions (keyed by ID).
     * @return
     */
    private static double[][] calculateTransactionDistances(ArrayList<String> transactionIds,
            ImmutableMap<String, Transaction> transactionsById) {
        double[][] transactionDistances = new double[transactionIds.size()][transactionIds.size()];

        for (int i = 0; i < transactionIds.size(); i++) {
            for (int j = 0; j < transactionIds.size(); j++) {
                transactionDistances[i][j] = calculateTransactionDistance(transactionsById.get(transactionIds.get(i)),
                        transactionsById.get(transactionIds.get(j)));
            }
        }

        return transactionDistances;
    }

    private DefaultClusteringAlgorithm clusteringAlgorithm;

    private double minJaroWinklerThreashold;

    private int minNbrOfItemsInCluster;

    private int minNbrOfRulesCharsInCluster;
    public LundgrenHedbergClusterer(int minNbrOfItemsInCluster, int minNbrOfRulesCharsInCluster,
            double minJaroWinklerThreashold) {
        this.minNbrOfItemsInCluster = minNbrOfItemsInCluster;
        this.minNbrOfRulesCharsInCluster = minNbrOfRulesCharsInCluster;
        this.minJaroWinklerThreashold = minJaroWinklerThreashold;

        this.clusteringAlgorithm = new DefaultClusteringAlgorithm();
    }

    /**
     * Checks that all items in cluster match the matchPattern
     */
    private String checkAllMatch(String pattern, ArrayList<String> clusterWords, int thisNumberOfChar, int startIndex) {
        String matchPattern = pattern.substring(startIndex, startIndex + thisNumberOfChar);

        if (startIndex == 0) {
            matchPattern = matchPattern + "*";
        } else {
            matchPattern = "*" + matchPattern + "*";
        }

        for (String word : clusterWords) {
            if (!matcher.match(word, matchPattern)) {
                return null;
            }
        }
        return matchPattern;
    }

    /**
     * Evaluate whether a cluster is valid by checking all the supplied
     * constraints supplied to the constructor.
     * 
     * @param cluster
     *            The cluster.
     * @param transactionsById
     *            The transactions (keyed by ID).
     * @param transactionIds
     *            The transaction IDs.
     * @param transactionDistances
     *            The distance matrix.
     * @return
     */
    private List<TransactionCluster> evaluateCluster(Cluster cluster, Map<String, Transaction> transactionsById,
            ArrayList<String> transactionIds, double[][] transactionDistances) {
        List<TransactionCluster> transactionClusters = Lists.newArrayList();

        // Get all the cluster's transactions.

        List<Transaction> transactions = fetchClusterTransactions(cluster, transactionsById);

        double minJaroWinklerInCluster = 1;
        int nbrOfTransactionsInCluster = transactions.size();

        // Get the minimum Jaro-Winkler distance within the cluster.

        for (Transaction transaction : transactions) {
            for (Transaction otherTransaction : transactions) {
                double distance = transactionDistances[transactionIds.indexOf(transaction.getId())][transactionIds
                        .indexOf(otherTransaction.getId())];

                // Use the inverted value from the distance matrix (as it's
                // actually the inverted Jaro-Winkler distance).

                minJaroWinklerInCluster = Math.min(minJaroWinklerInCluster, 1 / distance);
            }
        }

        // If we satisfy all our cluster constraints, construct and add the
        // cluster. If not, continue to search in child clusters.

        if (nbrOfTransactionsInCluster >= minNbrOfItemsInCluster && minJaroWinklerInCluster >= minJaroWinklerThreashold
                || cluster.getChildren() == null || cluster.getChildren().isEmpty()) {
            
            TransactionCluster transactionCluster = new TransactionCluster();
            
            transactionCluster.setDescription(findClusterPattern(transactions));

            Collections.sort(transactions, ABSOLUTE_COMPARATOR);

            transactionCluster.setTransactions(transactions);

            if (transactionCluster.getDescription().length() > minNbrOfRulesCharsInCluster) {
                transactionClusters.add(transactionCluster);
            } else {
                for (Cluster cl : cluster.getChildren()) {
                    transactionClusters.addAll(evaluateCluster(cl, transactionsById, transactionIds,
                            transactionDistances));
                }
            }
        } else {
            for (Cluster cl : cluster.getChildren()) {
                transactionClusters.addAll(evaluateCluster(cl, transactionsById, transactionIds, transactionDistances));
            }
        }

        return transactionClusters;
    }

    /**
     * Recursively fetch the transactions for the entire cluster.
     * 
     * @param cluster
     *            The transaction cluster.
     * @param transactionsById
     *            The transactions (keyed by ID).
     * @return
     */
    private List<Transaction> fetchClusterTransactions(Cluster cluster, Map<String, Transaction> transactionsById) {
        List<Transaction> transactions = Lists.newArrayList();

        if (cluster.getChildren() == null || cluster.getChildren().size() == 0) {
            transactions.add(transactionsById.get(cluster.getName()));
        } else {
            for (Cluster child : cluster.getChildren()) {
                transactions.addAll(fetchClusterTransactions(child, transactionsById));
            }
        }

        return transactions;
    }

    /**
     * Finds the most significant glob-pattern for a list of transactions.
     * 
     * @param list
     *            The transaction list.
     * @return The pattern.
     */
    private String findClusterPattern(List<Transaction> list) {
        if (list.size() == 0) {
            return "";
        }

        ArrayList<String> clusterWords = new ArrayList<String>();
        for (Transaction unit : list) {
            clusterWords.add(unit.getDescription());
        }

        if (clusterWords.size() == 1) {
            return clusterWords.get(0);
        }

        Collections.sort(list, (o1, o2) -> Ints.compare(o1.getDescription().length(), o2.getDescription().length()));

        String pattern = list.get(0).getDescription();
        int length = pattern.length();
        String globalBestPatter = "";
        int globalBestPatternCount = 0;
        String bestPattern = "";
        int bestPatternCount = 0;
        int thisNumberOfChar = 1;
        String matchPattern = null;
        int startIndex = 0;

        while (startIndex < length) {
            while (thisNumberOfChar <= length - startIndex) {
                // check forward
                matchPattern = checkAllMatch(pattern, clusterWords, thisNumberOfChar, startIndex);
                if (matchPattern != null) {
                    bestPattern = matchPattern;
                    bestPatternCount = thisNumberOfChar;
                    thisNumberOfChar++;
                } else {
                    break;
                }
            }
            if (bestPatternCount > globalBestPatternCount) {
                globalBestPatternCount = bestPatternCount;
                globalBestPatter = bestPattern;
            }
            startIndex++;
            thisNumberOfChar = 1;
        }
        if (numberOfPaternChars(globalBestPatter) >= minNbrOfRulesCharsInCluster
                && list.size() >= minNbrOfItemsInCluster) {
            return globalBestPatter;
        }
        return "";
    }

    /**
     * Construct the transaction clusters.
     * 
     * @param transactions
     *            The list of transactions.
     * @return
     */
    public Collection<TransactionCluster> findClusters(Iterable<Transaction> transactions) {
        log.debug("Finding clusters");

        // Construct helper data-structures.

        ImmutableMap<String, Transaction> transactionsById = Maps.uniqueIndex(transactions,
                Transaction::getId);

        ArrayList<String> transactionIds = Lists.newArrayList(transactionsById.keySet());

        // Calculate the distance matrix for all the transactions.

        double[][] transactionDistances = calculateTransactionDistances(transactionIds, transactionsById);

        // Run the clustering algorithm.

        Cluster rootCluster = clusteringAlgorithm.performClustering(transactionDistances,
                transactionIds.toArray(new String[] {}), new AverageLinkageStrategy());

        // Evaluate and return the detected (and valid) clusters.

        return evaluateCluster(rootCluster, transactionsById, transactionIds, transactionDistances);
    }

    public Collection<TransactionCluster> findClustersQuickly(List<Transaction> transactions) {
        log.debug("Finding clusters quickly");

        ImmutableListMultimap<String, Transaction> groupedTransactions = Multimaps.index(transactions,
                Transaction::getDescription);

        List<TransactionCluster> quickClusters = Lists.newArrayList();

        // Group the clusters by description.

        for (String description : groupedTransactions.keySet()) {
            List<Transaction> quickClusterTransactions = groupedTransactions.get(description);

            double quickClusterScore = 0;

            for (Transaction t : quickClusterTransactions) {
                quickClusterScore += Math.abs(t.getAmount());
            }

            TransactionCluster quickCluster = new TransactionCluster();

            quickCluster.setDescription(quickClusterTransactions.get(0).getDescription());
            quickCluster.setTransactions(quickClusterTransactions);
            quickCluster.setScore(quickClusterScore);

            quickClusters.add(quickCluster);
        }

        log.debug("Rationalized " + transactions.size() + " transactions into " + quickClusters.size()
                + " quick clusters");

        // Sort the quick clusters by amount.

        Collections.sort(quickClusters, (left, right) -> Doubles.compare(right.getScore(), left.getScore()));

        // Extract the quick clusters we should continue with.

        List<TransactionCluster> quickClustersToCluster = Lists.newArrayList(Iterables.limit(quickClusters,
                MAXIMUM_TRANSACTIONS_TO_CLUSTER));

        Map<String, TransactionCluster> quickClustersToClusterByTransactionId = Maps.uniqueIndex(
                quickClustersToCluster, tc -> tc.getTransactions().get(0).getId());

        Iterable<Transaction> transactionsToCluster = Iterables.transform(quickClustersToCluster,
                tc -> {
                    Transaction transaction = tc.getTransactions().get(0).clone();

                    double amount = 0;

                    for (Transaction t : tc.getTransactions()) {
                        amount += t.getAmount();
                    }

                    transaction.setAmount(amount);

                    return transaction;
                });

        Collection<TransactionCluster> clusters = findClusters(transactionsToCluster);

        if (log.isDebugEnabled()) {
            log.debug("Clustered " + Iterables.size(transactionsToCluster) + " transactions");
        }

        // Replace transactions from the quick clustering.

        for (TransactionCluster cluster : clusters) {
            List<Transaction> expandedClusterTransactions = Lists.newArrayList();

            for (Transaction t : cluster.getTransactions()) {
                if (quickClustersToClusterByTransactionId.containsKey(t.getId())) {
                    expandedClusterTransactions.addAll(quickClustersToClusterByTransactionId.get(t.getId())
                            .getTransactions());
                } else {
                    expandedClusterTransactions.add(t);
                }
            }

            cluster.setTransactions(expandedClusterTransactions);
        }

        // Add the clusters we've had to take from the quick clustering.

        List<TransactionCluster> quickClustersNotToCluster = Lists.newArrayList(Iterables.skip(quickClusters,
                MAXIMUM_TRANSACTIONS_TO_CLUSTER));

        for (TransactionCluster quickCluster : quickClustersNotToCluster) {
            quickCluster.setScore(0);
        }

        clusters.addAll(quickClustersNotToCluster);

        // Return the final cluster list.

        return clusters;
    }

    /**
     * Strips the pattern from wild cards and count "real" characters.
     */
    private int numberOfPaternChars(String globalBestPatter) {
        String cleandPattern = globalBestPatter.replace("*", "").replace(" ", "");
        return cleandPattern.length();
    }
}
