package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.stereotype.Component;

@Component
public class FuzzyFindStrategy implements SearchStrategy {
    @Override
    public List<Vendor> filterOnSearchString(List<Vendor> vendors, String search) {

        if (search != null) {
            vendors = vendors.stream()
                .filter(vendor -> {
                    int distance = calculateLevenshteinDistance(
                        vendor.getName().toLowerCase(),
                        search.toLowerCase()
                    );
                    return distance <= 2;
                })
                .collect(Collectors.toList());
        }

        return vendors;
    }

    private int calculateLevenshteinDistance(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[m][n];
    }
}
