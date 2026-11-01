package projetMichelin.repository;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Repository
public class S3Repository {

    @Value("${s3.url}")
    private String s3Url;

    @Value("${s3.accessKeyId}")
    private String s3AccessKeyId;

    @Value("${s3.secretAccessKey}")
    private String s3SecretKey;

    //pour envoyer une image
    public String getUploadFileUrl(String bucket, String key) {
        try (var s3Presigner = creerPresigner()) {
            PutObjectPresignRequest request = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                            .build())
                    .build();
            return s3Presigner.presignPutObject(request).url().toExternalForm();
        }
    }

    //voir une image
    public String getFileUrl(String bucket, String key) {
        try (var s3Presigner = creerPresigner()) {
            GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build())
                    .build();
            return s3Presigner.presignGetObject(request).url().toExternalForm();
        }
    }

    //on Crée l'objet qui signe les liens
    private S3Presigner creerPresigner() {
        return S3Presigner.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.builder().accessKeyId(s3AccessKeyId)
                        .secretAccessKey(s3SecretKey).build())).endpointOverride(URI.create(s3Url)) .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}