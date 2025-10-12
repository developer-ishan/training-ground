## Problem Statement

You are building a payment processing module for an e-commerce platform.
Customers can choose different modes of payment such as:

- Credit Card
- PayPal
- UPI

Each payment method has its own algorithm to process a payment.

Your task is to design a flexible and extensible system using the **Strategy Design Pattern**, such that:

1. You can easily add new payment methods in the future without modifying existing code.
2. The system allows switching between different payment strategies at runtime.

## Requirements

- Define a common interface for all payment strategies.
- Implement at least three different concrete strategies: `CreditCardPayment`, `PayPalPayment`, and `UPIPayment`.
- Create a `PaymentContext` class that uses a `PaymentStrategy` to process the payment.
- Demonstrate changing strategies at runtime.

## Bonus Requirements

- Add a feature where each strategy logs the transaction differently.
- Add validation logic specific to each payment method.
- Write unit tests to ensure each strategy works correctly and can be swapped dynamically.
