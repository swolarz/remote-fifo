
REGISTRY_HOST ?= localhost
REGISTRY_PORT ?= 1099

.PHONY: compile manager write read clean

compile:
	@mkdir -p target/protocol
	javac -d target/protocol \
		protocol/src/pl/put/swolarz/rfifo/protocol/NamedPipeRegistry.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoClient.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoReader.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoWriter.java
	@mkdir -p target/producer
	javac -cp ./lib;./target/protocol \
		-d target/producer \
		producer/src/pl/put/swolarz/rfifo/producer/Producer.java
	@mkdir -p target/consumer
	javac -cp ./lib;./target/protocol \
		-d target/consumer \
		consumer/src/pl/put/swolarz/rfifo/consumer/Consumer.java
	@mkdir -p target/manager
	javac -cp ./lib;./target/protocol \
		-d target/manager \
		manager/src/pl/put/swolarz/rfifo/manager/Manager.java \
		manager/src/pl/put/swolarz/rfifo/manager/NamedPipeRegistrar.java

manager:
	@java -cp ./target/manager pl.put.swolarz.rfifo.manager.Manager --port=$(REGISTRY_PORT)

write:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -cp ./target/producer pl.put.swolarz.rfifo.manager.Producer --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

read:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -cp ./target/consumer pl.put.swolarz.rfifo.manager.Consumer --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

clean:
	@rm -r target
