package dslabs.primarybackup;

import com.google.common.base.Objects;
import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

import static dslabs.primarybackup.BackUpTimer.BACKUP_MILLIS;
import static dslabs.primarybackup.SyncTimer.SYNC_MILLIS;
import static dslabs.primarybackup.ViewServer.STARTUP_VIEWNUM;
import static dslabs.primarybackup.PingTimer.PING_MILLIS;


@Log
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class PBServer extends Node {
    private final Address viewServer;


    public interface PBResult extends Result {
    }

    enum State {
        IDLE, SERVING, COMMAND_BACKUP, VIEW_BACKUP;
    }

    @Data
    public static final class InvalidView implements PBResult {
        @NonNull private final View view;
    }

    @Data
    public static final class BackupSuccess implements PBResult {
    }

    @Data
    public static final class SyncSuccess implements PBResult {
    }

    // TODO: declare fields for your implementation ...

    private AMOApplication app;

    private View view;
    private View syncingView;

    private int syncingRequest;
    private State state;
    private Address clientServing;


    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    PBServer(Address address, Address viewServer, Application app) {
        super(address);
        this.viewServer = viewServer;

        // TODO: wrap app inside AMOApplication ...
        this.app = new AMOApplication<>(app);
    }

    @Override
    public void init() {
        view = new View(STARTUP_VIEWNUM, null, address());
        syncingRequest = -1;
        state = State.IDLE;
        send(new Ping(STARTUP_VIEWNUM), viewServer);
        set(new PingTimer(), PING_MILLIS);
    }


    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request r, Address sender) {
        // TODO: handle client request ...
        if (state  == State.SERVING) {
            boolean noNeedToSync = false;
            AMOCommand cmd = new AMOCommand(sender.toString(), r.sequenceNum(), r.command());
            if (app.alreadyExecuted(cmd) || r.command().readOnly() || view.backup() == null) {
                noNeedToSync = true;
            }
            if (noNeedToSync) {
                Result result = app.execute(cmd).result();
                send(new Reply(result, r.sequenceNum()), sender);
            } else {
                state = State.COMMAND_BACKUP;
                syncingRequest = r.sequenceNum();
                clientServing = sender;
                SyncRequest req = new SyncRequest(r.command(), r.sequenceNum(), view, sender);
                send(req, view.backup());
                set(new SyncTimer(req), SYNC_MILLIS);
            }
        }
    }

    private void handleViewReply(ViewReply m, Address sender) {
        // TODO: handle view reply from view server ...
        int newViewNum = m.view().viewNum();
        int oldViewNum = view.viewNum();
        if (oldViewNum >= newViewNum || (syncingView != null && syncingView.viewNum() > newViewNum)) return;  // ignore old view
        if (Objects.equal(m.view().primary(), address())) {
            if (m.view().backup() != null) {
                state = State.VIEW_BACKUP;
                if (syncingView != m.view()) {
                    syncingView = m.view();
                }
                BackupRequest request = new BackupRequest(app, m.view(), m.view().viewNum());
                send(request, m.view().backup());
                set(new BackUpTimer(request), BACKUP_MILLIS);

            } else {
                state = State.SERVING;
                view = m.view();
                reset();
            }
        } else {
            state = State.IDLE;
            reset();
            return;
        }
    }

    // TODO: your message handlers ...
    private void handleBackupRequest(BackupRequest r, Address sender) {
        if (state == State.IDLE && view.viewNum() == r.view().viewNum()) {
            send(new BackupReply(r.app(), new BackupSuccess(), r.view(), r.view().viewNum()), sender);
        } else if (r.view().viewNum() > view.viewNum() && state == State.IDLE) {
            app = r.app();
            view = r.view();
            send(new BackupReply(r.app(), new BackupSuccess(), view, r.view().viewNum()), sender);
        }
    }

    private void handleBackupReply(BackupReply r, Address sender) {
        if (state == State.VIEW_BACKUP && syncingView != null && syncingView.viewNum() == r.view().viewNum()) {
            view = r.view();
            state = State.SERVING;
            reset();
        }
        send(new Ping(view.viewNum()), viewServer);
        set(new PingTimer(), PING_MILLIS);
    }

    private void handleSyncRequest(SyncRequest r, Address sender) {
        if (state == State.IDLE && Objects.equal(address(), view.backup()) && r.view().viewNum() == view.viewNum()) {
            AMOCommand amo = new AMOCommand(r.client().toString(), r.sequenceNum(), r.command());
            app.execute(amo).result();
            SyncReply reply = new SyncReply(new SyncSuccess(), r.command(), r.sequenceNum(), r.view(), r.client());
            send(reply, sender);
        }
    }

    private void handleSyncReply(SyncReply r, Address sender) {
        if (state == State.COMMAND_BACKUP && Objects.equal(clientServing, r.client()) && r.view().viewNum() == view.viewNum() && syncingRequest == r.sequenceNum()) {
            state = State.SERVING;
            reset();
            Result result = app.execute(new AMOCommand(r.client().toString(), r.sequenceNum(), r.command())).result();
            send(new Reply(result, r.sequenceNum()), r.client());
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingTimer(PingTimer t) {
        // TODO: on ping timeout ...
        Ping ping = new Ping(view.viewNum());
        send(ping, viewServer);
        set(t, PING_MILLIS);
    }

    // TODO: your message handlers ...
    private synchronized void onBackUpTimer(BackUpTimer t) {
        BackupRequest r = t.request();
        if (state == State.VIEW_BACKUP && view.viewNum() == r.view().viewNum()) {
            send(r, view.backup());
            set(t, BACKUP_MILLIS);
        }
    }

    private synchronized void onSyncTimer(SyncTimer t) {
        SyncRequest r = t.request();
        if (state == State.COMMAND_BACKUP && syncingRequest == r.sequenceNum() && clientServing.equals(r.client()) && view.viewNum() == r.view().viewNum()) {
            send(r, view.backup());
            set(t, SYNC_MILLIS);
        }
    }

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/

    public void reset() {
        syncingView = null;
        syncingRequest = -1;
        clientServing = null;
    }
}
