package src.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import src.models.*;
import src.models.abstractions.Transaction;
import src.models.enums.SplitType;
import src.models.enums.TransactionType;
import src.strategy.SplitStrategy;
import src.strategy.SplitStrategyFactory;

public class ExpenseService {

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Group> groups = new HashMap<>();
    private final SplitStrategyFactory factory = new SplitStrategyFactory();

    // -------------------------------------------------------------------------
    // User / Group management
    // -------------------------------------------------------------------------

    public User addUser(String name, String email) {
        User user = new User(UUID.randomUUID().toString(), name, email);
        users.put(user.getId(), user);
        return user;
    }

    public Group createGroup(String name, List<String> memberIds) {
        if (memberIds == null || memberIds.size() < 2) {
            throw new IllegalArgumentException("A group needs at least 2 members");
        }
        Group group = new Group(UUID.randomUUID().toString(), name);
        for (String memberId : memberIds) {
            group.addMember(getUser(memberId));
        }
        groups.put(group.getId(), group);
        return group;
    }

    public void addMemberToGroup(String groupId, String userId) {
        Group group = getGroup(groupId);
        User user = getUser(userId);
        group.addMember(user);
    }

    // -------------------------------------------------------------------------
    // Expense / Settlement
    // -------------------------------------------------------------------------

    public Expense addExpense(
            String groupId,
            String paidByUserId,
            BigDecimal amount,
            String description,
            List<String> participantIds,
            SplitType splitType,
            Map<String, BigDecimal> splitInput) {

        Group group = getGroup(groupId);
        User paidBy = getUser(paidByUserId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }
        if (!group.isMember(paidBy)) {
            throw new IllegalArgumentException("Payer " + paidBy.getName() + " is not a member of group " + group.getName());
        }

        List<User> participants = new ArrayList<>();
        for (String pid : participantIds) {
            User p = getUser(pid);
            if (!group.isMember(p)) {
                throw new IllegalArgumentException("Participant " + p.getName() + " is not a member of group " + group.getName());
            }
            participants.add(p);
        }

        // Resolve splitInput keys: String userId → User
        Map<User, BigDecimal> resolvedInput = new HashMap<>();
        if (splitInput != null) {
            for (Map.Entry<String, BigDecimal> e : splitInput.entrySet()) {
                resolvedInput.put(getUser(e.getKey()), e.getValue());
            }
        }

        SplitStrategy strategy = factory.getStrategy(splitType);
        Map<User, BigDecimal> splits = strategy.split(amount, participants, resolvedInput);

        Expense expense = new Expense(UUID.randomUUID().toString(), amount, description, paidBy, splits, splitType);
        group.addTransaction(expense);
        return expense;
    }

    public Settlement settleUp(String groupId, String fromUserId, String toUserId, BigDecimal amount, String description) {
        Group group = getGroup(groupId);
        User from = getUser(fromUserId);
        User to = getUser(toUserId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("fromUser and toUser must be different");
        }
        if (!group.isMember(from)) {
            throw new IllegalArgumentException(from.getName() + " is not a member of group " + group.getName());
        }
        if (!group.isMember(to)) {
            throw new IllegalArgumentException(to.getName() + " is not a member of group " + group.getName());
        }

        Settlement settlement = new Settlement(UUID.randomUUID().toString(), amount, description, from, to);
        group.addTransaction(settlement);
        return settlement;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<Transaction> getGroupHistory(String groupId) {
        return getGroup(groupId).getTransactions();
    }

    public void printBalances(String userId, String groupId) {
        User user = getUser(userId);
        Group group = getGroup(groupId);
        Map<User, BigDecimal> net = computeNetBalances(userId, groupId);

        System.out.println("Balances for " + user.getName() + " in group " + group.getName() + ":");
        boolean anyBalance = false;
        for (Map.Entry<User, BigDecimal> e : net.entrySet()) {
            BigDecimal balance = e.getValue();
            if (balance.abs().compareTo(new BigDecimal("0.01")) < 0) continue;
            anyBalance = true;
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  " + e.getKey().getName() + " owes you " + balance);
            } else {
                System.out.println("  You owe " + e.getKey().getName() + " " + balance.negate());
            }
        }
        if (!anyBalance) System.out.println("  All settled up!");
    }

    public void printMinimumTransfers(String groupId) {
        Group group = getGroup(groupId);
        Map<User, BigDecimal> net = new HashMap<>();
        for (User member : group.getMembers()) {
            net.put(member, BigDecimal.ZERO);
        }

        for (Transaction t : group.getTransactions()) {
            if (t.getType() != TransactionType.EXPENSE) continue;
            Expense expense = (Expense) t;
            net.merge(expense.getPaidBy(), expense.getAmount(), BigDecimal::add);
            for (Map.Entry<User, BigDecimal> split : expense.getSplits().entrySet()) {
                net.merge(split.getKey(), split.getValue().negate(), BigDecimal::add);
            }
        }

        BigDecimal threshold = new BigDecimal("0.01");
        List<Map.Entry<User, BigDecimal>> creditors = net.entrySet().stream()
                .filter(e -> e.getValue().compareTo(threshold) > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        List<Map.Entry<User, BigDecimal>> debtors = net.entrySet().stream()
                .filter(e -> e.getValue().compareTo(threshold.negate()) < 0)
                .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
                .collect(Collectors.toList());

        System.out.println("Minimum transfers to settle group " + group.getName() + ":");
        int ci = 0, di = 0;
        List<BigDecimal> cNet = creditors.stream().map(Map.Entry::getValue).collect(Collectors.toList());
        List<BigDecimal> dNet = debtors.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        while (ci < creditors.size() && di < debtors.size()) {
            BigDecimal transfer = cNet.get(ci).min(dNet.get(di).negate());
            System.out.printf("  %s pays %s: %s%n",
                    debtors.get(di).getKey().getName(),
                    creditors.get(ci).getKey().getName(),
                    transfer);
            cNet.set(ci, cNet.get(ci).subtract(transfer));
            dNet.set(di, dNet.get(di).add(transfer));
            if (cNet.get(ci).compareTo(threshold) < 0) ci++;
            if (dNet.get(di).compareTo(threshold.negate()) > 0) di++;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getUser(String userId) {
        User u = users.get(userId);
        if (u == null) throw new IllegalArgumentException("User not found: " + userId);
        return u;
    }

    private Group getGroup(String groupId) {
        Group g = groups.get(groupId);
        if (g == null) throw new IllegalArgumentException("Group not found: " + groupId);
        return g;
    }

    private Map<User, BigDecimal> computeNetBalances(String userId, String groupId) {
        User user = getUser(userId);
        Group group = getGroup(groupId);
        Map<User, BigDecimal> net = new HashMap<>();

        for (Transaction t : group.getTransactions()) {
            if (t.getType() == TransactionType.EXPENSE) {
                Expense expense = (Expense) t;
                if (expense.getPaidBy().equals(user)) {
                    for (Map.Entry<User, BigDecimal> split : expense.getSplits().entrySet()) {
                        if (!split.getKey().equals(user)) {
                            net.merge(split.getKey(), split.getValue(), BigDecimal::add);
                        }
                    }
                } else if (expense.getSplits().containsKey(user)) {
                    net.merge(expense.getPaidBy(), expense.getSplits().get(user).negate(), BigDecimal::add);
                }
            } else {
                Settlement s = (Settlement) t;
                if (s.getFromUser().equals(user)) {
                    net.merge(s.getToUser(), s.getAmount(), BigDecimal::add);
                } else if (s.getToUser().equals(user)) {
                    net.merge(s.getFromUser(), s.getAmount().negate(), BigDecimal::add);
                }
            }
        }
        return net;
    }
}
