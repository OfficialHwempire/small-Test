package com.example.smalltest.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3PrivateFileService {

    //s3 버킷을 제어하는 객체
    private S3Client s3Client;

    //Pre-signed URL 생성용 객체
    //S3 접근을 위한 임시 url, 일정 시간 지나면 만료, public 접근이 불가능해짐
    private S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    //s3에 연결해서 인증을 처리하는 로직 작성
    @PostConstruct // 클래스를 기반으로 객체가 생성될 때 1번만 자동 실행되는 어노테이션
    private void initializeAmazonS3Client(){
        //엑세스 키와 시크릿 키를 이용해서 계정 인증 받기
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        //S3 Presigner 초기화
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }


    public String uploadToS3Bucket(MultipartFile file) throws IOException {
        //1. 고유한 파일명 생성(UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

        //2. 업로드할 요청 객체 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(file.getContentType())
                .build();

        //3.실제 s3에 파일 업로드

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );
        //4. Pre-signed URL 생성 및 반환(1분동안 유효)
        return generatePresignedUrl(uniqueFileName,1);
    }

    //pre-signed url 생성
    private String generatePresignedUrl(String uniqueFileName, int durationMinutes) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .getObjectRequest(request)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

        log.info("Pre-signed URL: {}", presignedGetObjectRequest.url().toString());
        return presignedGetObjectRequest.url().toString();
    }

    //실제로는 폴더가 아니고 prefix로 파일을 구분
    public String uploadFileToFolder(MultipartFile file,String folder) throws IOException {
        //1. 고유한 파일명 생성(UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = folder +UUID.randomUUID() + "_" + originalFilename;

        //2. 업로드할 요청 객체 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(file.getContentType())
                .build();

        //3.실제 s3에 파일 업로드

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );
        //4. 업로드된 파일의 URL 반환
        return s3Client.utilities()
                .getUrl(x-> x.bucket(bucketName).key(uniqueFileName))
                .toString();
    }

            // 우리가 가진 데이터: https://s3-bucket-practice8917.s3.ap-northeast-2.amazonaws.com/74b59c79-d5da-4d05-b99a-557f00b4da07_fileName.gif
            // 가공 결과: 74b59c79-d5da-4d05-b99a-557f00b4da07_fileName.gif
    //버킷의 객체를 지우기 위해서는 키값을 줘야 함 (파일 이름)

    //파일 색제 로직
    public void deleteFile(String imageUrl) throws Exception {
        // getPath() -> 프로토콜,ip(도메인),포트 번호를 제외한 리소스 내부 경러만 받음
        //맨 앞에 있는 "/" 를 떼기 위해서 substring을 진행
        String key = extractFileNameFromUrl(imageUrl);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    //여러 파일 일괄 삭제
    public void deleteFiles(List<String> imageUrls) throws Exception {

        List<String> fileNames = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            fileNames.add(extractFileNameFromUrl(imageUrl));
        }
        List<ObjectIdentifier> objectIdentifiers = fileNames.stream().map(x ->
                ObjectIdentifier.builder()
                        .key(x)
                        .build()
        ).toList();

        Delete deleteBuild = Delete.builder()
                .objects(objectIdentifiers)
                .build();
        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(deleteBuild)
                .build();
        s3Client.deleteObjects(deleteRequest);
    }
    //파일 다운로드 요청
    public byte[] downloadFile(String fileUrl) throws Exception {
        String fileName = extractFileNameFromUrl(fileUrl);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(request);
        return objectAsBytes.asByteArray();
    }
    //파일 존재 여부 확인
    public boolean isFileExist(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
           if(e.statusCode()==404) return false;
           else throw new RuntimeException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String extractFileNameFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        String decode = URLDecoder.decode(url.getPath(),"UTF-8");
        return decode.substring(1);
    }


}
