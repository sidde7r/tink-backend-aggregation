package se.tink.backend.integration.agent_data_availability_tracker.client;

import java.util.ArrayDeque;
import se.tink.backend.integration.agent_data_availability_tracker.api.TrackAccountRequest;

public class AccountDeque extends ArrayDeque<TrackAccountRequest> {

    private static final int MAX_SIZE = 1000;

    public AccountDeque() {
        super(MAX_SIZE);
    }

    @Override
    public boolean add(TrackAccountRequest trackAccountRequest) {

        if (super.size() == MAX_SIZE - 1) {
            super.removeLast();
        }

        return super.add(trackAccountRequest);
    }
}
