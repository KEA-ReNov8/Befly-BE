package befly.common.service;

import static befly.common.code.status.S3ErrorStatus.INVALID_FILE_FORMAT;
import static befly.common.code.status.S3ErrorStatus.INVALID_S3_METHOD;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.common.s3.S3Properties;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class S3Service implements S3Interface {
    private final AmazonS3Client s3Client;
    private final S3Properties s3Properties;
    @Override
    public String createPreSignedUrl(String key, String httpMethod) {
        if (!"PUT".equalsIgnoreCase(httpMethod)) {
            throw new RestApiException(INVALID_S3_METHOD);
        }
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += s3Properties.getExpiration() * 1000;
        expiration.setTime(expTimeMillis);

        String objectKey = convertImageName(key);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(s3Properties.getBucketName(), objectKey)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }

    private String convertImageName(String key) {
        int lastDotIndex = key.lastIndexOf('.');
        String fileNameWithoutExtension;
        String extension = "";

        if (lastDotIndex != -1) {
            fileNameWithoutExtension = key.substring(0, lastDotIndex);
            extension = key.substring(lastDotIndex);
        } else {
            throw new RestApiException(INVALID_FILE_FORMAT);
        }
        return s3Properties.getFolderName() + "/" + fileNameWithoutExtension + "_"
                + getCurrentTimeStamp()
                + extension;
    }

    /**
     * 현재 시각을 문자열로 변환합니다.
     * @return 현재시각을 문자열로 변환한 값
     */
    private String getCurrentTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return now.format(formatter);
    }
}
