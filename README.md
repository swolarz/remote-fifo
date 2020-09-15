# Remote Named Pipe (FIFO) service

## Overview

Java RMI application for setting up named pipe/fifo (similar to Linux named pipes) between two different hosts.
Application consists of a manager which serves as a registry for named pipes, as well as a reader application and writer application.

The fifo queue registration is centralized at the manager side (which uses RMI registry), but the data stream flows only between the writer and the reader.

## Building and running

TODO