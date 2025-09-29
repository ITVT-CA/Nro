package managers;

import data.BlackGokuManager;
import data.BlackGokuResultSet;
import database.PlayerDAO;
import item.Item;
import player.Player;
import player.Service.InventoryService;
import services.ItemService;
import services.Service;
import utils.Logger;

public class RewardManager {

    private static final int[] NAP_MOC = {20000, 50000, 100000, 200000, 500000, 1000000};
    private static final int[] GOLD_BAR_REWARD = {20, 50, 100, 200, 500, 1000}; // Thỏi vàng thưởng theo tỷ lệ 1000 VND = 1 thỏi vàng

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
                // Tặng thỏi vàng
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    Item goldBar = ItemService.gI().createNewItem((short) 457);
                    goldBar.quantity = GOLD_BAR_REWARD[i];
                    InventoryService.gI().addItemBag(player, goldBar);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendThongBao(player,
                            "Bạn vừa nhận được " + GOLD_BAR_REWARD[i] + " thỏi vàng cho mốc nạp " + NAP_MOC[i] + "!");
                } else {
                    Service.gI().sendThongBao(player, "Hành trang đầy! Vui lòng dọn dẹp hành trang và liên hệ admin để nhận " + GOLD_BAR_REWARD[i] + " thỏi vàng.");
                }
                
                // Mốc 550k: Tặng thêm cải trang với options đặc biệt
                if (NAP_MOC[i] == 500000) {
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        Item caiTrang = ItemService.gI().createNewItem((short) 452);
                        caiTrang.itemOptions.add(new Item.ItemOption(101, 50)); // TNSM 50%
                        caiTrang.itemOptions.add(new Item.ItemOption(19, 30));  // Sức đánh 30%
                        InventoryService.gI().addItemBag(player, caiTrang);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được cải trang đặc biệt với 50% TNSM và 30% sức đánh!");
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đầy! Cải trang sẽ được gửi qua mail.");
                    }
                }

                 if (NAP_MOC[i] == 1000000) {
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        Item giapItem = ItemService.gI().createNewItem((short) 1716);   
                        giapItem.itemOptions.add(new Item.ItemOption(9, 10)); // TNSM 50%        
                        InventoryService.gI().addItemBag(player, giapItem);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được giáp luyện tập cấp 4");
                    } else {
                        Service.gI().sendThongBao(player, " Hành trang đầy! Cải trang sẽ được gửi qua mail.");
                    }
                }
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
