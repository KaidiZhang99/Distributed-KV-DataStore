# Project 1: Report
## Intro (Your understanding)
This project is a simple illustration of client and server nodes under DSLab framework. Client will send a request/ping to the server and server will respond/pong.

Client is implemented in `PingClient`, and server is implemented in `PingServer` which hosts a `PingApplication`.

## Flow of Control & Code Design
- PingServer starts and host the server
    - The serving logic is implemented in PingApplication
    - The purpose of wrapping the logic into a different application is to allow the framework to handle any kinds of applications
- PingApplication handles the requests and return a response
- PingClient will send a request to the server, and then waits for a response
  - If server does not respond on time, it will call `onPongTimer` to resend the request and reset the time
## Design Decisions
- Server logic is separated into `PingServer` and `PingApplication`
  - The actual logic (message handler) is implemented in `PingApplication`
  - `PingServer` extends the `Node` class and is only responsible for:
    - listening on the TCP port
    - dispatch the message to message handler/PingApplication
    - send reply message to client

## Missing Components

- None

## References
- README.md
## Extra (Optional)