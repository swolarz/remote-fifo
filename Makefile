
REGISTRY_HOST ?= localhost
REGISTRY_PORT ?= 1099

.PHONY: compile manager write read clean

all: compile

compile:
	@mkdir -p target/protocol
	javac -d target/protocol \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoRegistry.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoClient.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoReader.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoWriter.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/ConsumerFailureException.java \
		protocol/src/pl/put/swolarz/rfifo/protocol/FifoSideAlreadyBoundException.java
	jar cvf target/protocol.jar -C target/protocol .
	@mkdir -p target/producer
	javac -cp ./lib/*:./target/protocol \
		-d target/producer \
		producer/src/pl/put/swolarz/rfifo/producer/Producer.java \
		producer/src/pl/put/swolarz/rfifo/producer/ProducerWriter.java
	jar cvfm target/producer.jar producer/Manifest.txt -C target/producer . -C target protocol.jar
	@mkdir -p target/consumer
	javac -cp ./lib/*:./target/protocol \
		-d target/consumer \
		consumer/src/pl/put/swolarz/rfifo/consumer/Consumer.java \
		consumer/src/pl/put/swolarz/rfifo/consumer/ConsumerReader.java
	jar cvfm target/consumer.jar consumer/Manifest.txt -C target/consumer . -C target protocol.jar
	@mkdir -p target/manager
	javac -cp ./lib/*:./target/protocol \
		-d target/manager \
		manager/src/pl/put/swolarz/rfifo/manager/Manager.java \
		manager/src/pl/put/swolarz/rfifo/manager/FifoConnectionsRegistry.java
	jar cvfm target/manager.jar manager/Manifest.txt \
		-C target/manager .
	@cp lib/* target/

manager:
	@cd target && java -jar manager.jar --port=$(REGISTRY_PORT)

write:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -jar target/producer.jar --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

read:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -jar target/consumer.jar --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

clean:
	@rm -r target
