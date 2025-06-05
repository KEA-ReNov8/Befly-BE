package befly.community.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {

    public static String formatTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = duration.toDays();

        if (seconds < 60) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days == 1) {
            return "어제";
        } else if (days < 7) {
            return days + "일 전";
        } else {
            return createdAt.toLocalDate().toString(); // yyyy-mm-dd
        }
    }
}
