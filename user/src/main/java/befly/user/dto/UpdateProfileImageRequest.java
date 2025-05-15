package befly.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileImageRequest {
    private String imageKey; // S3에 업로드된 이미지의 키
} 