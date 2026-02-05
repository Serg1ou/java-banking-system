package h08;

import java.time.LocalDate;
import java.util.*;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * Represents a bank. A bank offers accounts to its customers and allows them to transfer money to other accounts.
 */
public class Bank {

    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_TRANSACTION_CAPACITY = 10;

    private final String name;
    private final int bic;
    private Bank[] transferableBanks;
    private final Account[] accounts;
    private final int capacity;
    private int size = 0;
    private int transactionHistoryCapacity = DEFAULT_TRANSACTION_CAPACITY;

    public Bank(String name, int bic, int capacity) {
        this.name = name;
        this.bic = bic;
        this.accounts = new Account[capacity];
        this.capacity = capacity;
        this.transferableBanks = new Bank[0];
    }

    public Bank(String name, int bic) {
        this(name, bic, DEFAULT_CAPACITY);
    }

    public String getName() { return name; }
    public int getBic() { return bic; }
    public Account[] getAccounts() {
        Account[] availableAccounts = new Account[size];
        System.arraycopy(accounts, 0, availableAccounts, 0, size);
        return availableAccounts;
    }
    public Bank[] getTransferableBanks() { return transferableBanks; }
    public int capacity() { return capacity; }
    public int size() { return size; }
    public int transactionCapacity() { return transactionHistoryCapacity; }

    public void setTransactionHistoryCapacity(int transactionHistoryCapacity) {
        this.transactionHistoryCapacity = transactionHistoryCapacity;
        for (int i = 0; i < size; i++) {
            Account account = accounts[i];
            account.setHistory(new TransactionHistory(account.getHistory(), transactionHistoryCapacity));
        }
    }

    protected boolean isIbanAlreadyUsed(long iban) {
        for (int x = 0; x < size; x++) {
            if (accounts[x].getIban() == iban) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate IBAN using seed; if collision -> try next seeds (seed++).
     */
    protected long generateIban(Customer customer, long seed) {
        long s = seed;
        long generated;
        // protect against hashCode * s overflow producing same value repeatedly:
        do {
            long v = (long) customer.hashCode() * s;
            if (v == Long.MIN_VALUE) v = 0;
            generated = Math.abs(v);
            s++;
        } while (isIbanAlreadyUsed(generated));
        return generated;
    }

    /**
     * Add a new account for customer. Throws IllegalStateException when bank full.
     */
    public void add(Customer customer) {
        if (size >= capacity) {
            throw new IllegalStateException("Bank is full");
        }
        long iban = generateIban(customer, System.nanoTime());
        TransactionHistory th = new TransactionHistory(transactionHistoryCapacity);
        Account account = new Account(customer, iban, 0.0, this, th);
        accounts[size++] = account;
    }

    public void add(Bank bank) {
        for (Bank transferableBank : transferableBanks) {
            if (transferableBank.getBic() == bank.getBic()) {
                throw new IllegalArgumentException("Cannot add duplicates!");
            }
        }
        Bank[] newTransferableBanks = new Bank[transferableBanks.length + 1];
        System.arraycopy(transferableBanks, 0, newTransferableBanks, 0, transferableBanks.length);
        newTransferableBanks[transferableBanks.length] = bank;
        transferableBanks = newTransferableBanks;
    }

    public int getAccountIndex(long iban) {
        for (int i = 0; i < size; i++) {
            if (accounts[i].getIban() == iban)
                return i;
        }
        throw new NoSuchElementException(String.valueOf(iban));
    }

    public Account remove(long iban) {
        // getAccountIndex throws if not present
        int numberinArray = getAccountIndex(iban);
        Account removedAccount = accounts[numberinArray];
        for (int x = numberinArray; x < size - 1; x++) {
            accounts[x] = accounts[x + 1];
        }
        accounts[size - 1] = null;
        size--;
        return removedAccount;
    }

    private int getBankIndex(int bic) {
        for (int i = 0; i < transferableBanks.length; i++) {
            if (transferableBanks[i].getBic() == bic)
                return i;
        }
        throw new NoSuchElementException(String.valueOf(bic));
    }

    private Bank getBank(int bic) {
        return transferableBanks[getBankIndex(bic)];
    }

    public Bank remove(int bic) {
        assert bic >= 0;
        int index = getBankIndex(bic);
        Bank removedBank = transferableBanks[index];
        Bank[] newTransferableBanks = new Bank[transferableBanks.length - 1];
        System.arraycopy(transferableBanks, 0, newTransferableBanks, 0, index);
        System.arraycopy(transferableBanks, index + 1, newTransferableBanks, index, transferableBanks.length - index - 1);
        transferableBanks = newTransferableBanks;
        return removedBank;
    }

    public void deposit(long iban, double amount) {
        if (amount <= 0) throw new IllegalArgumentException(String.valueOf(amount));
        int index = getAccountIndex(iban); // throws if not found
        accounts[index].setBalance(accounts[index].getBalance() + amount);
    }

    public void withdraw(long iban, double amount) {
        if (amount <= 0) throw new IllegalArgumentException(String.valueOf(amount));
        int index = getAccountIndex(iban); // throws if not found
        if (accounts[index].getBalance() < amount) {
            throw new IllegalArgumentException(String.valueOf(accounts[index].getBalance() - amount));
        }
        accounts[index].setBalance(accounts[index].getBalance() - amount);
    }

    protected long generateTransactionNumber() {
        return System.nanoTime();
    }

    /**
     * Transfer: looks up sender in this bank; receiver may be in this bank or a transferable bank.
     * Updates both histories and returns CLOSED or CANCELLED.
     */
    public Status transfer(long senderIBAN, long receiverIBAN, int receiverBIC, double amount, String description) {
        try {
            Account sender = accounts[getAccountIndex(senderIBAN)];
            Account receiver;
            Bank receiverBank;
            if (receiverBIC == this.bic) {
                receiverBank = this;
                receiver = accounts[getAccountIndex(receiverIBAN)];
            } else {
                receiverBank = getBank(receiverBIC); // may throw
                int idx = receiverBank.getAccountIndex(receiverIBAN);
                receiver = receiverBank.accounts[idx];
            }

            Transaction openTx = new Transaction(sender, receiver, amount, generateTransactionNumber(), description, LocalDate.now(), Status.OPEN);
            sender.getHistory().add(openTx);
            receiver.getHistory().add(openTx);

            try {
                // perform changing balances on correct banks
                this.withdraw(senderIBAN, amount);
                receiverBank.deposit(receiverIBAN, amount);

                Transaction closed = new Transaction(sender, receiver, amount, openTx.transactionNumber(), description, LocalDate.now(), Status.CLOSED);
                sender.getHistory().update(closed);
                receiver.getHistory().update(closed);
                return Status.CLOSED;
            } catch (Exception e) {
                Transaction cancelled = new Transaction(sender, receiver, amount, openTx.transactionNumber(), description, LocalDate.now(), Status.CANCELLED);
                // update histories to cancelled
                try { sender.getHistory().update(cancelled); } catch (TransactionException ignored) {}
                try { receiver.getHistory().update(cancelled); } catch (TransactionException ignored) {}
                return Status.CANCELLED;
            }
        } catch (NoSuchElementException e) {
            return Status.CANCELLED;
        }
    }

    /**
     * Check open transactions: iterate only actual accounts (0..size-1).
     * - If OPEN and older than 4 weeks -> collect and throw TransactionException
     * - If OPEN and older than 2 weeks (but <=4) -> try to re-do transfer (i.e. create new open tx and leave it open)
     * Returns array of transactions that were re-triggered (or otherwise relevant) — adjust to tests expectations.
     */
    public Transaction[] checkOpenTransactions() throws TransactionException {
        List<Transaction> reopened = new ArrayList<>();
        List<Long> tooOld = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Account account = accounts[i];
            Transaction[] hist = account.getHistory().getTransactions();
            for (Transaction t : hist) {
                if (t == null) continue;
                if (t.status() != Status.OPEN) continue;
                // compute age in days (approx)
                long days = java.time.temporal.ChronoUnit.DAYS.between(t.date(), LocalDate.now());
                if (days > 28) { // older than 4 weeks
                    tooOld.add(t.transactionNumber());
                } else if (days > 14) { // older than 2 weeks (but <=4)
                    // try to re-transfer: create a new open transaction and add to target&source histories
                    Account src = t.sourceAccount();
                    Account tgt = t.targetAccount();
                    Transaction newTx = new Transaction(src, tgt, t.amount(), generateTransactionNumber(), t.description(), LocalDate.now(), Status.OPEN);
                    src.getHistory().add(newTx);
                    tgt.getHistory().add(newTx);
                    reopened.add(newTx);
                }
            }
        }

        if (!tooOld.isEmpty()) {
            // build message with transaction numbers
            long[] nums = tooOld.stream().mapToLong(Long::longValue).toArray();
            Transaction[] dummy = new Transaction[0]; // not used in constructor below
            // throw with the numbers joined; reuse TransactionException that accepts Transaction[] — but better throw with message
            StringBuilder sb = new StringBuilder("Transaction numbers: [");
            for (int i = 0; i < tooOld.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(tooOld.get(i));
            }
            sb.append("]");
            throw new TransactionException(sb.toString(), -1);
        }

        return reopened.toArray(new Transaction[0]);
    }

    // equals, hashCode, toString remain as before (omitted for brevity)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bank bank = (Bank) o;
        return getBic() == bank.getBic()
            && capacity() == bank.capacity()
            && size() == bank.size()
            && Objects.equals(getName(), bank.getName())
            && Arrays.equals(getAccounts(), bank.getAccounts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getBic(), capacity(), size(), Arrays.hashCode(getAccounts()));
    }

    @Override
    public String toString() {
        return "Bank{" +
            "name='" + name + '\'' +
            ", bic=" + bic +
            ", capacity=" + capacity +
            ", size=" + size +
            '}';
    }
}

