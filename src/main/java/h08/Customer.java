package h08;

import java.time.LocalDate;
import java.time.Period;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * Represents a customer.
 *
 * @param firstName   the first name of the customer
 * @param lastName    the last name of the customer
 * @param address     the address of the customer
 * @param dateOfBirth the date of birth of the customer
 */
public record Customer(
    String firstName,
    String lastName,
    String address,
    LocalDate dateOfBirth
) {
    /**
     * The minimum age of a customer.
     */
    private static final int MIN_AGE = 18;

    /**
     * Constructs a new customer.
     *
     * @param firstName   the first name of the customer
     * @param lastName    the last name of the customer
     * @param address     the address of the customer
     * @param dateOfBirth the date of birth of the customer
     */
    public Customer {
assert firstName!=null && lastName!=null && address!=null && dateOfBirth!=null;
LocalDate today=LocalDate.now();
if (Period.between(dateOfBirth, today).getYears()<18){
 throw new BadTimestampException(dateOfBirth);
}
    }

}
