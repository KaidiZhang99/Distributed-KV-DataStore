package dslabs.primarybackup;

import dslabs.framework.Message;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Address;
import dslabs.primarybackup.PBServer.*;
import dslabs.atmostonce.AMOApplication;
import lombok.Data;

/* -------------------------------------------------------------------------
    ViewServer Messages
   -----------------------------------------------------------------------*/
@Data
class Ping implements Message {
    private final int viewNum;
}

@Data
class GetView implements Message {
}

@Data
class ViewReply implements Message {
    private final View view;
}

/* -------------------------------------------------------------------------
    Primary-Backup Messages
   -----------------------------------------------------------------------*/
@Data
class Request implements Message {
    // TODO: client request ...
    private final Command command;
    private final int sequenceNum;
}

@Data
class Reply implements Message {
    //TODO: server response ...
    private final Result result;
    private final int sequenceNum;
}

@Data
class BackupRequest implements Message {
    // TODO: primary send backup request to backup ...
    private final AMOApplication app;
    private final View view;
    private final int viewNum;
}

@Data
class BackupReply implements Message {
    // TODO: backup send reply to primary ...
    private final AMOApplication app;
    private final PBResult result;
    private final View view;
    private final int viewNum;
}

@Data
class SyncRequest implements Message {
    // TODO: primary sync up with backup ...
    private final Command command;
    private final int sequenceNum;
    private final View view;
    private final Address client;
}

@Data
class SyncReply implements Message {
    // TODO: backup send reply to primary ...
    private final PBResult result;
    private final Command command;
    private final int sequenceNum;
    private final View view;
    private final Address client;
}

// TODO: add more messages here ...
