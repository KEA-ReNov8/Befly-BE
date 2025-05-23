package befly.common.service;

import static befly.common.code.status.S3ErrorStatus.INVALID_FILE_FORMAT;
import static befly.common.code.status.S3ErrorStatus.INVALID_S3_METHOD;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.common.s3.S3Properties;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3Service implements S3Interface {
    private final AmazonS3 amazonS3Client;
    private final S3Properties s3Properties;
    @Override
    public String createPreSignedUrl(String key, String httpMethod) {
        //expire 설정
        Date expiration = new Date();
        long expTimeMills = expiration.getTime();
        expTimeMills += 1000L * s3Properties.getExpiration(); //1시간
        expiration.setTime(expTimeMills);

        if("PUT".equalsIgnoreCase(httpMethod)) {
            return amazonS3Client.generatePresignedUrl(
                    s3Properties.getBucketName(),
                    convertImageName(key),
                    expiration).toString();
        }else {
            throw new RestApiException(INVALID_S3_METHOD);
        }
    }

    //https://objectstorage.kr-central-2.kakaocloud.com + v1 + projectId + bucketName + folderName + imageName
    @Override
    public String getImageUrl(String key) {
        return s3Properties.getEndPoint() + "/v1/"
                + s3Properties.getProjectId() + "/"
                + s3Properties.getBucketName() + "/"
                + s3Properties.getFolderName() + "/"
                + convertImageName(key);
    }
    /**
     * 주어진 이미지 파일 이름을 중복되지 않게 현재 시각과 합쳐 반환합니다.
     * @param key  이미지 파일 이름(확장자 포함)
     * @return     새로운 파일 이름(현재 시각 포함)
     */
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
        return fileNameWithoutExtension + "_"
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
