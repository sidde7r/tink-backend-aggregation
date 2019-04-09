package se.tink.libraries.metrics;

/** To avoid sequence conflicts, since their domains aren't defined by the class, keep them here. */
public class SequenceTimers {
    /*
     * @see ActivityGeneratorWorker.process()
     */
    public static final String GENERATE_ACTIVITIES = "generate-activities";

    /*
     * @see ActivityGeneratorWorker.loadActivityContext()
     */
    public static final String GENERATE_ACTIVITY_CONTEXT = "generate-activity-context";

    /*
     * @see StatisticsGeneratorWorker.process()
     */
    public static final String GENERATE_STATISTICS = "generate-statistics";

    /*
     * @see StatisticsGeneratorWorker.generateUpdatedUserState()
     */
    public static final String GENERATE_USER_STATE = "generate-user-state";

    /*
     * @see StreamingGrpcTransport.stream()
     */
    public static final String START_GRPC_STREAMING = "start-grpc-streaming";

    /*
     * @see SystemServiceResource.updateTransactions()
     */
    public static final String UPDATE_TRANSACTIONS = "update-transactions";

    /*
     * @see StatisticsGeneratorWorker.process()
     */

    public static final String INVALIDATE_STATISTICS_CACHE = "invalidate-statistics-cache";

    /*
     * @see AccountServiceController.update()
     */
    public static final String UPDATE_ACCOUNT = "update-account";

    /*
     * @see TransactionServiceResource.updateTransactions()
     */
    public static final String MAIN_UPDATE_TRANSACTIONS = "main-update-transactions";

    /*
     * @See TransactionServiceController.categorize()
     */
    public static final String CATEGORIZE = "categorize";
}
