package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.result.VerificationException;

public interface Validator {
    void setNext(Validator nextStep);

    void verify(Payment payment) throws VerificationException;
}
