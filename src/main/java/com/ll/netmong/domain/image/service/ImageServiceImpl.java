package com.ll.netmong.domain.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ll.netmong.domain.image.entity.Image;
import com.ll.netmong.domain.image.repository.ImageRepository;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final AmazonS3Client amazonS3Client;
    private final ImageRepository imageRepository;

    @Value("${cloud.aws.s3.resized-bucket}")
    private String resizedBucket;

    @Value("${cloud.aws.s3.bucket}")
    private String originalBucket;

    @Value("${cloud.aws.s3.url}")
    private String originalBucketUrl;

    @Transactional
    public <T> Optional<Image> uploadImage(T requestType, MultipartFile file) throws IOException {
        String imageLocation = originalBucketUrl;
        String imageName = file.getOriginalFilename();
        String requestTypeSimpleName = requestType.getClass().getSimpleName() + "/";

        String imagePath = imageLocation + requestTypeSimpleName + imageName;

        String fileName = requestTypeSimpleName + file.getOriginalFilename();

        Optional<Image> image = Optional.empty();

        if (requestType instanceof Product) {
            Image productImage = Product.createProductImage(fileName, imagePath);
            image = Optional.of(productImage);
        }

        if (requestType instanceof Post) {
            Image postImage = Post.createProductImage(fileName, imagePath);
            image = Optional.of(postImage);
        }

        if (image.isPresent()) {
            imageRepository.save(image.get());
            uploadOriginalImage(originalBucket, fileName, file);
            uploadResizedImage(resizedBucket, fileName, file);
        }

        return image;
    }

    private void uploadOriginalImage(String bucketName, String fileName, MultipartFile image) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        amazonS3Client.putObject(bucketName, fileName, image.getInputStream(), metadata);
    }

    private void uploadResizedImage(String bucketName, String fileName, MultipartFile image) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        byte[] buffer = getResizedImageStream(image);
        metadata.setContentLength(buffer.length);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        amazonS3Client.putObject(bucketName, fileName, inputStream, metadata);
    }

    private byte[] getResizedImageStream(MultipartFile image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(image.getInputStream()).size(400, 400).toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}
