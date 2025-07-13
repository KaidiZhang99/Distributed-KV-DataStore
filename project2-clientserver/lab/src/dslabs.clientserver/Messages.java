package dslabs.clientserver;

import dslabs.framework.Command;
import dslabs.framework.Message;
import dslabs.framework.Result;
import lombok.Data;

@Data
class Request implements Message {
    //TODO: message for client request
    private final Command command;
    private final int sequenceNum;
}

@Data
class Reply implements Message {
    //TODO: message for server reply
    private final Result result;
    private final int sequenceNum;
}
