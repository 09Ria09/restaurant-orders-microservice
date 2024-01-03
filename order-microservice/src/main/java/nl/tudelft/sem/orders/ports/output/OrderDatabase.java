package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.model.Order;

public interface OrderDatabase {
    Order getById(long orderId);

    Order save(Order toSave);
}
