package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private int totalApps;
    private int activeSubscriptions;
    private double walletBalance;
    private int totalTransactions;
    private int referralCount;
    private String kycStatus;

    private double totalSpent;
    private double totalReceived;
}
