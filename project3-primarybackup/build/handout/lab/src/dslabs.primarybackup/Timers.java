package dslabs.primarybackup;

import dslabs.framework.Timer;


import lombok.Data;

@Data
final class PingCheckTimer implements Timer {
    static final int PING_CHECK_MILLIS = 100;
}

@Data
final class PingTimer implements Timer {
    static final int PING_MILLIS = 25;
}

@Data
final class ClientTimer implements Timer {
    // TODO: add fields for client request
    private final Request request;
    private final int sequenceNum;
    static final int CLIENT_RETRY_MILLIS = 100;
}

// TODO: add more timers here ...
@Data
final class BackUpTimer implements Timer {
    private final BackupRequest request;
    static final int BACKUP_MILLIS = 25;
}

@Data
final class SyncTimer implements Timer {
    private final SyncRequest request;
    static final int SYNC_MILLIS = 25;
}