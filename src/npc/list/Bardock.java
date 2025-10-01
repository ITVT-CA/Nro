package npc.list;

import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import player.Service.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.Input;
import utils.Util;

public class Bardock extends Npc {

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "CUA HANG QUAY THUONG VIP\n" +
                    "===========================================\n" +
                    "CACH THANH TOAN: Ve quay hoac 20 TV\n" +
                    "Quay Cai Trang (Chi so tu 25-50%)\n" +
                    "Quay Pet (Chi so tu 5-20%)\n" +
                    "Quay Van Bay (Chi so tu 5-20%)\n" +
                    "Quay Thu Deo Lung (Chi so tu 5-30%)\n" +
                    "===========================================\n" +
                    "Quay Cai Trang", 
                     "Quay Cai Trang","Quay Pet", 
                    "Quay Van Bay", 
                    "Quay Thu Deo Lung",
                    // "Doi sang Ruby",
                    "Dong");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        quayCaiTrang(player);
                        break;
                    case 1:
                        quayPet(player);
                        break;
                    case 2:
                        quayVanBay(player);
                        break;
                    case 3:
                        quayThuDeoLung(player);
                        break;
                    // case 4:
                    //     Input.gI().createFormTradeRuby(player);
                    //     break;
                    case 5:
                        Service.gI().sendThongBao(player, "Hẹn gặp lại bạn!");
                        break;
                }
            }
        }
    }
    
    private boolean kiemTraThanhToan(Player player) {
        Item ticketItem = InventoryService.gI().findItemBag(player, 1730);
        
        if (ticketItem != null && ticketItem.quantity > 0) {
            InventoryService.gI().subQuantityItemsBag(player, ticketItem, 1);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Đã sử dụng 1 vé quay!");
            return true;
        }

        Item goldItem = InventoryService.gI().findItemBag(player, 457);
        if (goldItem != null && goldItem.quantity >= 20) {
            InventoryService.gI().subQuantityItemsBag(player, goldItem, 20);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Đã sử dụng 20 thỏi vàng để quay!");
            return true;
        }
        
        Service.gI().sendThongBao(player, "Bạn không có vé quay hoặc không đủ 20 thỏi vàng!");
        return false;
    }
    
    private void quayCaiTrang(Player player) {
        if (!kiemTraThanhToan(player)) return;
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy!");
            return;
        }
        
        int[] itemIds = {421, 422, 450, 549, 528, 612, 613, 614};
        int[] itemIdsVIP = {1731, 1557, 1590, 1732, 1587, 1743, 1716};
        
        int chance = Util.nextInt(1, 100);
        short selectedItemId;
        
        if (chance <= 10) {
            selectedItemId = (short) itemIdsVIP[Util.nextInt(0, itemIdsVIP.length - 1)];
        } else {
            selectedItemId = (short) itemIds[Util.nextInt(0, itemIds.length - 1)];
        }
        
        Item item = ItemService.gI().createNewItem(selectedItemId);
        
        if (selectedItemId >= 421 && selectedItemId <= 614) {
            item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(20, 30)));
            item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(20, 30)));
            item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 30)));
        } else if (selectedItemId == 1731) {
            item.itemOptions.add(new Item.ItemOption(50, 50));
            item.itemOptions.add(new Item.ItemOption(77, 50));
            item.itemOptions.add(new Item.ItemOption(103, 50));
        } else {
            item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(35, 50)));
            item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(35, 50)));
            item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(35, 50)));
        }
        
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được: " + item.template.name);
    }
    
    private void quayPet(Player player) {
        if (!kiemTraThanhToan(player)) return;
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy!");
            return;
        }
        
        int[] petIds = {1458, 1551, 16, 17, 18, 19};
        int[] petIdsVIP = {1631, 1573};
        
        int chance = Util.nextInt(1, 100);
        short selectedPetId;
        
        if (chance <= 15) {
            selectedPetId = (short) petIdsVIP[Util.nextInt(0, petIdsVIP.length - 1)];
        } else {
            selectedPetId = (short) petIds[Util.nextInt(0, petIds.length - 1)];
        }
        
        Item pet = ItemService.gI().createNewItem(selectedPetId);
        
        if (selectedPetId != 16 && selectedPetId != 17 && selectedPetId != 18 && selectedPetId != 19) {
            if (chance <= 15) {
                pet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                pet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                pet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
            } else {
                pet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                pet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                pet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
            }
        }
        
        InventoryService.gI().addItemBag(player, pet);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "🐾 Bạn nhận được: " + pet.template.name);
    }
    
    private void quayVanBay(Player player) {
        if (!kiemTraThanhToan(player)) return;
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy!");
            return;
        }
        
        int[] vanBayIds = {1603, 1711, 16, 18, 19, 20};
        int[] vanBayIdsVIP = {1734, 1733};
        
        int chance = Util.nextInt(1, 100);
        short selectedVanBayId;
        
        if (chance <= 20) {
            selectedVanBayId = (short) vanBayIdsVIP[Util.nextInt(0, vanBayIdsVIP.length - 1)];
        } else {
            selectedVanBayId = (short) vanBayIds[Util.nextInt(0, vanBayIds.length - 1)];
        }
        
        Item vanBay = ItemService.gI().createNewItem(selectedVanBayId);
        
        if (selectedVanBayId != 16 && selectedVanBayId != 17 && selectedVanBayId != 18 && selectedVanBayId != 19) {
            if (chance <= 20) {
                vanBay.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                vanBay.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                vanBay.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
            } else {
                vanBay.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                vanBay.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                vanBay.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
            }
        }
        
        InventoryService.gI().addItemBag(player, vanBay);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được: " + vanBay.template.name);
    }
    
    private void quayThuDeoLung(Player player) {
        if (!kiemTraThanhToan(player)) return;
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy!");
            return;
        }
        
        int[] thuDeoLungIds = {16, 17, 18, 19, 20,467,468,469};
        int[] thuDeoLungIdsVIP = {741,740,745};
        
        int chance = Util.nextInt(1, 100);
        short selectedThuDeoLungId;
        
        if (chance <= 12) {
            selectedThuDeoLungId = (short) thuDeoLungIdsVIP[Util.nextInt(0, thuDeoLungIdsVIP.length - 1)];
        } else {
            selectedThuDeoLungId = (short) thuDeoLungIds[Util.nextInt(0, thuDeoLungIds.length - 1)];
        }
        
        Item thuDeoLung = ItemService.gI().createNewItem(selectedThuDeoLungId);
        
        if (selectedThuDeoLungId != 16 && selectedThuDeoLungId != 17 && selectedThuDeoLungId != 18 && selectedThuDeoLungId != 19) {
            if (chance <= 12) {
                thuDeoLung.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 30)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(77, Util.nextInt(25, 30)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(103, Util.nextInt(25, 30)));
            } else {
                thuDeoLung.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 25)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 25)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 25)));
            }
        }
        
        InventoryService.gI().addItemBag(player, thuDeoLung);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được: " + thuDeoLung.template.name);
    }
}