package dslabs.primarybackup;

import dslabs.framework.Address;
import dslabs.framework.Node;
import static dslabs.primarybackup.PingCheckTimer.PING_CHECK_MILLIS;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class ViewServer extends Node {
    static final int STARTUP_VIEWNUM = 0;
    private static final int INITIAL_VIEWNUM = 1;

    // TODO: declare fields for your implementation ...
    private View currentView = null;
    private boolean primaryPinged = false;
    private Set<Address> pingedNodesCurInterval = new HashSet<>();

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public ViewServer(Address address) {
        super(address);
    }

    @Override
    public void init() {
        set(new PingCheckTimer(), PING_CHECK_MILLIS);
        // TODO: initialize fields ...
        currentView = new View(STARTUP_VIEWNUM, null, null);
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handlePing(Ping m, Address sender) {
        // TODO: handle server ping, filtering outdated ping ...
        // if it's the first view, assign the sender the first primary node
        pingedNodesCurInterval.add(sender);
        if (currentView.viewNum() == STARTUP_VIEWNUM) {
            currentView = new View(INITIAL_VIEWNUM, sender, null);
        } else { // view already initialized
            if (m.viewNum() == currentView.viewNum()) {
                if (currentView.primary().equals(sender)) {
                    primaryPinged = true;
                }
            }

            // no matter what's the viewNum of m
            // as long as ViewServer see backup is null, we should assign one to it.
            if (primaryPinged && currentView.backup() == null) {
                // view += 1
                // assign a backup
                pingedNodesCurInterval.remove(currentView.primary());
                Address newBackUp = getIdleNode();
                if (newBackUp != null) {
                    setBackUp(newBackUp);
                }
                pingedNodesCurInterval.add(currentView.primary());
            }
        }

        ViewReply currentReply = new ViewReply(currentView);
        send(currentReply, sender);
    }

    private void handleGetView(GetView m, Address sender) {
        // TODO: return current view to sender ...
        ViewReply currentReply = new ViewReply(currentView);
        send(currentReply, sender);
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private void onPingCheckTimer(PingCheckTimer t) {
        // TODO: check server liveness on ping check ...
        set(t, PING_CHECK_MILLIS);
        if (!pingedNodesCurInterval.isEmpty()) { // if it's empty, no nodes
            if (!pingedNodesCurInterval.contains(currentView.primary())) {
                pingedNodesCurInterval.remove(currentView.backup());
                setPrimary(getIdleNode());
            } else if (!pingedNodesCurInterval.contains(currentView.backup()) ||
                    currentView.backup() == null) {
                pingedNodesCurInterval.remove(currentView.primary());
                setBackUp(getIdleNode());
            }
        }
        pingedNodesCurInterval.clear();
    }

    /* -------------------------------------------------------------------------
        Utils
       -----------------------------------------------------------------------*/
    // TODO: add utils here ...
    private void setPrimary(Address newBackUpNode) {
        if (primaryPinged && currentView.backup()!=null) {
            currentView = new View(currentView.viewNum()+1, currentView.backup(), newBackUpNode);
            primaryPinged = false;
        }
    }

    private void setBackUp(Address newBackUpNode) {
        if (primaryPinged) {
            currentView = new View(currentView.viewNum()+1, currentView.primary(), newBackUpNode);
            primaryPinged = false;
        }
    }

    private Address getIdleNode() {
        if (!pingedNodesCurInterval.isEmpty()) {
            return (Address) pingedNodesCurInterval.toArray()[0];
        }
        return null;
    }
}