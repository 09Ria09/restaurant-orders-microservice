package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.stereotype.Component;

@Component
public class FuzzyFindStrategy implements SearchStrategy {
    @Override
    public List<Vendor> filterOnSearchString(List<Vendor> vendors, String search) {

        // search through vendor names here

        return vendors;
    }
}
