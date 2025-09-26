package services.func;

/*
 * @Author Coder: Nguyễn Tấn Tài
 * @Description: Ngọc Rồng Kiwi - Máy Chủ Chuẩn Teamobi 2025
 * @Group Zalo: https://zalo.me/g/toiyeuvietnam2025
 */
import boss.Boss;
import boss.Bossmini.SoiHecQuyn;
import services.shenron.SummonDragon;
import combine.CombineService;
import boss.BossID;
import consts.ConstItem;
import radar.Card;
import services.RadarService;
import radar.RadarCard;
import consts.ConstMap;
import item.Item;
import consts.ConstNpc;
import consts.ConstPlayer;
import services.shenron.Shenron_Service;
import item.Item.ItemOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import map.ItemMap;
import map.Zone;
import player.Inventory;
import map.Service.NpcService;
import player.Player;
import skill.Skill;
import network.Message;
import map.Service.ChangeMapService;
import utils.SkillUtil;
import services.Service;
import utils.Util;
import network.MySession;
import npc.MabuEgg;
import services.ItemService;
import services.ItemTimeService;
import services.PetService;
import services.RewardService;
import player.Service.PlayerService;
import services.TaskService;
import player.Service.InventoryService;
import map.Service.MapService;
import services.dungeon.NgocRongNamecService;
import map.Service.ItemMapService;
import utils.Logger;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null) {
            return;
        }
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            if (player.setClothes != null) {
                player.setClothes.setup();
            }
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().sendFlagBag(player);
            Service.gI().point(player);
            Service.gI().sendSpeedPlayer(player, -1);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public Item finditem(Player player, int iditem) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học " + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {
                                    if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                        Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n" + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    if (player.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22).count() > 2) {
                                        Service.gI().sendThongBaoOK(player, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n" + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            UseItem.gI().useItem(player, item, index);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }

                        if (item.isNotNullItem() && item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }
                        if (!item.isNotNullItem()) {
                            return;
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryService.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryService.gI().sendItemBags(player);
                    break;
                case ACCEPT_USE_ITEM:
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item != null && item.isNotNullItem()) {

            if (item.template.id == 570) {
                if (!Util.isAfterMidnight(pl.lastTimeRewardWoodChest)) {
                    Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
                } else {
                    openRuongGo(pl);
                }
                return;
            }
            if (item.template.strRequire <= pl.nPoint.power) {
                switch (item.template.type) {
                    case 33: //card
                        UseCard(pl, item);
                        break;
                    case 7: //sách học, nâng skill
                        learnSkill(pl, item);
                        break;
                    case 6: //đậu thần
                        this.eatPea(pl);
                        break;
                    case 12: //ngọc rồng các loại
                        controllerCallRongThan(pl, item);
                        break;
                    case 23: //thú cưỡi mới
                    case 24: //thú cưỡi cũ
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    case 11: //item bag
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendFlagBag(pl);
                        break;
                    case 25: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                    }
                    default:
                        switch (item.template.id) {
                            case 992: // Nhan thoi khong
                                pl.type = 2;
                                pl.maxTime = 5;
                                Service.gI().Transport(pl);
                                break;
                            case 361:
                                pl.idGo = (short) Util.nextInt(0, 6);
                                NgocRongNamecService.gI().menuCheckTeleNamekBall(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                break;
                            case 892:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 882, 883, 884);
                                Service.gI().point(pl);
                                break;
                            case 893:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 885, 886, 887);
                                Service.gI().point(pl);
                                break;
                            case 908:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 891, 892, 893);
                                Service.gI().point(pl);
                                break;
                            case 909:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 894, 895, 896);
                                Service.gI().point(pl);
                                break;
                            case 910:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 897, 898, 899);
                                Service.gI().point(pl);
                                break;
                            case 916:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 925, 926, 927);
                                Service.gI().point(pl);
                                break;
                            case 917:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 928, 929, 930);
                                Service.gI().point(pl);
                                break;
                            case 918:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 931, 932, 933);
                                Service.gI().point(pl);
                                break;
                            case 919:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 934, 935, 936);
                                Service.gI().point(pl);
                                break;
                            case 936:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 718, 719, 720);
                                Service.gI().point(pl);
                                break;
                            case 942:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 966, 967, 968);
                                Service.gI().point(pl);
                                break;
                            case 943:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 969, 970, 971);
                                Service.gI().point(pl);
                                break;
                            case 944:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 972, 973, 974);
                                Service.gI().point(pl);
                                break;
                            case 967:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1050, 1051, 1052);
                                Service.gI().point(pl);
                                break;
                            case 1008:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1074, 1075, 1076);
                                Service.gI().point(pl);
                                break;
                            case 1039:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1089, 1090, 1091);
                                Service.gI().point(pl);
                                break;
                            case 1040:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1092, 1093, 1094);
                                Service.gI().point(pl);
                                break;
                            case 1046:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, -1, -1, -1);
                                Service.gI().point(pl);
                                break;
                            case 1107:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1155, 1156, 1157);
                                Service.gI().point(pl);
                                break;
                            case 1114:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1158, 1159, 1160);
                                Service.gI().point(pl);
                                break;
                            case 1188:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1183, 1184, 1185);
                                Service.gI().point(pl);
                                break;
                            case 1202:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1201, 1202, 1203);
                                Service.gI().point(pl);
                                break;
                            case 1203:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1201, 1202, 1203);
                                Service.gI().point(pl);
                                break;
                            case 1207:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1077, 1078, 1079);
                                Service.gI().point(pl);
                                break;
                            case 1224:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1227, 1228, 1229);
                                Service.gI().point(pl);
                                break;
                            case 1225:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1233, 1234, 1235);
                                Service.gI().point(pl);
                                break;
                            case 1226:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1230, 1231, 1232);
                                Service.gI().point(pl);
                                break;
                            case 1243:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1245, 1246, 1247);
                                Service.gI().point(pl);
                                break;
                            case 1244:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1248, 1249, 1250);
                                Service.gI().point(pl);
                                break;
                            case 1256:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1267, 1268, 1269);
                                Service.gI().point(pl);
                                break;
                            case 1318:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1299, 1300, 1301);
                                Service.gI().point(pl);
                                break;
                            case 1347:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1302, 1303, 1304);
                                Service.gI().point(pl);
                                break;
                            case 1414:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1341, 1342, 1343);
                                Service.gI().point(pl);
                                break;
                            case 1435:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1347, 1348, 1349);
                                Service.gI().point(pl);
                                break;
                            case 1452:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1365, 1366, 1367);
                                Service.gI().point(pl);
                                break;
                            case 1458:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1368, 1369, 1370);
                                Service.gI().point(pl);
                                break;
                            case 1482:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1398, 1399, 1400);
                                Service.gI().point(pl);
                                break;
                            case 1497:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1401, 1402, 1403);
                                Service.gI().point(pl);
                                break;
                            case 1550:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1428, 1429, 1430);
                                Service.gI().point(pl);
                                break;
                            case 1551:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1425, 1426, 1427);
                                Service.gI().point(pl);
                                break;
                            case 1564:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1437, 1438, 1439);
                                Service.gI().point(pl);
                                break;
                            case 1568:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1443, 1444, 1445);
                                Service.gI().point(pl);
                                break;
                            case 1573:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1446, 1447, 1448);
                                Service.gI().point(pl);
                                break;
                            case 1596:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1473, 1474, 1475);
                                Service.gI().point(pl);
                                break;
                            case 1597:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1473, 1474, 1475);
                                Service.gI().point(pl);
                                break;
                            case 1611:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1488, 1494, 1495);
                                Service.gI().point(pl);
                                break;
                            case 1620:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1496, 1497, 1498);
                                Service.gI().point(pl);
                                break;
                            case 1621:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1496, 1497, 1498);
                                Service.gI().point(pl);
                                break;
                            case 1622:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1488, 1489, 1490);
                                Service.gI().point(pl);
                                break;
                            case 1629:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1505, 1506, 1507);
                                Service.gI().point(pl);
                                break;
                            case 1630:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1508, 1509, 1510);
                                Service.gI().point(pl);
                                break;
                            case 1631:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1513, 1516, 1517);
                                Service.gI().point(pl);
                                break;
                            case 1633:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1523, 1524, 1525);
                                Service.gI().point(pl);
                                break;
                            case 1654:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1526, 1529, 1530);
                                Service.gI().point(pl);
                                break;
                            case 1668:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1550, 1551, 1552);
                                Service.gI().point(pl);
                                break;
                            case 1682:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1558, 1559, 1560);
                                Service.gI().point(pl);
                                break;
                            case 1683:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1561, 1562, 1563);
                                Service.gI().point(pl);
                                break;
                            case 1686:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1572, 1573, 1574);
                                Service.gI().point(pl);
                                break;
                            case 1750:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                PetService.Pet2(pl, 1464, 1465, 1466);
                                Service.gI().point(pl);
                                break;
                            case 1559: // capule kich hoat
                                UseItem.gI().CapsuleKichHoat(pl);
                                UseItem.gI().NguyenTanTai_PhaoHoa(pl, item);
                                break;
                            case 1505:
                                Item gm = null;
                                if (item.isNotNullItem()) {
                                    if (item.isGiayMau()) {
                                        gm = item;
                                    }
                                }
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.event3, -1,
                                        "|1|Gói Hộp đựng quà\n"
                                        + "|2|Giấy màu " + gm.quantity + "/99\n"
                                        + "|2|Giá vàng 2.000.000",
                                        "Đồng ý", "Đóng");

                                break;
                            case 1506:
                            case 1507:
                            case 1508:
                            case 1509:
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.event3_1, -1,
                                        "Bạn muốn gói loại nào",
                                        "Gói Hộp quà nhẹ nhàng", "Gói Hộp quà chỉnh chu", "Từ chối");
                                break;
                            case 211: //nho tím
                            case 212: //nho xanh
                                eatGrapes(pl, item);
                                break;
                            case 460:
                                NguyenTanTai_XuongCho(pl, item);
                                break;
                            case 962:
                                NguyenTanTai_C5(pl, item);
                                break;
                            case 963:
                                NguyenTanTai_C7(pl, item);
                                break;
                            case 342:
                            case 343:
                            case 344:
                            case 345:
                                if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22).count() < 3) {
                                    Service.gI().dropSatellite(pl, item, pl.zone, pl.location.x, pl.location.y);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                } else {
                                    Service.gI().sendThongBaoOK(pl, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                }
                                break;
                            case 380: //cskb
                                openCSKB(pl, item);
                                break;
                            case 381: //cuồng nộ
                            case 382: //bổ huyết
                            case 383: //bổ khí
                            case 384: //giáp xên
                            case 385: //ẩn danh
                            case 379: //máy dò capsule
                            case 638: //commeson                           
                            case 579:
                            case 1045: //đuôi khỉ
                            case 663: //bánh pudding
                            case 664: //xúc xíc
                            case 665: //kem dâu
                            case 666: //mì ly
                            case 667: //sushi
                            case 764: //Khau Trang
                            case 1150:
                            case 1151:
                            case 1152:
                            case 1153:
                            case 1154:
                            case 1233:
                            case 1532:
                            case 1628:

                                useItemTime(pl, item);
                                break;
                            case 880:
                            case 881:
                            case 882:
                                if (pl.itemTime.isEatMeal2) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 521: //tdlt
                                useTDLT(pl, item);
                                break;
                            case 454: //bông tai
                                UseItem.gI().usePorata(pl);
                                break;
                            case 921: //bông tai
                                UseItem.gI().usePorata2(pl);
                                break;
                            case 193: //gói 10 viên capsule
                                openCapsuleUI(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            case 194: //capsule đặc biệt
                                openCapsuleUI(pl);
                                break;
                            case 401: //đổi đệ tử
                                changePet(pl, item);
                                break;
                            case 402: //sách nâng chiêu 1 đệ tử
                            case 403: //sách nâng chiêu 2 đệ tử
                            case 404: //sách nâng chiêu 3 đệ tử
                            case 759: //sách nâng chiêu 4 đệ tử
                                upSkillPet(pl, item);
                                break;
                            case 726:
                                UseItem.gI().ItemManhGiay(pl, item);
                                break;
                            case 727:
                            case 728:
                                UseItem.gI().ItemSieuThanThuy(pl, item);
                                break;
                            case 1775:
                                UseItem.gI().OpenHopThanlinh(pl, item.template.id);
                                break;
                            case 648:
                                UseItem.gI().NoelItemBox(pl, item);
                                break;
                            case 1171:
                                UseItem.gI().ChuLunBox(pl, item);
                                break;
                            case 1560:
                                if (InventoryService.gI().findItem(pl.inventory.itemsBag, 1561) != null) {
                                    UseItem.gI().RuongNgocRong(pl, item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Bạn không có chía khoá vàng!");
                                }
                                break;

                            case 1170:
                                UseItem.gI().BlackGokuItemBoxEventNoel(pl, item);
                                break;
                            case 736:
                                ItemService.gI().OpenItem736(pl, item);
                                break;
                            case 987:
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); //đá bảo vệ
                                break;
                            case 568: //quả trứng
                            if (pl.mabuEgg == null) {
                                    MabuEgg.createMabuEgg(pl);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    if (pl.zone.map.mapId == (21 + pl.gender)) {
                                    if (pl.mabuEgg != null) {
                                        pl.mabuEgg.sendMabuEgg();
                                    }
                            }
                            } else {
                                Service.gI().sendThongBao(pl, "Bạn đã có quả trứng nên không thể sử dụng");
                            }
                             break;
                            case 2006:
                                Input.gI().createFormChangeNameByItem(pl);
                                break;
                            case 1774 : //đổi đệ tử
                                Black(pl, item);
                                break;
                            case 1758: {
                                Player player = pl;
                                if (player.pet != null) {
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                                        player.pet.openSkill2();
                                    } else {
                                        Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                        return;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                                    return;
                                }
                            }
                            break;
                            case 1759: {
                                Player player = pl;
                                if (player.pet != null) {
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                        player.pet.openSkill3();
                                    } else {
                                        Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                        return;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                                    return;
                                }
                            }
                            break;
                            case 1760: {
                                Player player = pl;
                                if (player.pet != null) {
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                        player.pet.openSkill4();
                                    } else {
                                        Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");
                                        return;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                                    return;
                                }
                            }
                            break;
                        }
                        break;
                }
                TaskService.gI().checkDoneTaskUseItem(pl, item);
                InventoryService.gI().sendItemBags(pl);
            } else {
                Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
            }
        }
    }

    public void openRuongGo(Player player) {
        // Tìm kiếm rương gỗ trong hành trang của người chơi
        Item ruongGo = InventoryService.gI().findItemBag(player, 570);
        if (ruongGo != null) {
            int level = InventoryService.gI().getParam(player, 72, 570);

            // Tính toán số ô trống cần thiết cho phần thưởng
            int requiredSlots = calculateRequiredEmptySlots(level);

            // Kiểm tra số lượng ô trống trong hành trang người chơi
            if (InventoryService.gI().getCountEmptyBag(player) < requiredSlots) {
                Service.gI().sendThongBao(player, "Cần ít nhất " + (requiredSlots - InventoryService.gI().getCountEmptyBag(player)) + " ô trống trong hành trang");
            } else {
                player.itemsWoodChest.clear();  // Xóa các item trong danh sách phần thưởng trước khi mở rương

                // Phần thưởng khi cấp độ = 0
                if (level == 0) {
                    InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
                    InventoryService.gI().sendItemBags(player);

                    // Tạo item vàng (ID 190) cho phần thưởng
                    Item item = ItemService.gI().createNewItem((short) 190);
                    item.quantity = 1;  // Số lượng vàng ở level 0 là 1
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBags(player);

                    Service.gI().sendThongBao(player, "reward");
                    return;  // Thoát ra nếu cấp độ = 0
                }

                // Tính toán số lượng vàng thưởng dựa trên cấp độ
                int baseGoldAmount = 100 * level;  // Tính số lượng vàng cơ bản
                int randomFactor = Util.nextInt(-15, 15);  // Tạo một yếu tố ngẫu nhiên để biến động số lượng vàng
                int goldAmount = baseGoldAmount + (baseGoldAmount * randomFactor / 100);

                Item itemGold = ItemService.gI().createNewItem((short) 190);
                itemGold.quantity = goldAmount * 1000;  // Số lượng vàng thưởng (đơn vị là vàng)
                player.itemsWoodChest.add(itemGold);  // Thêm vàng vào phần thưởng
                // Kiểm tra nếu cấp độ > 9
                if (level >= 9) {
                    // Tính số lượng item ID 77, bắt đầu từ 100 và tăng 20 mỗi cấp
                    int quantity = 100 + (level - 9) * 20;

                    // Tạo item với ID 77
                    Item item77 = ItemService.gI().createNewItem((short) 77);
                    item77.quantity = quantity;

                    // Thêm item vào danh sách phần thưởng
                    player.itemsWoodChest.add(item77);
                }

                // Phần thưởng đồ tại rương
                int clothesCount = 1;
                if (level >= 5 && level <= 8) {
                    clothesCount = 2;  // Nếu cấp độ từ 5 đến 8, thưởng 2 món đồ
                } else if (level >= 10 && level <= 12) {
                    clothesCount = 3;  // Nếu cấp độ từ 10 đến 12, thưởng 3 món đồ
                }

                // Tạo đồ thưởng (clothes) và thêm vào phần thưởng
                for (int i = 0; i < clothesCount; i++) {
                    int randItemId = randClothes(level);  // Lấy ID ngẫu nhiên của món đồ
                    Item rewardItem = ItemService.gI().createNewItem((short) randItemId);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) randItemId);
                    if (ops != null && !ops.isEmpty()) {
                        rewardItem.itemOptions.addAll(ops);  // Thêm thuộc tính item
                    }
                    rewardItem.quantity = 1;  // Số lượng món đồ là 1
                    player.itemsWoodChest.add(rewardItem);  // Thêm món đồ vào phần thưởng
                }

                // Phần thưởng item ngẫu nhiên (từ rewardItems)
                int[] rewardItems = {17, 18, 19, 20, 380, 381, 382, 383, 384, 385, 1229};
                int rewardCount = 2;  // Số lượng item mặc định

                // Thay đổi số lượng phần thưởng tùy theo cấp độ
                if (level >= 5 && level <= 8) {
                    rewardCount = 3;  // Nếu cấp độ từ 5 đến 8, thưởng 3 item ngẫu nhiên
                } else if (level >= 10 && level <= 12) {
                    rewardCount = 4;  // Nếu cấp độ từ 10 đến 12, thưởng 4 item ngẫu nhiên
                }

                // Thêm item ngẫu nhiên vào phần thưởng
                Set<Integer> selectedItems = new HashSet<>();
                while (selectedItems.size() < rewardCount) {
                    int randItemId = rewardItems[Util.nextInt(0, rewardItems.length - 1)];
                    if (!selectedItems.contains(randItemId)) {
                        selectedItems.add(randItemId);
                        Item rewardItem = ItemService.gI().createNewItem((short) randItemId);
                        rewardItem.quantity = Util.nextInt(1, level);  // Số lượng item phụ thuộc vào cấp độ
                        player.itemsWoodChest.add(rewardItem);  // Thêm item vào phần thưởng
                    }
                }

                // Phần thưởng sao pha lê (nâng cấp)
                int saoPhaLeCount = (level > 9) ? 2 : 1;  // Nếu cấp độ > 9, thêm 2 sao phá lệ
                for (int i = 0; i < saoPhaLeCount; i++) {
                    int rand = Util.nextInt(0, 6);
                    Item level1 = ItemService.gI().createNewItem((short) (441 + rand));
                    level1.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
                    level1.quantity = Util.nextInt(1, 3);  // Số lượng sao phá lệ
                    player.itemsWoodChest.add(level1);  // Thêm sao phá lệ vào phần thưởng
                }

                // Phần thưởng đá nâng cấp
                int dncCount = (level > 9) ? 2 : 1;  // Nếu cấp độ > 9, có 2 đá nâng cấp
                for (int i = 0; i < dncCount; i++) {
                    int rand = Util.nextInt(0, 4);
                    Item dnc = ItemService.gI().createNewItem((short) (220 + rand));
                    dnc.itemOptions.add(new Item.ItemOption(71 - rand, 0));
                    dnc.quantity = Util.nextInt(1, level * 2);  // Số lượng đá nâng cấp phụ thuộc vào cấp độ
                    player.itemsWoodChest.add(dnc);  // Thêm đá nâng cấp vào phần thưởng            
                }

                // Trừ 1 rương gỗ
                InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
                InventoryService.gI().sendItemBags(player);

                // Thêm các phần thưởng vào hành trang
                for (Item it : player.itemsWoodChest) {
                    InventoryService.gI().addItemBag(player, it);
                }
                InventoryService.gI().sendItemBags(player);

                // Cập nhật chỉ số rương gỗ
                player.indexWoodChest = player.itemsWoodChest.size() - 1;
                int i = player.indexWoodChest;
                if (i < 0) {
                    return;
                }
                Item itemWoodChest = player.itemsWoodChest.get(i);
                player.indexWoodChest--;
                String info = "|1|" + itemWoodChest.template.name;
                if (itemWoodChest.quantity > 1) {
                    info += " (x" + itemWoodChest.quantity + ")";
                }

                String info2 = "\n|2|";
                if (!itemWoodChest.itemOptions.isEmpty()) {
                    for (Item.ItemOption io : itemWoodChest.itemOptions) {
                        if (io.optionTemplate.id != 102 && io.optionTemplate.id != 73) {
                            info2 += io.getOptionString() + "\n";
                        }
                    }
                }
                info = (info2.length() > "\n|2|".length() ? (info + info2).trim() : info.trim()) + "\n|0|" + itemWoodChest.template.description;
                NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1, "Bạn nhận được\n"
                        + info.trim(), "OK" + (i > 0 ? " [" + i + "]" : ""));
            }
        }
    }

    public int calculateRequiredEmptySlots(int level) {
        // Khởi tạo số ô trống cần thiết
        int requiredSlots = 0;

        // Tính số lượng vàng
        int baseGoldAmount = 100 * level;
        int randomFactor = Util.nextInt(-15, 15);
        int goldAmount = baseGoldAmount + (baseGoldAmount * randomFactor / 100);

        // Vàng có ID 190, không tính vào số ô trống yêu cầu
        if (goldAmount > 0) {
            requiredSlots++;
        }

        // Tính phần thưởng quần áo
        int clothesCount = 1;
        if (level >= 5 && level <= 8) {
            clothesCount = 2;
        } else if (level >= 10 && level <= 12) {
            clothesCount = 3;
        }
        // Đếm số phần thưởng quần áo
        requiredSlots += clothesCount;

        // Tính phần thưởng item hỗ trợ
        int[] rewardItems = {17, 18, 19, 20, 380, 381, 382, 383, 384, 385, 1229};
        int rewardCount = 2;

        if (level >= 5 && level <= 8) {
            rewardCount = 3;
        } else if (level >= 10 && level <= 12) {
            rewardCount = 4;
        }
        // Đếm phần thưởng item hỗ trợ
        requiredSlots += rewardCount;

        // Tính sao pha lê (Số lượng 2 nếu level > 9)
        int saoPhaLeCount = (level > 9) ? 2 : 1;
        requiredSlots += saoPhaLeCount;

        // Tính đá nâng cấp (Số lượng 2 nếu level > 9)
        int dncCount = (level > 9) ? 2 : 1;
        requiredSlots += dncCount;

        // Trả về tổng số ô trống cần thiết
        return requiredSlots;
    }

    private int randClothes(int level) {
        int result = level - Util.nextInt(2, 4);
        if (result < 1) {
            result = 1;
        }
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][result];
    }

    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }
    private void Black(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.gender; // Lấy giới tính của player
            PetService.gI().changeBlack(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void NguyenTanTai_PhaoHoa(Player pl, Item item) {
        int[][] gold = {{5000, 20000}};
        short[] icon = new short[2];
        icon[0] = item.template.iconID;
        pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
        if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
            pl.inventory.gold = Inventory.LIMIT_GOLD;
        }
        Service.gI().NguyenTanTai_LogicEffect(pl, 1011, 1, -1, 1, 1, 15000);
        Service.gI().NguyenTanTai_LogicEffect(pl, 63, 1, -1, 1, 1, 5000);
        Service.gI().NguyenTanTai_LogicEffect(pl, 64, 1, -1, 1, 1, 5000); // eff này live
        Service.gI().NguyenTanTai_LogicEffect(pl, 65, 1, -1, 1, 1, 5000);

        PlayerService.gI().sendInfoHpMpMoney(pl);

        InventoryService.gI().sendItemBags(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 379: // máy dò capsule
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;

            case 381: // cuồng nộ
                if (pl.itemTime.isUseCuongNo2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            case 382: //bổ huyết
                if (pl.itemTime.isUseBoHuyet2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                Service.gI().point(pl);
                break;
            case 383: //bổ khí
                if (pl.itemTime.isUseBoKhi2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                Service.gI().point(pl);
                break;
            case 384: //giáp xên
                if (pl.itemTime.isUseGiapXen2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                Service.gI().point(pl);
                break;
            case 385: // ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case 764: // Khau trang
                pl.itemTime.lastTimeKhauTrang = System.currentTimeMillis();
                pl.itemTime.isUseKhauTrang = true;
                break;
            case 1150: // cuồng nộ 2
                if (pl.itemTime.isUseCuongNo) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);
                break;

            case 1151: // bổ khí 2
                if (pl.itemTime.isUseBoKhi) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                Service.gI().point(pl);
                break;

            case 1152: // bổ huyết 2
                if (pl.itemTime.isUseBoHuyet) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                Service.gI().point(pl);
                break;

            case 1153: // giáp xên 2
                if (pl.itemTime.isUseGiapXen) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                Service.gI().point(pl);
                break;

            case 1154: // an danh
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;

            case 638: //Commeson
                pl.itemTime.lastTimeUseCMS = System.currentTimeMillis();
                pl.itemTime.isUseCMS = true;
                break;
            case 1233: //Nồi cơm điện
                pl.itemTime.lastTimeUseNCD = System.currentTimeMillis();
                pl.itemTime.isUseNCD = true;
                break;
            case 579:
            case 1045: // Đuôi khỉ
                pl.itemTime.lastTimeUseDK = System.currentTimeMillis();
                pl.itemTime.isUseDK = true;
                break;
            case 663: //bánh pudding
            case 664: //xúc xíc
            case 665: //kem dâu
            case 666: //mì ly
            case 667: //sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            case 880:
            case 881:
            case 882:
                pl.itemTime.lastTimeEatMeal2 = System.currentTimeMillis();
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
                break;

            case 1532: //máy dò đồ
                pl.itemTime.lastTimeUseKhoBauX2 = System.currentTimeMillis();
                pl.itemTime.isUseKhoBauX2 = true;

//            case 2109: //máy dò đồ
//                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
//                pl.itemTime.isUseMayDo2 = true;
                break;
            case 1628: //máy dò đồ
                long currentTime = System.currentTimeMillis();

                // +++
                if (pl.itemTime.isUseBuaSanta) {
                    pl.itemTime.lastTimeBuaSanta += 1_800_000; // 30p
                } else {
                    //null
                    pl.itemTime.lastTimeBuaSanta = currentTime + 1; // 0
                    pl.itemTime.isUseBuaSanta = true;
                }
                break;

        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId >= Shenron_Service.NGOC_RONG_1_SAO && tempId <= Shenron_Service.NGOC_RONG_7_SAO) {
            Shenron_Service.gI().openMenuSummonShenron(pl, 0);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {//Hoc skill moi
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else { // neu chua hoc ma hoc lv cao
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            pl.BoughtSkill.add((int) item.template.id);
                            //System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        pl.idMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {

        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        //Kiểm tra số lượng người trong khu

        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 1000)) {
            return;
        }
        player.lastTimeEatPea = System.currentTimeMillis();
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cám ơn sư phụ");
            }

            InventoryService.gI().subQuantityItemsBag(player, pea, 1);
            InventoryService.gI().sendItemBags(player);
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: //skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: //skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: //skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: //skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    public void OpenHopThanlinh(Player player, int itemUseiD) {
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            Item itemused = InventoryService.gI().findItemBag(player, itemUseiD);

            int[][] items = {{555, 556, 562, 563, 561}, {557, 558, 564, 565, 561}, {559, 560, 566, 567, 561}};
            Item aotl = ItemService.gI().createNewItem((short) items[player.gender][0]);
            Item wTl = ItemService.gI().createNewItem((short) items[player.gender][1]);
            Item gTl = ItemService.gI().createNewItem((short) items[player.gender][2]);
            Item jayTl = ItemService.gI().createNewItem((short) items[player.gender][3]);
            Item RdTl = ItemService.gI().createNewItem((short) items[player.gender][4]);

            RewardService.gI().initChiSoItem(aotl);
            RewardService.gI().initChiSoItem(wTl);
            RewardService.gI().initChiSoItem(gTl);
            RewardService.gI().initChiSoItem(jayTl);
            RewardService.gI().initChiSoItem(RdTl);

            aotl.itemOptions.add(new ItemOption(30, 1));
            wTl.itemOptions.add(new ItemOption(30, 1));
            gTl.itemOptions.add(new ItemOption(30, 1));
            jayTl.itemOptions.add(new ItemOption(30, 1));
            RdTl.itemOptions.add(new ItemOption(30, 1));

            InventoryService.gI().addItemBag(player, aotl);
            InventoryService.gI().addItemBag(player, wTl);
            InventoryService.gI().addItemBag(player, gTl);
            InventoryService.gI().addItemBag(player, jayTl);
            InventoryService.gI().addItemBag(player, RdTl);

            InventoryService.gI().subQuantityItemsBag(player, itemused, 1);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn vừa nhận được Set thần linh");
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu có 5 ô trống hành trang");
        }
    }

    private void ItemManhGiay(Player pl, Item item) {
        if (pl.winSTT && !Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            Service.gI().sendThongBao(pl, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (pl.winSTT && Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            pl.winSTT = false;
            pl.callBossPocolo = false;
            pl.zoneSieuThanhThuy = null;
        }
        NpcService.gI().createMenuConMeo(pl, item.template.id, 564, "Đây chính là dấu hiệu riêng của...\nĐại Ma Vương Pôcôlô\nĐó là một tên quỷ dữ đội lốt người, một kẻ đại gian ác\ncó sức mạnh vô địch và lòng tham không đáy...\nĐối phó với hắn không phải dễ\nCon có chắc chắn muốn tìm hắn không?", "Đồng ý", "Từ chối");
    }

    private void NguyenTanTai_XuongCho(Player pl, Item item) {
        List<Player> bosses = pl.zone.getBosses();
        boolean checkSoi = false;

        synchronized (bosses) {
            for (Player bossPlayer : bosses) {
                if (bossPlayer.id == BossID.SOI_HEC_QUYN && !pl.isDie()) {
                    checkSoi = true;
                }
            }
        }

        if (!checkSoi) {
            Service.gI().sendThongBao(pl, "Không tìm thấy Sói hẹc quyn");
            return;
        }

        synchronized (bosses) {
            for (Player bossPlayer : bosses) {
                if (bossPlayer.id == BossID.SOI_HEC_QUYN) {
                    Boss soihecQuyn = (Boss) bossPlayer;
                    if (soihecQuyn != null) {
                        if (((SoiHecQuyn) soihecQuyn).NguyenTanTai_KiemTraNhatXuong()) {
                            Service.gI().sendThongBao(pl, "Sói đã no rồi");
                            continue;
                        } else {
                            ((SoiHecQuyn) soihecQuyn).NhatXuong();
                            Service.gI().chat(soihecQuyn, "Ê, Cục xương ngon quá");
                        }

                        ItemMap itemMap = null;
                        int x = pl.location.x;
                        if (x < 0 || x >= pl.zone.map.mapWidth) {
                            return;
                        }
                        int y = pl.zone.map.yPhysicInTop(x, pl.location.y - 24);
                        itemMap = new ItemMap(pl.zone, 460, 1, x, y, pl.id);
                        itemMap.isPickedUp = true;
                        itemMap.createTime -= 23000;
                        if (itemMap != null) {
                            Service.gI().dropItemMap(pl.zone, itemMap);
                        }

                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        InventoryService.gI().sendItemBags(pl);

                        if (Util.nextInt(4) < 3) { // 75% cơ hội
                            int rand = Util.nextInt(0, 6); // Random từ 0 đến 6
                            short idItem = (short) (rand + 441); // Item 441 + rand
                            Item it = ItemService.gI().createNewItem(idItem);
                            it.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));

                            if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                                InventoryService.gI().addItemBag(pl, it);
                                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
                            } else {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                            }
                        } else {
                            short idItem = 459; // Item 459
                            Item it = ItemService.gI().createNewItem(idItem);
                            it.itemOptions.add(new Item.ItemOption(112, 80));
                            it.itemOptions.add(new Item.ItemOption(93, 90));
                            it.itemOptions.add(new Item.ItemOption(20, Util.nextInt(10000)));
                            if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                                InventoryService.gI().addItemBag(pl, it);
                                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
                            } else {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                            }
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            // Handle exception
                        }

                        ItemMapService.gI().removeItemMapAndSendClient(itemMap);
                        ((SoiHecQuyn) soihecQuyn).leaveMapNew();
                    }

                }
            }

            InventoryService.gI().sendItemBags(pl);
        }

    }
//NguyenTanTai_XuongCho

    public void NoelItemBox(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int spl = Util.nextInt(441, 445);
            int dnc = Util.nextInt(381, 384);
            int nr = Util.nextInt(17, 20);
            int nrBang = Util.nextInt(925, 931);
            int mts = Util.nextInt(1066, 1070);

            if (Util.isTrue(5, 90)) {
                int ruby = Util.nextInt(10, 20);
                pl.inventory.gem += ruby;
                PlayerService.gI().sendInfoHpMpMoney(pl);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Bạn nhận được " + ruby + "  Ngọc");
            } else {
                int[] temp = {spl, dnc, nr, nrBang, mts, 533, 380};
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
                /*
                # Item Vặt
                 */
                if (temp[index] >= 441 && temp[index] <= 443) {// sao pha le
                    it.itemOptions.add(new ItemOption(temp[index] - 346, 5));
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 444 && temp[index] <= 445) {
                    it.itemOptions.add(new ItemOption(temp[index] - 346, 3));
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 381 && temp[index] <= 384) { // da nang cap
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 1066 && temp[index] <= 1070) { // da nang cap
                    it.quantity = Util.nextInt(1, 5);

                    /*
                   # Item Cải Trang
                     */
                } else if (temp[index] >= 387 && temp[index] <= 393) { // mu noel do
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(80, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                    it.itemOptions.add(new ItemOption(199, 0));
                    /*
                    # Item Pet
                  # Nếu Muốn Random Item Thì:  } else if (temp[index] == 936 && temp[index] == 936) {
                     */
                } else if (temp[index] == 936) { // tuan loc
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                    /*
                    # Item Flag
                    # Nếu Muốn Random Item Thì:  } else if (temp[index] == 822 && temp[index] == 822) {
                     */
                } else if (temp[index] == 822) { // cay thong noel
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                    it.itemOptions.add(new ItemOption(30, 0));
                    it.itemOptions.add(new ItemOption(74, 0));
                    /*
                      # Item VanBay
                      # Nếu Muốn Random Item Thì:  } else if (temp[index] == 746 && temp[index] == 746) {
                     */
                } else if (temp[index] == 746) { // xe truot tuyet
                    it.itemOptions.add(new ItemOption(74, 0));
                    it.itemOptions.add(new ItemOption(30, 0));
                    if (Util.isTrue(99, 100)) {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(30, 360)));
                    }

                } else if (temp[index] == 821) {
                    it.itemOptions.add(new ItemOption(30, 0));
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }
     public void HopQuaChinhChu(Player pl,Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int tst = Util.nextInt(637,642);
          int itc1 = Util.nextInt(1150,1154);

{
                int[] temp = {tst, itc1};
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
               
              
                    /*
                   # Item Cải Trang
                     */
                if (temp[index] == 1503 && temp[index] == 1504) { //oy tiu nun
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                 
                } else if (temp[index] == 681) { // tuan loc
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }
  public void HopQuaNheNhang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int tst = Util.nextInt(381, 385);
          int itc1 = Util.nextInt(628, 636);

{
                int[] temp = {tst, itc1};
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
               
              
                    /*
                   # Item Cải Trang
                     */
                if (temp[index] == 1503 && temp[index] == 1504) { //oy tiu nun
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                 
                } else if (temp[index] == 681) { // tuan loc
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void BlackGokuItemBoxEventNoel(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int hanhtinh = pl.gender;
            int[] caitrangdietquy = {1087, 1088, 1089, 1090, 1091}; // cải trang diệt wỹ

            int itemBlackGoku = Util.nextInt(0, 3) == 0 ? 746
                    : // 1/3%
                    Util.nextInt(0, 3) == 1 ? (hanhtinh == 0 ? 1155 : hanhtinh == 1 ? 1157 : 1156)
                    : // Cải trang Noel
                    Util.nextInt(0, 3) == 2 ? (hanhtinh == 0 ? 1018 : hanhtinh == 1 ? 1019 : 1020)
                    : // Cải trang Broly
                    caitrangdietquy[Util.nextInt(0, caitrangdietquy.length - 1)]; // Cải Trang Diệt Wỹ

            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);
            if (itemBlackGoku == 746) { // Xe trượt tuyết
                if (Util.isTrue(1, 100)) { // 1% ra HSD vĩnh viễn
                    it.itemOptions.add(new ItemOption(73, 0)); // Option 73 với giá trị 0: HSD vĩnh viễn
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30))); // 99% ra HSD 7-30 ngày
                }
            } else if (itemBlackGoku == 1155 || itemBlackGoku == 1156 || itemBlackGoku == 1157
                    || itemBlackGoku == 1018 || itemBlackGoku == 1019 || itemBlackGoku == 1020) { // Cải trang Noel hoặc Broly
                it.itemOptions.add(new ItemOption(50, 23));
                it.itemOptions.add(new ItemOption(77, 23));
                it.itemOptions.add(new ItemOption(103, 23));
                if (Util.isTrue(95, 100)) {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3))); // 90% ra option 93
                } else {
                    it.itemOptions.add(new ItemOption(73, 0)); // 10% ra option 73
                }
            } else if (itemBlackGoku >= 1087 && itemBlackGoku <= 1191) { // Diệt Quỷ
                it.itemOptions.add(new ItemOption(50, 22));
                it.itemOptions.add(new ItemOption(77, 21));
                it.itemOptions.add(new ItemOption(103, 21));
                if (Util.isTrue(95, 100)) {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3))); // 90% ra HSD (1-3 ngày)
                } else {
                    it.itemOptions.add(new ItemOption(73, 0)); // 5% không ra HSD
                }
            }

            // Cập nhật túi đồ
            InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
            InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
            InventoryService.gI().sendItemBags(pl); // send
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }
//NguyenTanTai_XuongCho

    public void NguyenTanTai_C5(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[] itemList = {1087, 1088, 1089, 1090, 1091};

            int itemBlackGoku = itemList[Util.nextInt(0, itemList.length - 1)];
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(1, 16))); //
            it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 17))); // 
            it.itemOptions.add(new ItemOption(103, Util.nextInt(1, 15))); // 
            it.itemOptions.add(new ItemOption(95, Util.nextInt(1, 5))); //
            it.itemOptions.add(new ItemOption(96, Util.nextInt(1, 5))); // 

            int[] options = {94, 97, 108};
            int randomOption = options[Util.nextInt(0, options.length - 1)];
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5)));
            if (Util.isTrue(98, 100)) {
                it.itemOptions.add(new ItemOption(93, 5));
            }
            // Cập nhật túi đồ
            InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
            InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
            InventoryService.gI().sendItemBags(pl); // Gửi cập nhật túi đồ
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void NguyenTanTai_C7(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[] itemList = {1087, 1088, 1089, 1090, 1091};

            int itemBlackGoku = itemList[Util.nextInt(0, itemList.length - 1)];
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(1, 16))); //
            it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 17))); // 
            it.itemOptions.add(new ItemOption(103, Util.nextInt(1, 15))); // 
            it.itemOptions.add(new ItemOption(95, Util.nextInt(1, 5))); //
            it.itemOptions.add(new ItemOption(96, Util.nextInt(1, 5))); // 

            int[] options = {94, 97, 108};
            int randomOption = options[Util.nextInt(0, options.length - 1)];
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5)));
            if (Util.isTrue(100, 100)) {
                it.itemOptions.add(new ItemOption(93, 7));
            }
            // Cập nhật túi đồ
            InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
            InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
            InventoryService.gI().sendItemBags(pl); // Gửi cập nhật túi đồ
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void ChuLunBox(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            // Mảng vật phẩm không giới hạn hành tinh
            int[] itemList = {1158, 1159, 1160, 1161, 1162, 1163, 1164};

            // Chọn ngẫu nhiên một vật phẩm từ danh sách itemList
            int itemBlackGoku = itemList[Util.nextInt(0, itemList.length - 1)];

            // Tạo vật phẩm mới
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);

            // Thêm các item options cố định
            it.itemOptions.add(new ItemOption(50, 11)); // Option 50 = 11
            it.itemOptions.add(new ItemOption(77, 13)); // Option 77 = 13
            it.itemOptions.add(new ItemOption(103, 13)); // Option 103 = 13        

            int[] options = {94, 97, 108}; // Mảng chứa các option
            int randomOption = options[Util.nextInt(0, options.length - 1)]; // Chọn ngẫu nhiên một option từ mảng
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5))); // Thêm option ngẫu nhiên
            if (Util.isTrue(98, 100)) {
                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3))); // 90% ra HSD (1-3 ngày)
            } else {
                it.itemOptions.add(new ItemOption(73, 0)); // 5% không ra HSD
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
            InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
            InventoryService.gI().sendItemBags(pl); // Gửi cập nhật túi đồ
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void NonNoelDo(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int hanhtinh = pl.gender;

            // Chỉ giữ lại cải trang Noel
            int itemBlackGoku = (hanhtinh == 0 ? 387 : hanhtinh == 1 ? 390 : 393);

            // Tạo vật phẩm
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
            it.itemOptions.add(new ItemOption(80, Util.nextInt(30, 50)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
            it.itemOptions.add(new ItemOption(106, 1));

            // 95% ra HSD 1-3 ngày, 5% ra vĩnh viễn
            if (Util.isTrue(95, 100)) {
                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3))); // 95% ra option 93
            } else {
                it.itemOptions.add(new ItemOption(73, 0)); // 5% ra option 73
            }
            // Cập nhật túi đồ
            InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
            InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
            InventoryService.gI().sendItemBags(pl); // send
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void CapsuleKichHoat(Player player) {
        Item item = InventoryService.gI().findItem(player.inventory.itemsBag, 1559);
        if (item == null || item.quantity < 1) {
            return;
        }

        int emtyBag = 1; // check bag nek 
        if (InventoryService.gI().getCountEmptyBag(player) >= emtyBag) {
            // xoa capsule
            InventoryService.gI().subQuantityItemsBag(player, item, 1);

            // random skh theo gender
            int[] ID_Option;
            if (player.gender == 0) { // TraiDat
                ID_Option = new int[]{127, 128, 129}; // id option
            } else if (player.gender == 1) { // Xayda
                ID_Option = new int[]{130, 131, 132}; // id option
            } else { // Namek
                ID_Option = new int[]{133, 134, 135}; // id option
            }

            //random skh
            int ID_TrangBi = ID_Option[Util.nextInt(0, ID_Option.length - 1)];

            // random item vải thô
            short itemId = ConstItem.TrangBiKichHoat[Util.nextInt(0, 4)][player.gender][0];
            Item ID_SetKichHoat = ItemService.gI().createNewItem(itemId);
            // random item vải thô -> hủy diệt
//            short itemId = ConstItem.TrangBiKichHoat[Util.nextInt(0, 4)][player.gender][Util.nextInt(0, ConstItem.TrangBiKichHoat[0][0].length - 1)];
//            Item ID_SetKichHoat = ItemService.gI().createNewItem(itemId);

            // add option
            RewardService.gI().initChiSoItem(ID_SetKichHoat);
            ItemService.gI().NguyenTanTai_Option(ID_SetKichHoat, ID_TrangBi);

            if (ID_SetKichHoat != null) {
                InventoryService.gI().addItemBag(player, ID_SetKichHoat);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Bạn vừa nhận được " + ID_SetKichHoat.template.name);
            }
        } else {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất " + emtyBag + " ô trống");
        }
    }

    public void TuiVang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            pl.inventory.gold += Util.nextInt(100000, 10000000);
            int itemBlackGoku = 190;
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);
            int randomValue = Util.nextInt(3, 333);
            it.itemOptions.add(new ItemOption(1, randomValue));
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Happy");
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void ItemSieuThanThuy(Player pl, Item item) {
        long tnsm = 5_000_000;
        int n = 0;
        switch (item.template.id) {
            case 727:
                n = 2;
                break;
            case 728:
                n = 10;
                break;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        if (Util.isTrue(90, 100)) {
            Service.gI().sendThongBao(pl, "Bạn đã bị chết vì độc của thuốc tăng lực siêu thần thủy.");
            pl.setDie();
        } else {
            for (int i = 0; i < n; i++) {
                Service.gI().addSMTN(pl, (byte) 2, tnsm, true);
            }
        }
    }

    public void UseCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id).findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }
        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }
            Card cardRequire = pl.Cards.stream().filter(r -> r.Id == radarRequireTemplate.Id).findFirst().orElse(null);
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + radarRequireTemplate.Name + " ở cấp độ " + radarTemplate.RequireLevel + " mới có thể sử dụng thẻ này");
                return;
            }
        }
        Card card = pl.Cards.stream().filter(r -> r.Id == item.template.id).findFirst().orElse(null);
        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            pl.Cards.add(newCard);
            RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            if (card.Level >= 2) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        }
    }

    private void RuongNgocRong(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            // Số ngẫu nhiên từ 0 đến 100 để quyết định tỷ lệ
            int random = Util.nextInt(0, 100);

            int itemBlackGoku;

            // 85% xuất hiện item 20, 19, 18, 17
            if (random < 85) {
                int[] itemList = {20, 19, 18, 17};  // Các item có tỉ lệ xuất hiện 85%
                itemBlackGoku = itemList[Util.nextInt(0, itemList.length - 1)];
            } // 10% xuất hiện item 16
            else if (random < 95) {
                itemBlackGoku = 16;  // Item 16 có tỉ lệ 10%
            } // 5% xuất hiện item 14 hoặc 15
            else {
                itemBlackGoku = Util.nextInt(14, 15);  // Item 14 hoặc 15 có tỉ lệ 5%
            }

            // Tạo vật phẩm mới từ ID đã chọn
            Item it = ItemService.gI().createNewItem((short) itemBlackGoku);

            // Kiểm tra nếu người chơi có item 1561 (chìa khóa)
            Item item1561 = InventoryService.gI().findItem(pl.inventory.itemsBag, 1561);
            if (item1561 != null) {
                // Trừ vật phẩm và thêm vật phẩm mới vào túi đồ
                InventoryService.gI().subQuantityItemsBag(pl, item, 1); // Trừ 1 Item Box
                InventoryService.gI().subQuantityItemsBag(pl, item1561, 1); // Trừ 1 Item Box
                InventoryService.gI().addItemBag(pl, it); // Thêm item vào túi
                InventoryService.gI().sendItemBags(pl); // Gửi cập nhật túi đồ
                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Bạn không có chìa khoá vàng");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

}
