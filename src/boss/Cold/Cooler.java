package boss.Cold;


import boss.Boss;
import boss.BossID;
import boss.BossesData;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

import java.util.Random;
import services.ItemService;
import services.TaskService;

public class Cooler extends Boss {

    private long st;

    public Cooler() throws Exception {
        super(BossID.COOLER, BossesData.COOLER, BossesData.COOLER_2);
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x; // đâyyyy
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        int drop = 190; // 100% rơi item ID 190
        int quantity = Util.nextInt(20000, 30000);
        // Tạo itemMap cho item ID 190
        if (Util.isTrue(5 , 100)) {
        ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x, y, plKill.id);
        if (it != null) {
        Service.gI().dropItemMap(zone, it);
        }
        }
        ItemMap itemMap = new ItemMap(this.zone, drop, quantity, x, y, plKill.id);
        Item item = ItemService.gI().createNewItem((short) drop);
        Service.gI().dropItemMap(zone, itemMap);
        // 30% xác suất để rơi đồ
        if (Util.isTrue(5, 100)) {
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1;  // 70% chọn Áo Quần Giày (group = 0), 30% chọn Găng Rada (group = 1)

            // Các vật phẩm rơi từ nhóm Áo Quần Giày và Găng Rada
            int[][] drops = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276}, // Áo Quần Giày
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280} // Găng Rada
            };
            // Chọn vật phẩm ngẫu nhiên từ nhóm đã chọn
            int dropOptional = drops[group][Util.nextInt(0, drops[group].length - 1)];
            // Tạo vật phẩm và thêm chỉ số shop
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, 1, x, y, plKill.id);
            Item optionalItem = ItemService.gI().createNewItem((short) dropOptional);
            List<Item.ItemOption> optionalOps = ItemService.gI().getListOptionItemShop((short) dropOptional);
            optionalOps.forEach(option -> option.param = (int) (option.param * Util.nextInt(100, 115) / 100.0));
            optionalItemMap.options.addAll(optionalOps);
            // Thêm chỉ số sao pha lê (80% từ 1-3 sao, 17% từ 4-5 sao, 3% sao 6)
            int rand = Util.nextInt(1, 100);
            int value = 0;
            if (rand <= 80) {
                value = Util.nextInt(1, 3); // 80% xác suất: sao từ 1 đến 3
            } else if (rand <= 97) {
                value = Util.nextInt(4, 5); // 17% xác suất: sao từ 4 đến 5
            } else {
                value = 6; // 3% xác suất: sao 6
            }
            optionalItemMap.options.add(new Item.ItemOption(107, value));
            // Drop vật phẩm tùy chọn xuống bản đồ
            Service.gI().dropItemMap(zone, optionalItemMap);
        }
        // 80% xác suất rơi ngọc rồng
        if (Util.isTrue(80, 100)) {
            int[] dropItems = {15,16,17,18,19,20};
            int dropOptional = dropItems[Util.nextInt(0, dropItems.length - 1)];
            // Tạo và rơi vật phẩm ngọc rồng hoặc item cấp 2
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, Util.nextInt(1, 3), x, y, plKill.id);
            Item optionalItem = ItemService.gI().createNewItem((short) dropOptional);
            Service.gI().dropItemMap(zone, optionalItemMap);
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (Util.isTrue(10, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
//        System.out.println("Cooler join map" + this.zone.map.mapId + " Zone: " + this.zone.zoneId);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

}
