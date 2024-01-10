package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FuzzyFindStrategy implements SearchStrategy {
    @Override
    public List<Long> filterOnSearchString(List<Long> vendors, String search) {

        // search through vendor names here

        return vendors;
    }
}
