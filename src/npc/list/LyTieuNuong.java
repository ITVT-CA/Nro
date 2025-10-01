package npc.list;

import com.mysql.jdbc.log.Log;

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
import utils.Logger;
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
                        "Mở Thành Viên", "Đổi Thỏi Vàng", "Nhận Mốc Nạp", "Đổi Mảnh Thiệp");
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
                                if (PlayerDAO.MuaThanhVien(player, 0)) {
                                    player.getSession().actived = true;
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendMoney(player);
                                    Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã mở thành viên thành công!");
                                    Service.gI().sendThongBao(player, "Bạn đã nhận được các đặc quyền thành viên!");
                                } else {
                                    Service.gI().sendThongBao(player, " Có lỗi xảy ra khi mở thành viên!");
                                }
                            } else {
                                Service.gI().sendThongBao(player,
                                        "Không đủ tiền! Cần ít nhất 10.000 VND để mở thành viên.");
                                Service.gI().sendThongBao(player,
                                        "Số dư hiện tại: " + player.getSession().vnd + " VND");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Bạn đã là thành viên rồi!");
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
                                        "- Khi bạn nạp  **20.000** VND → nhận ngay **20 TV**.\r\n" + //
                                        "- Khi bạn nạp  **50.000** VND → nhận ngay **30 TV**.\r\n" + //
                                        "- Khi bạn nạp  **100.000** VND → nhận ngay **50 TV**.\r\n" + //
                                        "- Khi bạn nạp  **200.000** VND → nhận ngay **110 TV**.\r\n" + //
                                        "- Khi bạn nạp  **550.000** VND → nhận ngay **350 TV + Cải trang đặc biệt (50% TNSM + 30% Sức đánh)**\r\n"
                                        + //
                                        "- Khi bạn nạp  **1.000.000** VND → nhận ngay **500 TV + 1 giáp luyện tập cấp 4.**\r\n"
                                        + //
                                        "\r\n" + //
                                        "Mỗi mốc quà chỉ có thể nhận **1 lần duy nhất**.  \r\n",
                                "Nhận quà");

                    }
                    case 3 -> {
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            Item thiepItem = InventoryService.gI().findItemBag(player, 399);

                            if (thiepItem != null && thiepItem.quantity >= 2000) {

                                InventoryService.gI().subQuantityItemsBag(player, thiepItem, 2000);

                                Item goldBar = ItemService.gI().createNewItem((short) 457);
                                goldBar.quantity = 2;
                                InventoryService.gI().addItemBag(player, goldBar);

                                InventoryService.gI().sendItemBags(player);

                                Service.gI().sendThongBao(player, "Đã sử dụng 2000 mảnh thiệp để đổi lấy 2 thỏi vàng!");
                            } else {
                                Service.gI().sendThongBao(player,
                                        "Bạn cần ít nhất 2000 mảnh thiệp để đổi 2 thỏi vàng!");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đầy! Vui lòng dọn dẹp hành trang.");
                        }
                    }
                }
            } else if (player.idMark.getIndexMenu() == 3) {
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
                    // this.npcChat(player, "Hẹn gặp lại!");
                    // }
                }
            }
        }
    }
}
