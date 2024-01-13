package nl.tudelft.sem.orders.ring0.payment;

import java.util.Objects;
import lombok.Generated;

@Generated
public class Payment {
    private long userId;
    private long orderId;
    private String token;

    /**
     * Create a new payment object.
     *
     * @param userId The customers id.
     * @param orderId The id of the order.
     * @param token The payment token.
     */
    public Payment(long userId, long orderId, String token) {
        this.userId = userId;
        this.orderId = orderId;
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Payment payment = (Payment) o;
        return userId == payment.userId && orderId == payment.orderId
            && Objects.equals(token, payment.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, orderId, token);
    }
}
