Low-Level Design: ATM Machine System 💰
An ATM (Automated Teller Machine) system is designed to efficiently handle customer banking operations, authenticate users, process transactions, and manage cash inventory. The system needs to support multiple transaction types, maintain security, handle various states of operation, and provide a seamless user experience. The system should be reliable and capable of handling different machine states and transaction processing.

‍

Rules of the System:
Setup:

• The ATM has a cash inventory with different denominations.

• Cards have attributes like card number, PIN, and associated account details.

• The system authenticates users before allowing access to their accounts.

‍

Operation:

• Users insert their cards and enter their PIN for authentication.

• The ATM has several states: idle, has card, select operation, cash withdrawal, check balance.

• Once authenticated, users can select from available operations (withdraw cash, check balance, etc.).

• After completing operations, the card is returned to the user.

‍

Safety Features:

• The system validates PIN for secure authentication.

• Transaction validation ensures secure and accurate processing.

• The system maintains transaction logs for auditing.

• Maintenance mode prevents user interaction during servicing.