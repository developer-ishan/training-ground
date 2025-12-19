#!/usr/bin/env python3
"""
gRPC Client Example
Demonstrates calling various RPC patterns
"""

import grpc
import user_pb2
import user_pb2_grpc
import time

def run_unary_examples(stub):
    """Demonstrate unary RPC calls"""
    print("\n" + "=" * 50)
    print("UNARY RPC EXAMPLES")
    print("=" * 50)
    
    # Create a user
    print("\n1. Creating a user...")
    create_response = stub.CreateUser(
        user_pb2.CreateUserRequest(
            name="Alice",
            email="alice@example.com"
        )
    )
    print(f"   Created: {create_response.name} (ID: {create_response.id})")
    
    # Create another user
    create_response2 = stub.CreateUser(
        user_pb2.CreateUserRequest(
            name="Bob",
            email="bob@example.com"
        )
    )
    print(f"   Created: {create_response2.name} (ID: {create_response2.id})")
    
    # Get a user
    print("\n2. Getting user by ID...")
    get_response = stub.GetUser(
        user_pb2.GetUserRequest(user_id=1)
    )
    print(f"   Retrieved: {get_response.name} - {get_response.email}")
    
    # Try to get non-existent user
    print("\n3. Attempting to get non-existent user...")
    try:
        get_response = stub.GetUser(
            user_pb2.GetUserRequest(user_id=999)
        )
    except grpc.RpcError as e:
        print(f"   Error (expected): {e.code()} - {e.details()}")

def run_server_streaming_example(stub):
    """Demonstrate server streaming RPC"""
    print("\n" + "=" * 50)
    print("SERVER STREAMING RPC EXAMPLE")
    print("=" * 50)
    
    print("\nReceiving stream of users...")
    request = user_pb2.ListUsersRequest(limit=5, offset=0)
    
    for user in stub.ListUsers(request):
        print(f"   Received: {user.name} (ID: {user.id}) - {user.email}")

def run_client_streaming_example(stub):
    """Demonstrate client streaming RPC"""
    print("\n" + "=" * 50)
    print("CLIENT STREAMING RPC EXAMPLE")
    print("=" * 50)
    
    print("\nSending stream of user creation requests...")
    
    def generate_requests():
        names = ["Charlie", "Diana", "Eve"]
        for name in names:
            yield user_pb2.CreateUserRequest(
                name=name,
                email=f"{name.lower()}@example.com"
            )
            time.sleep(0.5)  # Simulate delay
    
    response = stub.CreateUsers(generate_requests())
    print(f"\n   Created {response.count} users:")
    for user in response.users:
        print(f"     - {user.name} (ID: {user.id})")

def run_bidirectional_streaming_example(stub):
    """Demonstrate bidirectional streaming RPC"""
    print("\n" + "=" * 50)
    print("BIDIRECTIONAL STREAMING RPC EXAMPLE")
    print("=" * 50)
    
    print("\nStarting chat session...")
    
    def generate_messages():
        messages = [
            "Hello, server!",
            "How are you?",
            "This is a bidirectional stream.",
            "Goodbye!"
        ]
        for msg in messages:
            yield user_pb2.ChatMessage(
                user="Client",
                message=msg,
                timestamp=int(time.time())
            )
            time.sleep(1)
    
    print("\nSending messages and receiving responses:")
    for response in stub.Chat(generate_messages()):
        print(f"   {response.user}: {response.message}")

def main():
    """Main function to run all examples"""
    # Create a channel to the server
    channel = grpc.insecure_channel('localhost:50051')
    stub = user_pb2_grpc.UserServiceStub(channel)
    
    try:
        # Test connection
        print("Connecting to gRPC server...")
        
        # Run all examples
        run_unary_examples(stub)
        run_server_streaming_example(stub)
        run_client_streaming_example(stub)
        run_bidirectional_streaming_example(stub)
        
        print("\n" + "=" * 50)
        print("All examples completed successfully!")
        print("=" * 50)
        
    except grpc.RpcError as e:
        print(f"RPC Error: {e.code()} - {e.details()}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        channel.close()

if __name__ == '__main__':
    main()

