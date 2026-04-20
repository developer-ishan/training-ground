package src;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.models.Expense;
import src.models.Settlement;
import src.models.User;
import src.models.Group;
import src.models.abstractions.Transaction;
import src.models.enums.SplitType;
import src.service.ExpenseService;

public class Main {

    public static void main(String[] args) {
        ExpenseService service = new ExpenseService();

        // ── 1. Add users ──────────────────────────────────────────────────────
        User alice   = service.addUser("Alice",   "alice@example.com");
        User bob     = service.addUser("Bob",     "bob@example.com");
        User charlie = service.addUser("Charlie", "charlie@example.com");
        User dave    = service.addUser("Dave",    "dave@example.com");

        // ── 2. Create group ───────────────────────────────────────────────────
        Group trip = service.createGroup("Goa Trip",
                List.of(alice.getId(), bob.getId(), charlie.getId(), dave.getId()));

        separator("SCENARIO: Goa Trip — 4 friends");

        // ── 3. Equal split: Alice pays hotel ₹400 ────────────────────────────
        // Each person owes ₹100
        service.addExpense(
                trip.getId(), alice.getId(),
                new BigDecimal("400"), "Hotel",
                List.of(alice.getId(), bob.getId(), charlie.getId(), dave.getId()),
                SplitType.EQUAL,
                Collections.emptyMap());

        System.out.println("Alice paid ₹400 for Hotel — split equally (₹100 each)");

        // ── 4. Exact split: Bob pays dinner ₹150 ─────────────────────────────
        // Alice: 60, Bob: 30, Charlie: 40, Dave: 20
        Map<String, BigDecimal> exactInput = new HashMap<>();
        exactInput.put(alice.getId(),   new BigDecimal("60"));
        exactInput.put(bob.getId(),     new BigDecimal("30"));
        exactInput.put(charlie.getId(), new BigDecimal("40"));
        exactInput.put(dave.getId(),    new BigDecimal("20"));

        service.addExpense(
                trip.getId(), bob.getId(),
                new BigDecimal("150"), "Dinner",
                List.of(alice.getId(), bob.getId(), charlie.getId(), dave.getId()),
                SplitType.EXACT,
                exactInput);

        System.out.println("Bob paid ₹150 for Dinner — exact split (Alice:60, Bob:30, Charlie:40, Dave:20)");

        // ── 5. Percentage split: Charlie pays transport ₹200 ─────────────────
        // Alice: 40%, Bob: 30%, Charlie: 20%, Dave: 10%
        Map<String, BigDecimal> pctInput = new HashMap<>();
        pctInput.put(alice.getId(),   new BigDecimal("40"));
        pctInput.put(bob.getId(),     new BigDecimal("30"));
        pctInput.put(charlie.getId(), new BigDecimal("20"));
        pctInput.put(dave.getId(),    new BigDecimal("10"));

        service.addExpense(
                trip.getId(), charlie.getId(),
                new BigDecimal("200"), "Transport",
                List.of(alice.getId(), bob.getId(), charlie.getId(), dave.getId()),
                SplitType.PERCENTAGE,
                pctInput);

        System.out.println("Charlie paid ₹200 for Transport — % split (Alice:40%, Bob:30%, Charlie:20%, Dave:10%)");

        // ── 6. Print balances before settlement ───────────────────────────────
        separator("BALANCES (before settlement)");
        service.printBalances(alice.getId(),   trip.getId());
        service.printBalances(bob.getId(),     trip.getId());
        service.printBalances(charlie.getId(), trip.getId());
        service.printBalances(dave.getId(),    trip.getId());

        // ── 7. Minimum transfers ──────────────────────────────────────────────
        separator("MINIMUM TRANSFERS TO SETTLE GROUP");
        service.printMinimumTransfers(trip.getId());

        // ── 8. Dave settles up with Alice ─────────────────────────────────────
        separator("SETTLEMENT: Dave pays Alice ₹100");
        service.settleUp(trip.getId(), dave.getId(), alice.getId(),
                new BigDecimal("100"), "Dave settling with Alice");

        // ── 9. Print balances after settlement ───────────────────────────────
        separator("BALANCES (after Dave's settlement)");
        service.printBalances(alice.getId(), trip.getId());
        service.printBalances(dave.getId(),  trip.getId());

        // ── 10. Group transaction history ─────────────────────────────────────
        separator("GROUP HISTORY");
        List<Transaction> history = service.getGroupHistory(trip.getId());
        for (Transaction t : history) {
            if (t instanceof Expense e) {
                System.out.printf("[EXPENSE]    %-12s paid %-8s  desc: %s%n",
                        e.getPaidBy().getName(), e.getAmount(), e.getDescription());
            } else if (t instanceof Settlement s) {
                System.out.printf("[SETTLEMENT] %-12s paid %-8s  to: %s%n",
                        s.getFromUser().getName(), s.getAmount(), s.getToUser().getName());
            }
        }
    }

    private static void separator(String title) {
        System.out.println("\n─── " + title + " " + "─".repeat(Math.max(0, 50 - title.length())));
    }
}
