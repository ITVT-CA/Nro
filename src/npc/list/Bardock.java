package npc.list;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import player.Service.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
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
                    "CỬA HÀNG QUAY THƯỞNG VIP \n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "QUAY CẢI TRANG:\n" +
                    "• Cải trang Rose: Chỉ số MAX---" +
                    "• Cải trang VIP: 35-50 chỉ số\n" +
                    "• Giáp luyện tập cấp 4---" +
                    "• Cải trang thường: 30 chỉ số\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "QUAY PET:\n" +
                    "• Pet VIP: Chỉ số cao---" +
                    "• Pet thường: Chỉ số trung bình\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    " QUAY VÁN BAY:\n" +
                    "• Ván bay VIP: Chỉ số  cao---" +
                    "• Ván bay thường: Chỉ số bình thường\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "QUAY THÚ ĐEO LƯNG:\n" +
                    "• Thú đeo lưng VIP: Chỉ số cao---" +
                    "• Thú đeo lưng thường: Chỉ số bình thường\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    " Quay Cải Trang\n", 
                    "Quay Pet\n", 
                    "Quay Ván Bay\n", 
                    "Quay Thú Đeo Lưng\n",
                    "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> { // Quay Cải Trang
                        quayCaiTrang(player);
                    }
                    case 1 -> { // Quay Pet
                        quayPet(player);
                    }
                    case 2 -> { // Quay Ván Bay
                        quayVanBay(player);
                    }
                    case 3 -> { // Quay Thú Đeo Lưng
                        quayThuDeoLung(player);
                    }
                    case 4 -> { // Đóng
                        Service.gI().sendThongBao(player, "Hẹn gặp lại bạn!");
                    }
                }
            }
        }
    }
    
    // Hàm quay cải trang
    private void quayCaiTrang(Player player) {
        Item ticketItem = InventoryService.gI().findItemBag(player, 1730);
        if (ticketItem == null || ticketItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "🎭 Bạn không có vé quay cải trang !");
            return;
        }
        
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, " Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        
        // Trừ vé quay
        InventoryService.gI().subQuantityItemsBag(player, ticketItem, 1);
        InventoryService.gI().sendItemBags(player);
        
        // Danh sách item thường
        int[] itemIds = {421, 422, 450, 549, 528, 612, 613, 614};
        // Danh sách item VIP
        int[] itemIdsVIP = {1731, 1557, 1590, 1732, 1587, 1743, 1716};
        
        // 10% VIP, 90% thường
        short selectedItemId;
        int chance = Util.nextInt(1, 100);
        
        if (chance <= 10) {
            int randomIndex = Util.nextInt(0, itemIdsVIP.length - 1);
            selectedItemId = (short) itemIdsVIP[randomIndex];
        } else {
            int randomIndex = Util.nextInt(0, itemIds.length - 1);
            selectedItemId = (short) itemIds[randomIndex];
        }
        
        Item item = ItemService.gI().createNewItem(selectedItemId);
        
        // Thêm option cho cải trang
        if (selectedItemId >= 421 && selectedItemId <= 614) {
            item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(20, 30)));
            item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(20, 30)));
            item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 30)));
        } else if (selectedItemId == 1731) {
            item.itemOptions.add(new Item.ItemOption(50, 50));
            item.itemOptions.add(new Item.ItemOption(77, 50));
            item.itemOptions.add(new Item.ItemOption(103, 50));
        } else if (selectedItemId == 1557 || selectedItemId == 1590 || selectedItemId == 1732 || selectedItemId == 1587 || selectedItemId == 1743) {
            item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(35, 50)));
            item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(35, 50)));
            item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(35, 50)));
        }
        
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "🎭 Bạn nhận được: " + item.template.name);
    }
    
    // Hàm quay pet
    private void quayPet(Player player) {
        Item ticketItem = InventoryService.gI().findItemBag(player, 1730);
        if (ticketItem == null || ticketItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "🐾 Bạn không có vé quay pet!");
            return;
        }
        
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        
        // Trừ vé quay
        InventoryService.gI().subQuantityItemsBag(player, ticketItem, 1);
        InventoryService.gI().sendItemBags(player);
        
        // Danh sách pet thường
        int[] petIds = {1458,1551,16,17,18,19}; // Pet thường
        // Danh sách pet VIP
        int[] petIdsVIP = {1631,1573}; // Pet VIP
        
        // 15% VIP, 85% thường
        short selectedPetId;
        int chance = Util.nextInt(1, 100);
        
        if (chance <= 15) {
            int randomIndex = Util.nextInt(0, petIdsVIP.length - 1);
            selectedPetId = (short) petIdsVIP[randomIndex];
        } else {
            int randomIndex = Util.nextInt(0, petIds.length - 1);
            selectedPetId = (short) petIds[randomIndex];
        }
        
        Item pet = ItemService.gI().createNewItem(selectedPetId);
        
        // Thêm option cho pet (trừ ID 16, 17, 18, 19)
        if (selectedPetId != 16 && selectedPetId != 17 && selectedPetId != 18 && selectedPetId != 19) {
            if (chance <= 15) {
                // Pet VIP
                pet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                pet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                pet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
            } else {
                // Pet thường
                pet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                pet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                pet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
            }
        }
        
        InventoryService.gI().addItemBag(player, pet);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "🐾 Bạn nhận được: " + pet.template.name);
    }
    
    // Hàm quay ván bay
    private void quayVanBay(Player player) {
        Item ticketItem = InventoryService.gI().findItemBag(player, 1730);
        if (ticketItem == null || ticketItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có vé quay !");
            return;
        }
        
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        
        // Trừ vé quay
        InventoryService.gI().subQuantityItemsBag(player, ticketItem, 1);
        InventoryService.gI().sendItemBags(player);
        
        // Danh sách ván bay thường
        int[] vanBayIds = {1603, 1711, 16,18,19,20}; // Ván bay thường
        // Danh sách ván bay VIP
        int[] vanBayIdsVIP = {1734, 1733}; // Ván bay VIP
        
        // 20% VIP, 80% thường
        short selectedVanBayId;
        int chance = Util.nextInt(1, 100);
        
        if (chance <= 20) {
            int randomIndex = Util.nextInt(0, vanBayIdsVIP.length - 1);
            selectedVanBayId = (short) vanBayIdsVIP[randomIndex];
        } else {
            int randomIndex = Util.nextInt(0, vanBayIds.length - 1);
            selectedVanBayId = (short) vanBayIds[randomIndex];
        }
        
        Item vanBay = ItemService.gI().createNewItem(selectedVanBayId);
        
        // Thêm option cho ván bay (trừ ID 16, 17, 18, 19)
        if (selectedVanBayId != 16 && selectedVanBayId != 17 && selectedVanBayId != 18 && selectedVanBayId != 19) {
            if (chance <= 20) {
                // Ván bay VIP
                vanBay.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                vanBay.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                vanBay.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
            } else {
                // Ván bay thường
                vanBay.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                vanBay.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                vanBay.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
            }
        }
        
        InventoryService.gI().addItemBag(player, vanBay);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "🛸 Bạn nhận được: " + vanBay.template.name);
    }
    
    // Hàm quay thú đeo lưng
    private void quayThuDeoLung(Player player) {
        Item ticketItem = InventoryService.gI().findItemBag(player, 1730);
        if (ticketItem == null || ticketItem.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn không có vé quay thú đeo lưng!");
            return;
        }
        
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, " Hành trang đầy, không thể nhận thưởng!");
            return;
        }
        
        // Trừ vé quay
        InventoryService.gI().subQuantityItemsBag(player, ticketItem, 1);
        InventoryService.gI().sendItemBags(player);
        
        // Danh sách thú đeo lưng thường
        int[] thuDeoLungIds = {1670, 1669, 1679, 1680, 16,17,18,19}; // Thú đeo lưng thường
        // Danh sách thú đeo lưng VIP
        int[] thuDeoLungIdsVIP = {1713, 1467, 1735, 1599}; // Thú đeo lưng VIP
        
        // 12% VIP, 88% thường
        short selectedThuDeoLungId;
        int chance = Util.nextInt(1, 100);
        
        if (chance <= 12) {
            int randomIndex = Util.nextInt(0, thuDeoLungIdsVIP.length - 1);
            selectedThuDeoLungId = (short) thuDeoLungIdsVIP[randomIndex];
        } else {
            int randomIndex = Util.nextInt(0, thuDeoLungIds.length - 1);
            selectedThuDeoLungId = (short) thuDeoLungIds[randomIndex];
        }
        
        Item thuDeoLung = ItemService.gI().createNewItem(selectedThuDeoLungId);
        
        // Thêm option cho thú đeo lưng (trừ ID 16, 17, 18, 19)
        if (selectedThuDeoLungId != 16 && selectedThuDeoLungId != 17 && selectedThuDeoLungId != 18 && selectedThuDeoLungId != 19) {
            if (chance <= 12) {
                // Thú đeo lưng VIP - chỉ số rất cao
                thuDeoLung.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 20)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 20)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 20)));
            } else {
                // Thú đeo lưng thường - chỉ số cao
                thuDeoLung.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                thuDeoLung.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
            }
        }
        
        InventoryService.gI().addItemBag(player, thuDeoLung);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được: " + thuDeoLung.template.name);
    }
}

