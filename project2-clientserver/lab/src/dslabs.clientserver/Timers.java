package dslabs.clientserver;

import dslabs.framework.Timer;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
    static final int CLIENT_RETRY_MILLIS = 100;

    //TODO: add fields to record monitoring request
    //associate timer with a request
    private final Request request;
}
