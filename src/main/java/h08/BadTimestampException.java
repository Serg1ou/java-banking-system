package h08;

import java.time.LocalDate;

public class BadTimestampException extends RuntimeException{
    public BadTimestampException(LocalDate date){
        super("Bad timestamp: "+ date);
    }
}
