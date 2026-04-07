package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private double totalBalance;
    private int totalTransactions;
    private double totalSpent;
    private double totalReceived;
}
