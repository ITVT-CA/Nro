package utils;

import java.util.Random;
import java.util.regex.Pattern;
import player.Player;

public class Functions {

    // Sử dụng const String cho biểu thức regex
    private static final String REGEX = "(?i)\\b(dkm|đkm|đbrr|địt|đĩ|đỹ|cm|cmm|lồn|buồi|cc|ôm cl|mẹ mày|cặc|đụ|fuck|damn|clmm|dcmm|cl|tml|đ*t|c*c|dit|d*t|c.a.c|l.o.n|c.ặ.c|l.ồ.n|b.u.ồ.i|bu*i|đặc cầu|đồn lầu|bú cu|buscu|đm|cc|đb|db|lol|nhu lon|nhu cac|vc|vl|vãi|đéo|đờ mờ|đờ cờ mờ|clgt|dell|mẹ|cứt|shit|idiot|khốn|xiên chết|cụ|giao phối|thiểu năng|ngáo|chó|dog|đcmm|vcl|vkl|đ!t|d!t|đỵt|dyt|ngu|óc|.com|.net|.online|.vn|.pw|.pro|.org|.info|.ml|.ga|.gq|.cf|.fun|.xyz|.io|.club)\\b";
    private static final Pattern pattern = Pattern.compile(REGEX);

    // Kiểm tra spam
    public static boolean isSpam(Player player, String text) {
        return pattern.matcher(text).find() && !"TaidzSieucapVipPro".equals(player.name);
    }

    // Giới hạn giá trị long về int
    public static int maxint(long n) {
        return (int) Math.min(n, Integer.MAX_VALUE);
    }

    // Tạo chuỗi ngẫu nhiên
    public static String generateRandomCharacters(int quantity) {
        StringBuilder sb = new StringBuilder(quantity);
        Random random = new Random();

        // Tạo ngẫu nhiên ký tự số hoặc chữ
        for (int i = 0; i < quantity; i++) {
            char generatedChar = (char) (random.nextInt(36) + (random.nextInt(2) == 0 ? '0' : 'A'));
            sb.append(generatedChar);
        }

        return sb.toString();
    }

    // Tạm dừng luồng
    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Cải thiện việc xử lý InterruptedException
        }
    }

}
