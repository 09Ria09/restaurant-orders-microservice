package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.result.VerificationException;

public abstract class BaseValidator implements Validator {
    private transient Validator next;

    public void setNext(Validator h) {
        this.next = h;
    }

    protected void checkNext(Payment transfer) throws
        VerificationException {
        if (next == null) {
            return;
        }

        next.verify(transfer);
    }
}
