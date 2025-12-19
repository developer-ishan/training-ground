# gRPC Python Example

This example demonstrates a complete gRPC implementation in Python with all four RPC patterns:
- Unary RPC (single request, single response)
- Server Streaming (single request, stream of responses)
- Client Streaming (stream of requests, single response)
- Bidirectional Streaming (stream of requests, stream of responses)

## Setup

1. Install dependencies:
```bash
pip install grpcio grpcio-tools
```

2. Generate Python code from the proto file:
```bash
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. user.proto
```

This will generate:
- `user_pb2.py` - Message classes
- `user_pb2_grpc.py` - Service and stub classes

## Running the Example

1. Start the server (in one terminal):
```bash
python server.py
```

2. Run the client (in another terminal):
```bash
python client.py
```

## What You'll See

The client demonstrates:
- Creating users (unary RPC)
- Retrieving users (unary RPC)
- Error handling (unary RPC)
- Server streaming (receiving multiple users)
- Client streaming (sending multiple creation requests)
- Bidirectional streaming (chat-like communication)

## Understanding the Code

### Server (`server.py`)
- Implements the `UserServiceServicer` interface
- Handles all four RPC patterns
- Manages in-memory user storage
- Demonstrates proper error handling

### Client (`client.py`)
- Creates a channel to the server
- Uses the generated stub to make RPC calls
- Demonstrates all four RPC patterns
- Shows error handling

### Protocol Buffer Definition (`user.proto`)
- Defines the service interface
- Defines message types
- Uses proto3 syntax

## Key Concepts Demonstrated

1. **Marshalling/Unmarshalling**: Protocol Buffers automatically handle serialization
2. **Stubs**: Client uses generated stub, server implements servicer
3. **Streaming**: Different patterns for different use cases
4. **Error Handling**: Using gRPC status codes
5. **Type Safety**: Strong typing through Protocol Buffers

