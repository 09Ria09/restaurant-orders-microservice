package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;

public interface SearchStrategy {
    List<Long> filterOnSearchString(List<Long> vendors, String search);
}
