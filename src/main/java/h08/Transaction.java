package h08;

import java.time.LocalDate;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * Represents a transaction between two accounts.
 *
 * @param sourceAccount     the source account of the transaction
 * @param targetAccount     the target account of the transaction
 * @param amount            the amount of money that is transferred
 * @param transactionNumber the transaction number of the transaction
 * @param description       the description of the transaction
 * @param date              the date of the transaction
 * @param status            the status of the transaction
 */
public record Transaction(
    Account sourceAccount,
    Account targetAccount,
    double amount,
    long transactionNumber,
    String description,
    LocalDate date,
    Status status
) {

    /**
     * Constructs a new transaction with the specified source account, target account, amount, transaction number,
     *
     * @param sourceAccount     the source account of the transaction
     * @param targetAccount     the target account of the transaction
     * @param amount            the amount of money that is transferred
     * @param transactionNumber the transaction number of the transaction
     * @param description       the description of the transaction
     * @param date              the date of the transaction
     * @param status            the status of the transaction
     * @throws BadTimestampException if the date is in the future
     */
    public Transaction {
assert sourceAccount!=null && targetAccount!=null && description!=null && date!=null && status!=null;
if (date.isAfter(LocalDate.now())){
    throw new BadTimestampException(date);
}

    }

}
