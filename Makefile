
REGISTRY_HOST ?= localhost
REGISTRY_PORT ?= 1099
EXPORT_HOSTNAME ?= 127.0.1.1


PROTOCOL_SOURCES := $(shell find protocol/src -type f -name "*.java")
PRODUCER_SOURCES := $(shell find producer/src -type f -name "*.java")
CONSUMER_SOURCES := $(shell find consumer/src -type f -name "*.java")
MANAGER_SOURCES := $(shell find manager/src -type f -name "*.java")


.PHONY: all compile manager write read clean compile-consumer compile-producer compile-manager compile-protocol

all: compile

compile-protocol: $(PROTOCOL_SOURCES)
	@mkdir -p target/protocol
	javac -d target/protocol $(PROTOCOL_SOURCES)
	jar cvf target/protocol.jar -C target/protocol .

compile-producer: compile-protocol $(PRODUCER_SOURCES)
	@mkdir -p target/producer
	javac -cp ./lib/*:./target/protocol -d target/producer $(PRODUCER_SOURCES)
	jar cvfm target/producer.jar producer/Manifest.txt -C target/producer .

compile-consumer: compile-protocol $(CONSUMER_SOURCES)
	@mkdir -p target/consumer
	javac -cp ./lib/*:./target/protocol -d target/consumer $(CONSUMER_SOURCES)
	jar cvfm target/consumer.jar consumer/Manifest.txt -C target/consumer .

compile-manager: compile-protocol $(MANAGER_SOURCES)
	@mkdir -p target/manager
	javac -cp ./lib/*:./target/protocol -d target/manager $(MANAGER_SOURCES)
	jar cvfm target/manager.jar manager/Manifest.txt -C target/manager .

compile: compile-protocol compile-producer compile-consumer compile-manager
	@cp lib/* target/

manager:
	@java -Djava.rmi.server.hostname=$(EXPORT_HOSTNAME) -jar target/manager.jar --port=$(REGISTRY_PORT)

write:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -Djava.rmi.server.hostname=$(EXPORT_HOSTNAME) \
		-jar target/producer.jar --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

read:
ifndef FIFO_NAME
	@echo FIFO_NAME variable not passed
else
	@java -Djava.rmi.server.hostname=$(EXPORT_HOSTNAME) \
		-jar target/consumer.jar --fifo=$(FIFO_NAME) --host=$(REGISTRY_HOST) --port=$(REGISTRY_PORT)
endif

clean:
	@rm -r target
