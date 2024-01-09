package nl.tudelft.sem.orders.adapters;

import java.util.List;
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
    public Order save(Order toSave) {
        return orderRepository.saveAndFlush(toSave);
    }

    @Override
    public List<Order> findByVendorIDAndCustomerID(long vendorID, long customerID) {
        return orderRepository.findByVendorIDAndCustomerID(vendorID, customerID);
    }

    @Override
    public List<Order> findByVendorID(long vendorID) {
        return orderRepository.findByVendorID(vendorID);
    }

    @Override
    public List<Order> findByCustomerID(long customerID) {
        return orderRepository.findByCustomerID(customerID);
    }

    @Override
    public List<Order> findByCourierID(long courierID) {
        return orderRepository.findByCourierID(courierID);
    }

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }


}
