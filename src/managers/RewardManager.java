package managers;

import data.BlackGokuManager;
import data.BlackGokuResultSet;
import database.PlayerDAO;
import player.Player;
import services.Service;
import utils.Logger;

public class RewardManager {

    private static final int[] NAP_MOC = {20000, 50000, 100000, 200000};
    private static final int[] RUBY_THUONG = {2000, 6000, 15000, 40000};

    // Parse từ "0b..." sang int
    private static int parseFlags(String s) {
        if (s == null || s.isEmpty() || s.equals("null")) return 0;
        s = s.trim();
        if (s.isEmpty() || s.equals("null")) return 0;
        
        try {
            if (s.startsWith("0b") || s.startsWith("0B")) {
                String binaryStr = s.substring(2);
                if (binaryStr.isEmpty()) return 0;
                return Integer.parseInt(binaryStr, 2);
            }
            // Kiểm tra xem string có chỉ chứa 0 và 1 không
            if (s.matches("[01]+")) {
                return Integer.parseInt(s, 2);
            }
            // Nếu không phải binary format, trả về 0
            return 0;
        } catch (NumberFormatException e) {
            Logger.logException(RewardManager.class, e, "Lỗi parse flags: " + s);
            return 0;
        }
    }

    // Convert từ int sang "0b..."
    private static String toBinaryString(int flags) {
        return "0b" + Integer.toBinaryString(flags);
    }

    // Kiểm tra mốc đã nhận chưa
    private static boolean hasClaimed(int rewardFlags, int index) {
        return (rewardFlags & (1 << index)) != 0;
    }

    // Set flag khi đã nhận
    private static int setClaimed(int rewardFlags, int index) {
        return rewardFlags | (1 << index);
    }

    // Hàm chính: kiểm tra và phát quà
    public static int claimRewards(Player player) {
        // Đọc reward từ player.reward (DB field, VARCHAR "0b...")
        try{ 
            BlackGokuResultSet rs = BlackGokuManager.executeQuery("select reward from account where id = ?", player.session.userId);
            if(rs.first()){
                String rewardStr = rs.getString("reward");
                player.reward = (rewardStr == null) ? "0b0" : rewardStr;
            } else {
                player.reward = "0b0";
            }
        } catch (Exception e) {
            Logger.logException(RewardManager.class, e, "Lỗi load reward from db");
            player.reward = "0b0";
        }
        Logger.success("abc "+player.reward);
        int flags = parseFlags(player.reward);
        int claimedThisTime = 0;

        for (int i = 0; i < NAP_MOC.length; i++) {
            if (player.getSession().tongnap >= NAP_MOC[i] && !hasClaimed(flags, i)) {
                // Cộng ruby
                player.inventory.ruby += RUBY_THUONG[i];
                Service.gI().sendMoney(player);
                Service.gI().sendThongBao(player,
                        "Bạn vừa nhận được " + RUBY_THUONG[i] + " ruby cho mốc nạp " + NAP_MOC[i] + "!");
                // Ghi cờ
                flags = setClaimed(flags, i);
                claimedThisTime++;
            }
        }

        // Cập nhật lại reward dưới dạng String
        String newStr = toBinaryString(flags);
        player.reward = newStr; // cập nhật trong Player object


        if(PlayerDAO.updateRewardFlags((int) player.session.userId, newStr)){
            Logger.success(player.name + " update reward successfully!");
        } else {
            Logger.error(player.name + " update reward failed!");
        }
        
        
        return claimedThisTime; // số mốc nhận được trong lần gọi này
    }
}
