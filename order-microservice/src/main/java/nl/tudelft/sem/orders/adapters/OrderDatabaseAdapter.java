package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.orders.domain.OrderRepository;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderDatabaseAdapter implements OrderDatabase {
    @Autowired
    private transient OrderRepository orderRepository;

    @Override
    public Order getById(long orderId) {
        return orderRepository.findByOrderID(orderId);
    }

    @Override
    public void save(Order toSave) {
        orderRepository.saveAndFlush(toSave);
    }

    public Long getLastId(){
        try {
            return orderRepository.findTopByOrderByOrderIDDesc().getOrderID();
        } catch (NullPointerException e) {
            return 0L;
        }
    }
}
