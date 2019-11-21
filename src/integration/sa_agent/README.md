# Standalone Agent PoC

This is a prototype of how a Standalone Agent could look like.

## Contract
Defined in package:
`//src/integration/sa_agent/sa_grpc_api`

All standalone agents are to implement a the same gRPC contract.
The contract defines all canonical data models such as Account, Transaction etc. as well as gRPC services for AIS/PIS. 
All operations such as fetching accounts, transactions, initiating payments and more are structured as gRPC methods.

## Java Agent framework
Defined in package:
`//src/integration/sa_agent/sa_agent_framework`

A toolkit for streamlining agent creation in Java. This toolkit is self contained and has no references to legacy code.
The agent framework is designed around Spring as it is able to deliver high velocity of development of new agents by 
developers familiar with Spring. Furthermore, the framework maintains it's set of dependencies such that agent code does
not introduce any new external dependencies.

## Agent implementation 
Defined in package:
`//src/integration/sa_agents/pt_sa_ob_sibs`

An example of an agent implemented in Java using the Java Agent framework

## Compatibility with Aggregation application
Defined in package:
`//src/integration/sa_generic_agent`

Standalone Agents implement a gRPC contract and therefore can be used directly without the need of going through
the Aggregation application. The generic agent package implements the Aggregation interfaces and acts as a client towards 
standalone agents in order to provide compatibility.

