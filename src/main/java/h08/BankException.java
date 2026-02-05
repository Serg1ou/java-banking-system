package h08;

public class BankException extends Exception{
    public BankException(String message){
        super(message);
    }
    public BankException(long bic){
        super("Cannot find Bank with BIC: "+bic);
    }
}
