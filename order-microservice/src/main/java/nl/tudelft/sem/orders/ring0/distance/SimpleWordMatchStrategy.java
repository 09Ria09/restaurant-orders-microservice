package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.stereotype.Component;

@Component
public class SimpleWordMatchStrategy implements SearchStrategy {
    @Override
    public List<Vendor> filterOnSearchString(List<Vendor> vendors, String search) {

        if (search != null) {
            vendors = vendors.stream()
                .filter(vendor -> vendor.getName().toLowerCase(Locale.getDefault())
                    .contains(search.toLowerCase(Locale.getDefault())))
                .collect(Collectors.toList());
        }

        return vendors;
    }
}
