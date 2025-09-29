package boss.MajinBuu14H;


import boss.Boss;
import boss.BossManager.FinalBossManager;
import boss.BossID;
import consts.BossStatus;
import boss.BossesData;
import static consts.BossType.FINAL;
import java.util.ArrayList;
import java.util.List;

import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import item.Item;
import map.ItemMap;
import services.ItemService;

import server.ServerNotify;
import services.ItemTimeService;
import services.SkillService;
import services.TaskService;
import map.Service.ChangeMapService;
import skill.Skill;
import utils.SkillUtil;

public class Mabu2H extends Boss {

    private long lastTimeEat;

    private long lastTimeUseSkill;
    private long timeUseSkill;
    public List<Player> maBuEat = new ArrayList<>();

    public Mabu2H() throws Exception {
        super(FINAL, BossID.MABU, BossesData.MABU, BossesData.SUPER_BU, BossesData.BU_TENK, BossesData.BU_HAN, BossesData.KID_BU);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            this.zone = zoneFinal;
        }
        ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
        this.changeStatus(BossStatus.ACTIVE);
    }

    private void eatPlayersInTheMap() {
        int numPlayers = 0;
        for (Player pl : this.zone.getPlayers()) {
            if (Util.isTrue(1, 5)) {
                pl.isMabuHold = true;
                Service.gI().sendMabuEat(this, pl);
                this.maBuEat.add(pl);
                numPlayers++;
            }
        }
        if (numPlayers > 0) {
            this.chat("Măm măm");
        }
    }

    private void petrifyPlayersInTheMap() {
        for (Player pl : this.zone.getNotBosses()) {
            if (Util.isTrue(1, 5)) {
                this.chat("Úm ba la xì bùa");
                EffectSkillService.gI().setSocola(pl, System.currentTimeMillis(), 30000);
                Service.gI().Send_Caitrang(pl);
                ItemTimeService.gI().sendItemTime(pl, 4133, 30);
            }
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                if (Util.canDoWithTime(lastTimeEat, 10000)) {
                    eatPlayersInTheMap();
                    if (this.currentLevel == 0) {
                        petrifyPlayersInTheMap();
                    }
                    this.lastTimeEat = System.currentTimeMillis();
                }
                if (this.currentLevel > 0) {
                    if (Util.canDoWithTime(lastTimeUseSkill, timeUseSkill)) {
                        Service.gI().sendMabuAttackSkill(this);
                        lastTimeUseSkill = System.currentTimeMillis();
                        timeUseSkill = Util.nextInt(5000, 10000);
                        return;
                    }
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)), pl.location.y);
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)), pl.location.y);
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        int drop = 190; // 100% rơi item ID 190
        int quantity = Util.nextInt(20000, 30000);
        // Tạo itemMap cho item ID 190
        if (Util.isTrue(5, 100)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x, y, plKill.id);
            if (it != null) {
                Service.gI().dropItemMap(zone, it);
            }
        }
        ItemMap itemMap = new ItemMap(this.zone, drop, quantity, x, y, plKill.id);
        Service.gI().dropItemMap(zone, itemMap);
        // 5% xác suất để rơi đồ
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
        // 10% xác suất rơi ngọc rồng
        if (Util.isTrue(10, 100)) {
            int[] dropItems = {15,16,17,18,19,20,992};
            int dropOptional = dropItems[Util.nextInt(0, dropItems.length - 1)];
            // Tạo và rơi vật phẩm ngọc rồng hoặc item cấp 2
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, Util.nextInt(1, 3), x, y, plKill.id);
            Service.gI().dropItemMap(zone, optionalItemMap);
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(10, 100)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (plAtt.isPl() && Util.isTrue(1, 5)) {
                plAtt.fightMabu.changePercentPoint((byte) 1);
            }
            if (this.currentLevel == this.data.length - 1) {
                if (plAtt.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI) {
                    damage = damage >= this.nPoint.hp ? 0 : damage;
                }
            }

            if (damage >= 30000000) {
                damage = 30000000 + Util.nextInt(-10000, 10000);
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                Boss boss = FinalBossManager.gI().getBossById(BossID.SUPERBU, 128, this.zone.zoneId);
                if (boss != null) {
                    boss.changeStatus(BossStatus.DIE);
                }
                die(plAtt);
            }

            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            List<Player> pls = new ArrayList<>();
            List<Player> players = this.maBuEat;
            for (Player pl : players) {
                pls.add(pl);
            }
            for (Player pl : pls) {
                if (pl.zone != null && pl.zone.map.mapId == 128) {
                    ChangeMapService.gI().changeMap(pl, 127, this.zone.zoneId, -1, 312);
                }
            }
            players.clear();
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

}
