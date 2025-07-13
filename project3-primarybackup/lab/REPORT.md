# Project 3: Report
## Intro (Your understanding)

This project implements a distributed system with one server (`PBServer`) and one primary backup (all other nodes are idle). To achieve this, there will be one "brain" of the entire system -- `ViewServer`, which is assumed non-failing. Clients (`PBClient`) will talk to the server of the system, and talk to the `ViewServer` to get an updated view of the system.

* `ViewServer` will monitor the entire system based on the periodic `Ping` message it receives from all nodes (server, backup, and other idle nodes). If within a given timeframe, it didn't receive messages from a given node, it will consider that node dead and update the view of the system (ex. re-select a backup server, promote the backup server to primary, etc.)

* `PBServer` is the server nodes that are serving in this distributed system. As discussed earlier, they will need to send `Ping` message periodically, and get back the most updated view of the system. Based on the view, each node will find out its role in the system, i.e. whether they are server, backup, or idle nodes. If they are primary node, they will need to handle client's request using at-most-once semantics.

* `PBClient` is the client node that are sending `Request` messages with `Commands` to servers to execute. They also communicate with `ViewServer` if they think their view is outdated (maybe request timed out or at the very beginning) to get the view of the system.

## Flow of Control & Code Design
### ViewServer
The `ViewServer` receives `Ping` messages from all other server nodes, and it will record their address once received `Ping` message for each time interval. By checking at the recorded address, `ViewServer` knows whether a given server node is live or not.

For each `Ping` message `ViewServer` receives, it will check to see whether it's from the current primary. Once it receives the `Ping` from primary in the current view, it will promote an idle node to be the backup server.

For each time interval: 
- If `ViewServer` doesn't receive `Ping` from the primary server (assume primary has died):
    - if there's a backup node, it will promote the backup to be the new primary, and start a new view
    - otherwise, stay in current view
- Or if `ViewServer` doesn't receive `Ping` from the backup server:
    - It will try promote another idle node to be the new backup, if there are any idle nodes
    - Otherwise, remove the backup

For each Ping message, `ViewServer` will send back a `ViewReply` message informing the nodes about the current view. It also has a `GetView` API for clients to get the most updated view.


### PBServer
PBServer are the server nodes in the distributed system. It can be in four states: 
- idle: it's an idle/backup node
- serving: it's a fully serving primary node
- command_backup: it's a primary node waiting for syncing the client's request with backup node, if there's a backup under current view
- view_backup: it's primary node waiting for syncing the entire application with the backup node, if there's a backup node under current view

If the node is under `idle` state, it will listen to `BackUpRequest` and `SyncRequest`, and record the application status. It also listens to the `ViewReply` to see whether there're updates in the view. If it gets promoted to primary node, it will see whether the new view has a backup node. If so, it will send a `BackUpRequest` to the backup node and move to `view_backup` state. If not, it will move to `serving` state.

If the node is under `serving` state, it will listen to `Request` from clients, and sync the requests with backup nodes while changing to `command_backup` state.

If the node is under `command_backup` state, it will listen to SyncReply messages which indicates that the backup had successfully updated the app status, and move back to serving state.

If the node is under `view_backup` state, it will listen to `BackUpReply` messages and move back to `serving` state.

### PBClient
Client will send a `GetView` message to `ViewServer` when it initializes and store the view, then it will only communicate with the primary server based on that view. If the request times out, it will communicate with `ViewServer` and update its view.


## Design Decisions
- One timer for each communication.
    - There are different timers associated with different request, addressing the issue of unreliable network:
        - BackUpTimer: for BackUpRequest 
        - SyncTimer: for SyncRequest
        - ClientTimer: for GetView Request, etc

- State Machine
    - PBServer is implemented with state machine since each server can operate on different states and roll into different states/roles based on incoming messages.
    - No distinction on states for backup server and idle nodes. Both of them are in `idle` state as they are operating similarly (except they listen to slightly different messages).


## Missing Components

Current design fails to handle the case where there's a split brain in the system, i.e. when there're two primary servers, and clients will only talk to the old (wrong) server,  but fails to update its view as the old primary server will continue responding to the client.

This may be addressed by adjusting the design of clients and/or the way clients and ViewServer sync, etc.

## References
- Java State Machine: https://www.baeldung.com/java-enum-simple-state-machine
- README: https://github.gatech.edu/omscs7210-spr23/project3-primarybackup/tree/main/lab
- Piazza: https://piazza.com/class/lco3w3pyqdq3vt/post/154
- Primary-backup: https://www.cs.cornell.edu/fbs/publications/DSbook.c8.pdf
- Notes: https://courses.cs.washington.edu/courses/cse452/22wi/lecture/L5/
## Extra (Optional)