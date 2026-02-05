package h08;

public class TransactionException extends Exception {
    public TransactionException(String message, long transactionNumber) {
        super(message + " " + transactionNumber);
    }

    public TransactionException(Transaction[] transactions) {
        super(buildMessage(transactions));
    }

    private static String buildMessage(Transaction[] transactions) {
        StringBuilder sb = new StringBuilder("Transaction numbers: [");
        for (int i = 0; i < transactions.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(transactions[i].transactionNumber());
        }
        sb.append("]");
        return sb.toString();
    }
}


