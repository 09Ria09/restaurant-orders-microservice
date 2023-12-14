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


    @Autowired
    public OrderFacade(OrderDatabase orderDatabase, UserMicroservice userMicroservice,
                       PaymentService paymentService) {
        this.orderDatabase = orderDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
    }

    @Override
    public void payForOrder(long userId, long orderId, String paymentConfirmation)
            throws MalformedException, ForbiddenException {
        Order order = orderDatabase.getById(orderId);

        if(order == null || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new MalformedException();
        }

        UsersGetUserTypeIdGet200Response.UserTypeEnum uType = null;

        try {
            uType = userMicroservice.getUserType(userId);
        } catch (Exception _e) {
            throw new MalformedException();
        }

        if(uType == null) {
            // This theoretically should never be allowed to happen
            // but just for safety we will perform this check.
            throw new MalformedException();
        }

        if(order.getCourierID() != userId || !paymentService.verifyPaymentConfirmation(paymentConfirmation))
            throw new ForbiddenException();

        order.setStatus(Order.StatusEnum.PENDING);

        orderDatabase.save(order);
    }
}
