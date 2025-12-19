#!/usr/bin/env python3
"""
gRPC Server Example
Demonstrates various RPC patterns: unary, server streaming, client streaming, bidirectional
"""

import grpc
from concurrent import futures
import time
import user_pb2
import user_pb2_grpc

class UserService(user_pb2_grpc.UserServiceServicer):
    """Implementation of the UserService"""
    
    def __init__(self):
        self.users = {}
        self.next_id = 1
        self.chat_history = []
    
    def GetUser(self, request, context):
        """Unary RPC: Get a single user by ID"""
        user_id = request.user_id
        if user_id in self.users:
            return self.users[user_id]
        
        # Set error status if user not found
        context.set_code(grpc.StatusCode.NOT_FOUND)
        context.set_details(f'User with ID {user_id} not found')
        return user_pb2.User()
    
    def CreateUser(self, request, context):
        """Unary RPC: Create a new user"""
        user = user_pb2.User(
            id=self.next_id,
            name=request.name,
            email=request.email,
            created_at=int(time.time())
        )
        self.users[self.next_id] = user
        self.next_id += 1
        print(f"Created user: {user.name} (ID: {user.id})")
        return user
    
    def ListUsers(self, request, context):
        """Server Streaming RPC: Stream list of users"""
        limit = request.limit if request.limit > 0 else len(self.users)
        offset = request.offset
        
        user_list = list(self.users.values())[offset:offset+limit]
        
        for user in user_list:
            yield user
            time.sleep(0.1)  # Simulate processing time
    
    def CreateUsers(self, request_iterator, context):
        """Client Streaming RPC: Create multiple users from stream"""
        created_users = []
        
        for create_request in request_iterator:
            user = user_pb2.User(
                id=self.next_id,
                name=create_request.name,
                email=create_request.email,
                created_at=int(time.time())
            )
            self.users[self.next_id] = user
            created_users.append(user)
            self.next_id += 1
            print(f"Created user from stream: {user.name} (ID: {user.id})")
        
        return user_pb2.CreateUsersResponse(
            count=len(created_users),
            users=created_users
        )
    
    def Chat(self, request_iterator, context):
        """Bidirectional Streaming RPC: Chat between client and server"""
        for chat_message in request_iterator:
            print(f"Received: {chat_message.user}: {chat_message.message}")
            
            # Echo the message back with server prefix
            response = user_pb2.ChatMessage(
                user="Server",
                message=f"Echo: {chat_message.message}",
                timestamp=int(time.time())
            )
            
            self.chat_history.append(chat_message)
            yield response

def serve():
    """Start the gRPC server"""
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    
    # Listen on all interfaces, port 50051
    server.add_insecure_port('[::]:50051')
    server.start()
    
    print("=" * 50)
    print("gRPC Server started on port 50051")
    print("=" * 50)
    print("\nAvailable RPC methods:")
    print("  - GetUser (Unary)")
    print("  - CreateUser (Unary)")
    print("  - ListUsers (Server Streaming)")
    print("  - CreateUsers (Client Streaming)")
    print("  - Chat (Bidirectional Streaming)")
    print("\nPress Ctrl+C to stop the server")
    print("=" * 50)
    
    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        server.stop(0)

if __name__ == '__main__':
    serve()

