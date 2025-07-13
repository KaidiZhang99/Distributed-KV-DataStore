package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore.SingleKeyCommand;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

import static dslabs.primarybackup.PingTimer.PING_MILLIS;
import static dslabs.primarybackup.ViewServer.STARTUP_VIEWNUM;
import static dslabs.primarybackup.ClientTimer.CLIENT_RETRY_MILLIS;

@Log
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBClient extends Node implements Client {
    private final Address viewServer;

    //TODO: declare fields for your implementation ...
    private int sequenceNum;
    private int viewNum;
    private Result result;
    private View view;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public PBClient(Address address, Address viewServer) {
        super(address);
        this.viewServer = viewServer;
    }

    @Override
    public synchronized void init() {
        // TODO: initialize fields ...
        sequenceNum = 0;
        viewNum = STARTUP_VIEWNUM;
        view = new View(STARTUP_VIEWNUM, null, null);
        send(new GetView(), viewServer);
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        //TODO: send command to server ...
        if (!(command instanceof SingleKeyCommand)) {
            throw new IllegalArgumentException();
        }
        result = null;
        Request request = new Request(command, sequenceNum);
        if (view.primary() != null) {
            this.send(request, view.primary());
//            set(new ClientTimer(request, sequenceNum), CLIENT_RETRY_MILLIS);
//        } else if (view.backup() != null) {
//            this.send(request, view.backup());
        }
        else {
            send(new GetView(), viewServer);
        }
        set(new ClientTimer(request, sequenceNum), CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        //TODO: check whether there is result ...
        return result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        //TODO: wait to get result ...
        while (result == null) { wait();}
        return result;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply m, Address sender) {
        //TODO: check desired reply arrive ...
        if (sequenceNum != m.sequenceNum()) {return;}
        sequenceNum+=1;
        result = m.result();
        notify();
    }

    private synchronized void handleViewReply(ViewReply m, Address sender) {
        //TODO: perform action when timer reach timeout ...
        if (m.view().viewNum() > viewNum) {
            view = m.view();
            viewNum = m.view().viewNum();
        }
        notify();
    }

    // TODO: add utils here ...

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer t) {
        // TODO: handle client request timeout ...
        if (t.sequenceNum() >= sequenceNum) {
            send(new GetView(), viewServer); // refresh view
            set(t, CLIENT_RETRY_MILLIS);
            sendCommand(t.request().command());
        }
//        send(new GetView(), viewServer);
    }
}
