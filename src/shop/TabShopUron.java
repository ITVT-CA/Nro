package shop;

import player.Player;
import java.util.ArrayList;
import java.util.List;
import player.Service.InventoryService;

public class TabShopUron extends TabShop {

    private final int[] listDauThan = {293, 294, 295, 296, 297, 298, 299, 596, 597, 598};

    public TabShopUron(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        int dauCanBuyId = idDauCanBuy(player);

        // Kiểm tra nếu người chơi có bông tai
        boolean hasBongTai = InventoryService.gI().findItemBongTai(player);

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender == 3) {
                boolean isInListDauThan = false;
                for (int id : listDauThan) {
                    if (itemShop.temp.id == id) {
                        isInListDauThan = true;
                        break;
                    }
                }
                // Bỏ qua item 453 nếu player có Tennis Space Ship
                if (player.haveTennisSpaceShip && itemShop.temp.id == 453) {
                    continue;
                }

                // Bỏ qua item 454 nếu người chơi đã có bông tai
                if (hasBongTai && itemShop.temp.id == 454) {
                    continue;
                }

                // Thêm item vào shop nếu không bị bỏ qua
                if (!isInListDauThan || itemShop.temp.id == dauCanBuyId) {
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }

    public int idDauCanBuy(Player player) {
        int level = player.magicTree.level;
        if (level == 10) {
            return listDauThan[9];
        } else if (level >= 1 && level <= 9) {
            return listDauThan[level];
        }
        throw new IllegalArgumentException("Invalid magic tree level: " + level);
    }
}
