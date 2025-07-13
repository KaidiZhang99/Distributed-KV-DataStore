package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore.SingleKeyCommand;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link Client} and {@link Node} for important
 * implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;

    //TODO: declare fields for your implementation
    private int sequenceNum = 0;
    private Request request;
    private Result result;
    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
        //TODO: Initialize declared fields if necessary
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        //TODO: send command to server
        if (!(command instanceof SingleKeyCommand)) {
            throw new IllegalArgumentException();
        }

        /* create request and set result status to null */

        result = null;
        request = new Request(command, sequenceNum);
        this.send(request, this.serverAddress);
        set(new ClientTimer(request), ClientTimer.CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        //TODO: check whether there is result
        return result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        //TODO: wait to get result
        while (result == null) {wait();}
        return result;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        //TODO: check desired reply arrive
        if ((m.sequenceNum() == sequenceNum) && request != null) {
            result = m.result();
            sequenceNum += 1;
            request = null;
            notify();
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        //TODO: perform action when timer reach timeout
        if (t.request().sequenceNum() == sequenceNum) {
            send(request, serverAddress);
            set(t, ClientTimer.CLIENT_RETRY_MILLIS);
        }
    }
}
