# RPC Quick Reference Guide

A concise reference for RPC concepts, patterns, and implementations.

## RPC Patterns

### 1. Unary RPC
- **Pattern**: One request → One response
- **Use Case**: Standard request/response operations
- **Example**: Get user by ID, create resource

### 2. Server Streaming
- **Pattern**: One request → Stream of responses
- **Use Case**: Server pushes multiple results
- **Example**: List users, real-time updates

### 3. Client Streaming
- **Pattern**: Stream of requests → One response
- **Use Case**: Client sends multiple items, server processes batch
- **Example**: Upload multiple files, batch creation

### 4. Bidirectional Streaming
- **Pattern**: Stream of requests ↔ Stream of responses
- **Use Case**: Real-time bidirectional communication
- **Example**: Chat, gaming, live collaboration

## RPC Call Semantics

| Semantics | Description | Use When |
|-----------|-------------|----------|
| **At-Least-Once** | May execute multiple times | Idempotent operations |
| **At-Most-Once** | Executes at most once | Non-critical operations |
| **Exactly-Once** | Executes exactly once | Critical operations (complex) |

## Protocol Comparison

| Protocol | Format | Transport | Performance | Use Case |
|----------|--------|-----------|-------------|----------|
| **gRPC** | Binary (protobuf) | HTTP/2 | Very High | Microservices, high-performance |
| **JSON-RPC** | Text (JSON) | HTTP | Medium | Web APIs, simple services |
| **XML-RPC** | Text (XML) | HTTP | Low | Legacy systems |
| **Thrift** | Binary | TCP/HTTP | High | Cross-language services |
| **REST** | Text (JSON/XML) | HTTP | Medium | Web APIs, CRUD operations |

## gRPC Status Codes

| Code | Name | Description |
|------|------|-------------|
| 0 | OK | Success |
| 1 | CANCELLED | Operation cancelled |
| 2 | UNKNOWN | Unknown error |
| 3 | INVALID_ARGUMENT | Invalid parameters |
| 4 | DEADLINE_EXCEEDED | Timeout |
| 5 | NOT_FOUND | Resource not found |
| 6 | ALREADY_EXISTS | Resource exists |
| 7 | PERMISSION_DENIED | Authorization failure |
| 8 | RESOURCE_EXHAUSTED | Out of resources |
| 9 | FAILED_PRECONDITION | Precondition failed |
| 10 | ABORTED | Operation aborted |
| 11 | OUT_OF_RANGE | Out of valid range |
| 12 | UNIMPLEMENTED | Not implemented |
| 13 | INTERNAL | Internal error |
| 14 | UNAVAILABLE | Service unavailable |
| 15 | DATA_LOSS | Data loss |

## JSON-RPC 2.0 Error Codes

| Code | Name | Description |
|------|------|-------------|
| -32700 | Parse error | Invalid JSON |
| -32600 | Invalid Request | Malformed request |
| -32601 | Method not found | Method doesn't exist |
| -32602 | Invalid params | Invalid parameters |
| -32603 | Internal error | Server error |
| -32000 to -32099 | Server error | Server-defined errors |

## Best Practices Checklist

### Design
- [ ] Use appropriate RPC pattern (unary vs streaming)
- [ ] Design idempotent operations
- [ ] Version your APIs
- [ ] Use strong typing (IDL/schemas)
- [ ] Design for backward compatibility

### Implementation
- [ ] Set appropriate timeouts
- [ ] Implement retries with exponential backoff
- [ ] Use connection pooling
- [ ] Implement circuit breakers
- [ ] Add request/response logging

### Error Handling
- [ ] Use standard error codes
- [ ] Provide meaningful error messages
- [ ] Distinguish retryable vs non-retryable errors
- [ ] Implement proper error propagation

### Security
- [ ] Use TLS for encryption
- [ ] Implement authentication
- [ ] Validate all inputs
- [ ] Implement rate limiting
- [ ] Use least privilege principle

### Performance
- [ ] Enable compression (gzip, snappy)
- [ ] Use streaming for large data
- [ ] Implement caching where appropriate
- [ ] Monitor latency (p50, p95, p99)
- [ ] Use load balancing

### Observability
- [ ] Add distributed tracing
- [ ] Log with request IDs
- [ ] Track metrics (QPS, error rate, latency)
- [ ] Set up alerts
- [ ] Use structured logging

## Common Patterns

### Retry Pattern
```python
# Exponential backoff retry
import time
import random

def retry_with_backoff(func, max_retries=3):
    for attempt in range(max_retries):
        try:
            return func()
        except Exception as e:
            if attempt == max_retries - 1:
                raise
            wait_time = (2 ** attempt) + random.uniform(0, 1)
            time.sleep(wait_time)
```

### Circuit Breaker Pattern
```python
class CircuitBreaker:
    def __init__(self, failure_threshold=5, timeout=60):
        self.failure_count = 0
        self.failure_threshold = failure_threshold
        self.timeout = timeout
        self.state = 'CLOSED'  # CLOSED, OPEN, HALF_OPEN
        self.last_failure_time = None
    
    def call(self, func):
        if self.state == 'OPEN':
            if time.time() - self.last_failure_time > self.timeout:
                self.state = 'HALF_OPEN'
            else:
                raise Exception("Circuit breaker is OPEN")
        
        try:
            result = func()
            if self.state == 'HALF_OPEN':
                self.state = 'CLOSED'
                self.failure_count = 0
            return result
        except Exception as e:
            self.failure_count += 1
            self.last_failure_time = time.time()
            if self.failure_count >= self.failure_threshold:
                self.state = 'OPEN'
            raise
```

### Service Discovery Pattern
```python
# Simple service discovery
class ServiceRegistry:
    def __init__(self):
        self.services = {}
    
    def register(self, name, address, port):
        self.services[name] = f"{address}:{port}"
    
    def discover(self, name):
        return self.services.get(name)
```

## Performance Tips

1. **Connection Reuse**: Always reuse connections when possible
2. **Batching**: Batch multiple small requests into one
3. **Compression**: Enable compression for large payloads
4. **Streaming**: Use streaming for large data transfers
5. **Async**: Use async/await for concurrent requests
6. **Caching**: Cache frequently accessed data
7. **Load Balancing**: Distribute load across multiple servers

## Debugging Tips

1. **Request IDs**: Include unique request IDs for tracing
2. **Logging**: Log all requests and responses (sanitize sensitive data)
3. **Tracing**: Use distributed tracing (Jaeger, Zipkin)
4. **Metrics**: Monitor latency, error rate, throughput
5. **Timeouts**: Set appropriate timeouts to detect hanging requests
6. **Health Checks**: Implement health check endpoints

## Common Pitfalls

1. **Ignoring Timeouts**: Always set timeouts
2. **No Retry Logic**: Implement retries for transient failures
3. **Blocking Calls**: Don't block on RPC calls unnecessarily
4. **No Error Handling**: Always handle errors gracefully
5. **Ignoring Versioning**: Plan for API versioning from start
6. **Security**: Don't skip authentication/authorization
7. **Monitoring**: Don't deploy without monitoring

## Quick Commands

### gRPC
```bash
# Generate code from proto
protoc --python_out=. --grpc_python_out=. service.proto

# Python
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. service.proto

# Go
protoc --go_out=. --go-grpc_out=. service.proto
```

### Testing
```bash
# Test gRPC with grpcurl
grpcurl -plaintext localhost:50051 list
grpcurl -plaintext localhost:50051 UserService.GetUser

# Test JSON-RPC with curl
curl -X POST http://localhost:3000 \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"add","params":[1,2],"id":1}'
```

## Further Reading

- See `RPC_Complete_Guide.md` for comprehensive documentation
- Check `rpc_examples/` for practical implementations
- Refer to official documentation for specific frameworks

---

*Last Updated: 2024*

