package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import nl.tudelft.sem.users.model.Vendor;

public interface SearchStrategy {
    List<Vendor> filterOnSearchString(List<Vendor> vendors, String search);
}
