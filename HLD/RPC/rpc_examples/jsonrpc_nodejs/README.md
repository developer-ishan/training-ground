# JSON-RPC 2.0 Node.js Example

This example demonstrates a complete JSON-RPC 2.0 implementation in Node.js.

## Features

- Full JSON-RPC 2.0 protocol implementation
- Error handling with proper error codes
- Batch request support
- Calculator service with multiple methods
- Comprehensive client examples

## Setup

1. Install dependencies (none required - uses Node.js built-in modules):
```bash
npm install
```

## Running the Example

1. Start the server (in one terminal):
```bash
npm start
# or
node server.js
```

2. Run the client (in another terminal):
```bash
npm run client
# or
node client.js
```

## Available Methods

The server implements the following methods:

- `add([a, b])` - Add two numbers
- `subtract([a, b])` - Subtract two numbers
- `multiply([a, b])` - Multiply two numbers
- `divide([a, b])` - Divide two numbers
- `getServerInfo([])` - Get server information
- `echo([message])` - Echo a message

## JSON-RPC 2.0 Error Codes

The implementation uses standard JSON-RPC 2.0 error codes:

- `-32700` - Parse error
- `-32600` - Invalid Request
- `-32601` - Method not found
- `-32602` - Invalid params
- `-32603` - Internal error
- `-32000` to `-32099` - Server error

## Understanding the Code

### Server (`server.js`)
- Implements JSON-RPC 2.0 protocol
- Handles single and batch requests
- Proper error handling with standard error codes
- Method registration and execution

### Client (`client.js`)
- Makes JSON-RPC calls over HTTP
- Demonstrates single and batch requests
- Error handling
- Promise-based API

## Key Concepts

1. **Request Format**: JSON object with `jsonrpc`, `method`, `params`, and `id`
2. **Response Format**: JSON object with `jsonrpc`, `result`/`error`, and `id`
3. **Error Handling**: Standardized error codes and messages
4. **Batch Requests**: Multiple requests in a single HTTP call
5. **Transport**: Uses HTTP POST (can be adapted to other transports)

## Testing with curl

You can also test the server using curl:

```bash
# Single request
curl -X POST http://localhost:3000 \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"add","params":[5,3],"id":1}'

# Batch request
curl -X POST http://localhost:3000 \
  -H "Content-Type: application/json" \
  -d '[{"jsonrpc":"2.0","method":"add","params":[1,2],"id":1},{"jsonrpc":"2.0","method":"multiply","params":[3,4],"id":2}]'
```

