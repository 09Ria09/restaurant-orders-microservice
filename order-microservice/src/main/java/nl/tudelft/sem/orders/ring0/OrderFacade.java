package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade implements OrderLogic {
    private transient OrderDatabase orderDatabase;
    private transient UserMicroservice userMicroservice;
    private transient PaymentService paymentService;


    /**
     * Creates a new order facade.
     *
     * @param orderDatabase The database output port.
     * @param userMicroservice The output port for the user microservice.
     * @param paymentService The output port for the payment service.
     */
    @Autowired
    public OrderFacade(OrderDatabase orderDatabase,
                       UserMicroservice userMicroservice,
                       PaymentService paymentService) {
        this.orderDatabase = orderDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
    }

    @Override
    public void payForOrder(long userId, long orderId,
                            String paymentConfirmation)
        throws MalformedException, ForbiddenException {
        Order order = orderDatabase.getById(orderId);

        if (order == null || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new MalformedException();
        }

        UsersGetUserTypeIdGet200Response.UserTypeEnum userType;

        try {
            userType = userMicroservice.getUserType(userId);
        } catch (Exception e) {
            throw new MalformedException();
        }

        // userType should never be null now
        // therefore there is no need to check for that.

        if (order.getCustomerID() != userId
            || !paymentService.verifyPaymentConfirmation(paymentConfirmation)) {
            throw new ForbiddenException();
        }

        order.setStatus(Order.StatusEnum.PENDING);

        orderDatabase.save(order);
    }
}
