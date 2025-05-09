package befly.common.s3;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class S3Properties {
    @Value("${s3.accessKey}")
    private String accessKey;

    @Value("${s3.secretKey}")
    private String secretKey;

    @Value("${s3.endPoint}")
    private String endPoint;

    @Value("${s3.region}")
    private String region;

    @Value("${s3.expiration}")
    private int expiration;

    @Value("${s3.bucketName}")
    private String bucketName;

    @Value("${s3.folderName}")
    private String folderName;

    @Value("${s3.projectId}")
    private String projectId;
}
