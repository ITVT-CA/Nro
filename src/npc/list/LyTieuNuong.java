package npc.list;

/*
 * @Author: NgọcRồngBlackGoku
 * @Description: Ngọc Rồng BlackGoku - Máy Chủ Chuẩn Teamobi 2024
 * @Group Zalo: https://zalo.me/g/qabzvn331
 */
import consts.ConstNpc;
import database.PlayerDAO;
import item.Item;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.Input;
import player.Service.InventoryService;
import shop.ShopService;
import managers.RewardManager;

public class LyTieuNuong extends Npc {

    public LyTieuNuong(int mapid, int status, int cx, int cy, int tempid, int avartar) {
        super(mapid, status, cx, cy, tempid, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|0| Game Ngọc Rồng Chuẩn Teamobi 2025",
                        "Mở Thành Viên", "Đổi Thỏi Vàng", "Nhận Mốc Nạp");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        if (!player.getSession().actived) {
                            if (player.getSession().vnd >= 10000) {
                                player.getSession().actived = true;
                                if (PlayerDAO.MuaThanhVien(player, 0)) {
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendMoney(player);
                                } else {
                                    this.npcChat(player, "Không đủ tiền để mở thành viên...!");
                                }
                            }
                        } else {
                            this.npcChat(player, "Đã mở thành viên!");
                        }
                    }
                    case 1 -> Input.gI().createFormTradeGold(player);

                    case 2 -> {
                        // int claimed = RewardManager.claimRewards(player);
                        // if (claimed == 0) {
                        // Service.gI().sendThongBao(player, "Bạn chưa đạt mốc nạp mới hoặc đã nhận hết
                        // quà.");
                        // } else {
                        // Service.gI().sendThongBao(player, "Nhận thành công " + claimed + " mốc quà
                        // nạp!");
                        // }
                        createOtherMenu(player, 3,
                                "Sự kiện nạp nhận quà:\r\n" + //
                                                                        "\r\n" + //
                                                                        "- Khi bạn nạp tích lũy đạt **20.000** VND → nhận ngay **2.000 Ruby**.\r\n" + //
                                                                        "- Khi bạn nạp tích lũy đạt **50.000** VND → nhận ngay **6.000 Ruby**.\r\n" + //
                                                                        "- Khi bạn nạp tích lũy đạt **100.000** VND → nhận ngay **15.000 Ruby**.\r\n" + //
                                                                        "- Khi bạn nạp tích lũy đạt **200.000** VND → nhận ngay **40.000 Ruby**.\r\n" + //
                                                                        "\r\n" + //
                                                                        "Mỗi mốc quà chỉ có thể nhận **1 lần duy nhất**.  \r\n" + //
                                                                        "Khi bạn đã đạt mốc nào, hệ thống sẽ tự động phát quà và ghi nhận lại, tránh nhận trùng lặp.\r\n" + //
                                                                        "",
                                "Nhận quà");

                    }
                    case 3 -> {
                        player.getSession().tongnap += 10000;
                        Service.gI().sendThongBao(player,
                                "Bạn vừa nhận được 10k coin");

                    }
                }
            }
            else if (player.idMark.getIndexMenu() == 3) {
            switch (select) {
                case 0 -> { // Nhận quà
                    int claimed = RewardManager.claimRewards(player);
                    if (claimed == 0) {
                        Service.gI().sendThongBao(player,
                            "Bạn chưa đạt mốc nạp mới hoặc đã nhận hết quà.");
                    } else {
                        Service.gI().sendThongBao(player,
                            "Nhận thành công " + claimed + " mốc quà nạp!");
                    }
                }
                // case 1 -> { // Từ chối
                //     this.npcChat(player, "Hẹn gặp lại!");
                //}
            }
        }
    }
}
}

