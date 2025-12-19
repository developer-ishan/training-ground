# RPC Learning Resources

Complete learning materials for Remote Procedure Call (RPC) covering theory, practice, and real-world implementations.

## 📚 Contents

### 1. [RPC Complete Guide](./RPC_Complete_Guide.md)
**Comprehensive guide covering:**
- Background and history of RPC
- Core concepts and theory
- RPC architecture and components
- Different RPC implementations (gRPC, JSON-RPC, Thrift, etc.)
- Protocols and standards
- Best practices
- Common challenges and solutions
- Modern RPC frameworks
- Extensive references (25+ sources)

### 2. [RPC Quick Reference](./RPC_Quick_Reference.md)
**Quick reference guide with:**
- RPC patterns cheat sheet
- Protocol comparisons
- Error codes reference
- Best practices checklist
- Common patterns (retry, circuit breaker, service discovery)
- Performance tips
- Debugging tips
- Quick commands

### 3. [Practical Examples](./rpc_examples/)
**Working code examples:**

#### gRPC Python Example
- Complete gRPC server and client
- Demonstrates all 4 RPC patterns:
  - Unary RPC
  - Server Streaming
  - Client Streaming
  - Bidirectional Streaming
- Protocol Buffer definitions
- Error handling examples

#### JSON-RPC Node.js Example
- Full JSON-RPC 2.0 implementation
- Calculator service
- Batch request support
- Comprehensive error handling
- Client examples

## 🚀 Getting Started

### For Theory and Concepts
1. Start with [RPC Complete Guide](./RPC_Complete_Guide.md) - Read sections 1-4 for fundamentals
2. Use [RPC Quick Reference](./RPC_Quick_Reference.md) as a cheat sheet while learning

### For Hands-On Practice
1. **gRPC Python Example:**
   ```bash
   cd rpc_examples/grpc_python
   ./setup.sh
   python3 server.py  # Terminal 1
   python3 client.py  # Terminal 2
   ```

2. **JSON-RPC Node.js Example:**
   ```bash
   cd rpc_examples/jsonrpc_nodejs
   npm start          # Terminal 1
   npm run client     # Terminal 2
   ```

## 📖 Learning Path

### Beginner
1. Read "Introduction" and "Background" sections
2. Understand basic RPC model
3. Run the JSON-RPC example (simpler to understand)
4. Read about marshalling/unmarshalling

### Intermediate
1. Study RPC architecture in detail
2. Understand different RPC patterns
3. Run the gRPC example
4. Learn about error handling and timeouts
5. Study best practices

### Advanced
1. Deep dive into specific implementations (gRPC, Thrift)
2. Learn about service discovery
3. Study distributed systems concepts
4. Implement your own RPC service
5. Explore performance optimization
6. Study monitoring and observability

## 🎯 Key Concepts to Master

1. **RPC Patterns**
   - Unary (request/response)
   - Server streaming
   - Client streaming
   - Bidirectional streaming

2. **Core Components**
   - Client stub
   - Server stub
   - RPC runtime
   - Marshalling/Unmarshalling

3. **Call Semantics**
   - At-least-once
   - At-most-once
   - Exactly-once

4. **Protocols**
   - gRPC (Protocol Buffers)
   - JSON-RPC
   - XML-RPC
   - Thrift

5. **Best Practices**
   - Error handling
   - Timeouts and retries
   - Security
   - Performance optimization
   - Monitoring

## 🔗 Additional Resources

All references are included in the [Complete Guide](./RPC_Complete_Guide.md), including:
- Academic papers
- Official documentation
- Books
- Online resources
- Tools and libraries

## 💡 Practice Projects

After completing the examples, try building:

1. **Distributed Calculator**
   - Multiple services (add, subtract, multiply, divide)
   - Service discovery
   - Load balancing

2. **Chat Application**
   - Bidirectional streaming
   - Multiple clients
   - Message persistence

3. **File Transfer Service**
   - Client streaming for uploads
   - Server streaming for downloads
   - Progress tracking

4. **Microservices Architecture**
   - User service
   - Order service
   - Payment service
   - Inter-service communication via RPC

5. **Real-time Dashboard**
   - Server streaming for updates
   - Multiple data sources
   - Aggregation service

## 📝 Notes

- All examples are production-ready patterns but simplified for learning
- Error handling is demonstrated but can be enhanced
- Security features (TLS, authentication) should be added for production
- Monitoring and observability should be implemented in real systems

## 🤝 Contributing

Feel free to:
- Add more examples
- Improve documentation
- Fix bugs
- Add more language implementations

## 📄 License

This is educational material. Use freely for learning purposes.

---

**Happy Learning! 🎓**

*Last Updated: 2024*

