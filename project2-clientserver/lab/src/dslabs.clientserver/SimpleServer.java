package dslabs.clientserver;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    //TODO: declare fields for your implementation
    private final AMOApplication app;
    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, Application app) {
        super(address);

        //TODO: record the passed application
        //Hints: wrap app inside your implementation for AMOApplication (Part 3)
        this.app = new AMOApplication<>(app);
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request m, Address sender) {
        //TODO: handle client request
        //Hints: call interface provided by AMOApplication to hide the duplication check from server
        AMOCommand cmd = new AMOCommand(m.sequenceNum(), m.command(), sender.toString());
        Result res = app.execute(cmd).result();
        Reply reply = new Reply(res, m.sequenceNum());
        send(reply, sender);
    }
}
