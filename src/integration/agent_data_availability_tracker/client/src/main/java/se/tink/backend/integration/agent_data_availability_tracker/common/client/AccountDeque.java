package se.tink.backend.integration.agent_data_availability_tracker.common.client;

import java.util.concurrent.LinkedBlockingDeque;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;

public class AccountDeque extends LinkedBlockingDeque<TrackAccountRequest> {

    private static final int MAX_SIZE = 1000;

    public AccountDeque() {
        super(MAX_SIZE);
    }

    @Override
    public boolean add(TrackAccountRequest trackAccountRequest) {

        if (super.remainingCapacity() <= 0) {
            super.removeLast();
        }

        return super.add(trackAccountRequest);
    }
}
