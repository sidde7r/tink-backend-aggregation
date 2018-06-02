package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.List;

public class TasksQueueConfiguration {

    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    public enum Modes {
        // The queue is not used at all, or even initialized.
        DISABLED,
        // Tasks are sent to the queue in parallel, and logged when consumed. No actual execution.
        TEST,
        // The queue is used for executing tasks.
        ENABLED
    }

    public static final Modes DEFAULT_MODE = Modes.DISABLED;
    public static final ImmutableSet<Modes> SHOULD_PRODUCE = ImmutableSet.of(Modes.ENABLED, Modes.TEST);
    public static final ImmutableSet<Modes> SHOULD_CONSUME = ImmutableSet.of(Modes.ENABLED);
    public static final ImmutableSet<Modes> SHOULD_RUN = SHOULD_PRODUCE;

    @JsonProperty
    private Modes mode = Modes.DISABLED;

    @JsonProperty
    private String groupId = "default";

    @JsonProperty
    private List<String> hosts;

    @JsonProperty
    private int workers = 5;

    /**
     * Maximum number of tasks currently processed to allowed to start a batch task
     * processing Is supposed to be lower than {@code workers} to allow batch tasks
     * to interfere with high priority ones to a smaller degree.
     */
    @JsonProperty
    private int mainTopicLimit = 3;

    @JsonProperty
    private int numRetries = 2000; // Basically a week worth of retrying.

    @JsonProperty
    private boolean firehoseEnabled = false;

    @JsonProperty
    private String resetFromDate;

    @JsonProperty
    private boolean highPrioTopic = false;

    @JsonProperty
    private int consumerMaxPollRecords = 5;

    @JsonProperty
    private boolean skipPending = false;

    @JsonProperty
    private boolean nsq = false;

    public boolean skipPending() {
        return skipPending;
    }

    public String getGroupId() {
        return groupId;
    }

    public Modes getMode() {
        return mode;
    }

    public int getWorkers() {
        return workers;
    }

    public int getMainTopicLimit() {
        return mainTopicLimit;
    }

    public String getHosts() {
        Preconditions.checkNotNull(hosts, "Missing hosts configuration");

        return COMMA_JOINER.join(hosts);
    }

    public boolean isFirehoseEnabled() {
        return firehoseEnabled;
    }

    public int getNumRetries() {
        return numRetries;
    }

    public boolean useHighPrioTopic() { return highPrioTopic; }

    public int getConsumerMaxPollRecords() {
        return consumerMaxPollRecords;
    }

    public boolean useNSQ() {
        return nsq;
    }
}
