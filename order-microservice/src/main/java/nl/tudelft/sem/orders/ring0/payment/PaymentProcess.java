package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.PaymentException;
import nl.tudelft.sem.orders.result.VerificationException;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcess {

    private final transient OrderDatabase orderDatabase;
    private final transient PaymentService paymentService;
    private final transient DistanceValidator distanceValidator;
    private final transient StatusValidator statusValidator;
    private final transient TokenValidator tokenValidator;
    private final transient UserOwnershipValidator userOwnershipValidator;
    private final transient DeliveryMicroservice deliveryMicroservice;

    /**
     * Constructor for a PaymentProcess class.
     *
     * @param orderDatabase The outgoing port for database of orders
     * @param paymentService The paymentService
     * @param distanceValidator The distance Validator
     * @param statusValidator The status validator
     * @param tokenValidator The token validator
     * @param userOwnershipValidator The userOwnershipValidator
     * @param deliveryMicroservice The outgoing port for the delivery Microservice
     */
    public PaymentProcess(OrderDatabase orderDatabase,
                          PaymentService paymentService,
                          DistanceValidator distanceValidator,
                          StatusValidator statusValidator,
                          TokenValidator tokenValidator,
                          UserOwnershipValidator userOwnershipValidator,
                          DeliveryMicroservice deliveryMicroservice) {
        this.orderDatabase = orderDatabase;
        this.paymentService = paymentService;
        this.distanceValidator = distanceValidator;
        this.statusValidator = statusValidator;
        this.tokenValidator = tokenValidator;
        this.userOwnershipValidator = userOwnershipValidator;
        this.deliveryMicroservice = deliveryMicroservice;
    }

    /**
     * The refactored method from the VendorFacade.
     *
     * @param userID The ID of the user that is trying to pay
     * @param orderID The order that is being paid
     * @param paymentConfirmation Confirmation of transactional success
     * @throws MalformedException Thrown if something went wrong during the payment.
     * @throws ForbiddenException Thrown if the payment token was invalid.
     */
    public void payForOrder(long userID, long orderID, String paymentConfirmation)
            throws MalformedException, ForbiddenException {
        // create the validation chain

        var handler = tokenValidator;
        handler.setNext(statusValidator);
        statusValidator.setNext(userOwnershipValidator);

        // make sure the distance validator is last since it is most costly

        userOwnershipValidator.setNext(distanceValidator);


        try {
            handler.verify(new Payment(userID, orderID, paymentConfirmation));
        } catch (PaymentException e) {
            throw new ForbiddenException();
        } catch (VerificationException e) {
            throw new MalformedException();
        }

        // now try debiting the amount from the card

        Order order = orderDatabase.getById(orderID);
        if (!paymentService.finalizePayment(paymentConfirmation)) {
            // just give up
            throw new MalformedException();
        }

        // otherwise first set the order to pend

        order.setStatus(Order.StatusEnum.PENDING);
        orderDatabase.save(order);

        // and inform the vendor of the new order

        try {
            deliveryMicroservice.newDelivery(order.getVendorID(), order.getOrderID(), order.getCustomerID());
        } catch (ApiException e) {
            throw new MalformedException();
        }
    }
}
