package h08;

import java.util.NoSuchElementException;
import java.util.Arrays;

public class TransactionHistory {

    private static final int DEFAULT_CAPACITY = 10;
    private final int capacity;
    private final Transaction[] transactions;
    private int nextIndex = 0;
    private int size = 0;

    public TransactionHistory(int capacity) {
        this.capacity = capacity;
        this.transactions = new Transaction[capacity];
    }

    TransactionHistory(TransactionHistory history, int capacity) {
        this.capacity = capacity;
        this.transactions = new Transaction[capacity];
        System.arraycopy(history.transactions, 0, this.transactions, 0, Math.min(capacity, history.size));
        this.size = Math.min(capacity, history.size);
        this.nextIndex = this.size % capacity;
    }

    public TransactionHistory() {
        this(DEFAULT_CAPACITY);
    }

    public void add(Transaction transaction) {
        for (int x = 0; x < size; x++) {
            if (transaction.transactionNumber() == transactions[x].transactionNumber()) {
                throw new IllegalArgumentException("This transaction already exists!");
            }
        }
        transactions[nextIndex] = transaction;
        nextIndex = (nextIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    public void update(Transaction transaction) throws TransactionException {
        boolean transactionFinder = false;
        for (int x = 0; x < size; x++) {
            if (transaction.transactionNumber() == transactions[x].transactionNumber()) {
                transactions[x] = transaction;
                transactionFinder = true;
                break;
            }
        }
        if (!transactionFinder) {
            throw new TransactionException("Transaction does not exist!", transaction.transactionNumber());
        }
    }

    public Transaction get(long transactionNumber) {
        for (int i = 0; i < size; i++) {
            Transaction transaction = transactions[i];
            if (transaction.transactionNumber() == transactionNumber) {
                return transaction;
            }
        }
        throw new NoSuchElementException(String.valueOf(transactionNumber));
    }

    public Transaction get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
        // oldest-first order: transactions[(nextIndex - size + index + capacity) % capacity]
        int realIndex = Math.floorMod(nextIndex - size + index, capacity);
        return transactions[realIndex];
    }

    public int size() { return size; }
    public int capacity() { return capacity; }

    public Transaction getLatestTransaction() {
        if (size == 0) throw new IllegalStateException("No transactions yet!");
        int idx = Math.floorMod(nextIndex - 1, capacity);
        return transactions[idx];
    }

    public Transaction[] getTransactions() {
        Transaction[] availableTransactions = new Transaction[size];
        for (int i = 0; i < size; i++) {
            availableTransactions[i] = get(i);
        }
        return availableTransactions;
    }

    public Transaction[] getTransactions(Status status) {
        int length = 0;
        for (int i = 0; i < size; i++) {
            Transaction t = get(i);
            if (t.status() == status) length++;
        }
        Transaction[] available = new Transaction[length];
        int pos = 0;
        for (int i = 0; i < size; i++) {
            Transaction t = get(i);
            if (t.status() == status) available[pos++] = t;
        }
        return available;
    }
}

