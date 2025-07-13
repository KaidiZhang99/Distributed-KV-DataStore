# Project 2: Report
## Intro (Your understanding)
- This project implements a simple client and server based on at-least-once and at-most-once semantics:

    - The client should send messages at least once
    - The server should execute each request at most once

## Flow of Control & Code Design
- `KVStore` is a simple key-value store that implements the `Application` interface
  - It's implemented as a wrapper around a java HashMap
- `SimpleClient` is the client sending requests
  - It keeps track of a sequence number which gets incremented for every completed request
  - When sending out request, it sets a timer associated with the request. If it times out and the client hasn't received the response yet, the client will resend the request
  - When the client receives the response, it will check if the response matches the sequence number of the request it's currently waiting for. If so, it will mark the request complete. Otherwise, the reply gets dropped
- `SimpleServer` leverages `AMOApplication` to handle request with at-most-once semantics
- `AMOApplication` wraps around an application and ensures the application execute the command at most once
  - `AMOApplication` uses a java `HashMap` with client id as key, and `AMOResult` as value
  - `AMOResult` stores the sequence number of the corresponding request
  - To determine if a request has already been served, `AMOApplication` uses the client id to query the `AMOResult` stored in the hashmap. If the `AMOResult` stored in the hashmap has a larger sequence number than current request, that means the request has already been served
  - When `execute()` gets called, if the request has already been served, it will return the `AMOResult` stored in the hashmap. Otherwise, it will serve the request

## Design Decisions
- In `AMOApplication`, the reason we can use client id as the key of internal hashmap is because there will only be one effective ongoing request for each client. Hence, for each client at any given time, we only need to keep track of one request.
- `AMOCommand`, `AMOResult`, and `SimpleClient` all used sequence number (as it's strictly increasing) to decide 1) whether the request/response has been fulfilled; 2) the order of messages. There's a risk that the sequence number might overflow `int`. In this case, it's fine as it's part of the project assumption, but the solution might not be sufficient in real life scenario.

## Missing Components
N/A

## References
DSLab Readme: https://github.gatech.edu/omscs7210-spr23/project2-clientserver


## Extra (Optional)