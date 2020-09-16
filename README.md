# Remote Named Pipe (FIFO) Service

## Overview

Java RMI application for setting up named pipe/fifo (similar to Linux named pipes) between two different hosts.
Application consists of a manager which serves as a registry for named pipes, as well as a reader application and a writer application.

The fifo queue registration is centralized at the manager side (which uses RMI registry), but the data stream flows only between the writer and the reader.

## Building and running

#### Building whole application
Run: `make [compile]`

#### Launching FIFO manager

Run locally: `make manager`

Run accessible remotely: `EXPORT_PATH=<HOSTNAME|PUBLIC_IP> make manager`

Run with different port: `REGISTRY_PORT=<PORT> make manager`

The named pipe manager starts RMI registry as well. The arguments: `EXPORT_PATH` and `REGISTRY_PORT` can be joined in one call.

#### Running FIFO reader and writer

Named pipes are registered automatically when a reader or writer occurs. The name of the fifo is specified in `FIFO_NAME` argument.

Run reader: `FIFO_NAME=test EXPORT_HOSTNAME<HOSTNAME|PUBLIC_IP> make read > <OUTPUT_FILE>`

Run writer: `FIFO_NAME=test EXPORT_HOSTNAME<HOSTNAME|PUBLIC_IP> make write < <INPUT_FILE>`

Both writer and reader applications support `REGISTRY_HOST` and `REGISTRY_PORT` arguments.
It is also advised to redirect input/output from/to a file, as the data is sent in blocks of 4kB and no less, unless EOF is encountered or *Ctrl+d* is pressed (on Linux terminal).

`EXPORT_HOSTNAME` must be used in case of deploying the manager, readers and writers on more than one host.
The default value is `127.0.1.1` which allows to run named pipes locally only.
The value of this argument should be a hostname or ip which is available from other hosts which are involved in the deployment.
