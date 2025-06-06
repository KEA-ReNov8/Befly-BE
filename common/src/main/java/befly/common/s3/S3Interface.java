package befly.common.s3;

public interface S3Interface {
    /**
     * key를 기준으로 프론트에서 직접 요청 가능한 일회용의 url을 생성합니다.
     * @param key        객체 키(unique한 파일이름)
     * @param httpMethod 요청 HTTP 메서드 ("GET":다운로드용 또는 "PUT":업로드용)
     * @return           pre-signed url
     */
    String createPreSignedUrl(String key, String httpMethod);

    /**
     * 파일 이름은 기존 파일명 + 현재 시각(ms)까지 해서 파일 덮어쓰기 방지하기
     * unique한 파일명이 중요
     *
     * bucketName, region, url 유효기간 등등은 application.yml파일에 둘 예정 -> 추가해야함(User만 되어있음)
     * 백엔드에선 Pre-Signed url 발급해주는 것이 책임의 끝
     * 나머진 프론트에서 처리
     *
     * Community service의 local,dev folder명은 dev_free_img, dev_share_img
     */
}
