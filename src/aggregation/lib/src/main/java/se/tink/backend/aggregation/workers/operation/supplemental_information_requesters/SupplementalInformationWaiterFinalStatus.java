package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

enum SupplementalInformationWaiterFinalStatus {
    NONE,
    FINISHED,
    FINISHED_WITH_EMPTY,
    CANCELLED,
    CANCELLED_NXGEN,
    TIMED_OUT,
    ERROR
}
