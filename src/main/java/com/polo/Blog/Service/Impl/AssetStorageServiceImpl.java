package com.polo.Blog.Service.Impl;

import com.polo.Blog.Service.AssetStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class AssetStorageServiceImpl implements AssetStorageService {
    private static final int SMALL_IMAGE_MAX_EDGE = 420;
    private static final int MEDIUM_IMAGE_MAX_EDGE = 960;
    private static final String SMALL_SUFFIX = "__sm";
    private static final String MEDIUM_SUFFIX = "__md";

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName:myblog}")
    private String bucketName;

    @Value("${minio.publicEndpoint:${minio.endpoint:http://localhost:9000}}")
    private String publicEndpoint;

    @Override
    public String store(MultipartFile file) throws Exception {
        String objectName = buildObjectName(file.getOriginalFilename());
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        byte[] originalBytes = file.getBytes();

        uploadObject(objectName, originalBytes, contentType);
        createImageVariantsIfNeeded(objectName, originalBytes, contentType);
        return buildPublicUrl(objectName);
    }

    private void createImageVariantsIfNeeded(String objectName, byte[] originalBytes, String contentType) {
        String formatName = resolveImageFormat(objectName, contentType);
        if (formatName == null) {
            return;
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (originalImage == null) {
                return;
            }

            uploadVariant(objectName, originalImage, originalBytes, contentType, formatName, SMALL_IMAGE_MAX_EDGE, SMALL_SUFFIX);
            uploadVariant(objectName, originalImage, originalBytes, contentType, formatName, MEDIUM_IMAGE_MAX_EDGE, MEDIUM_SUFFIX);
        } catch (Exception ignored) {
            // 变体生成失败时保留原图，避免阻断上传流程。
        }
    }

    private void uploadVariant(
            String originalObjectName,
            BufferedImage originalImage,
            byte[] originalBytes,
            String contentType,
            String formatName,
            int maxEdge,
            String suffix
    ) throws Exception {
        String variantObjectName = appendSuffix(originalObjectName, suffix);
        if (originalImage.getWidth() <= maxEdge && originalImage.getHeight() <= maxEdge) {
            uploadObject(variantObjectName, originalBytes, contentType);
            return;
        }

        BufferedImage resizedImage = resizeImage(originalImage, maxEdge, "png".equals(formatName));
        byte[] variantBytes = writeImageBytes(resizedImage, formatName);
        uploadObject(variantObjectName, variantBytes, contentType);
    }

    private BufferedImage resizeImage(BufferedImage source, int maxEdge, boolean preserveAlpha) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double scale = Math.min((double) maxEdge / Math.max(sourceWidth, sourceHeight), 1D);

        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);

        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return resized;
    }

    private byte[] writeImageBytes(BufferedImage image, String formatName) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
            if (!writers.hasNext()) {
                throw new IllegalStateException("不支持的图片格式: " + formatName);
            }

            ImageWriter writer = writers.next();
            try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
                writer.setOutput(imageOutputStream);
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                if (writeParam.canWriteCompressed() && ("jpg".equals(formatName) || "jpeg".equals(formatName))) {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(0.82f);
                }
                writer.write(null, new IIOImage(image, null, null), writeParam);
            } finally {
                writer.dispose();
            }

            return outputStream.toByteArray();
        }
    }

    private void uploadObject(String objectName, byte[] bytes, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    private String buildObjectName(String originalFilename) {
        String extension = resolveExtension(originalFilename);
        return UUID.randomUUID() + extension;
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }

    private String resolveImageFormat(String objectName, String contentType) {
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            return null;
        }
        String extension = objectName.substring(objectName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            default -> null;
        };
    }

    private String appendSuffix(String objectName, String suffix) {
        int dotIndex = objectName.lastIndexOf('.');
        if (dotIndex < 0) {
            return objectName + suffix;
        }
        return objectName.substring(0, dotIndex) + suffix + objectName.substring(dotIndex);
    }

    private String buildPublicUrl(String objectName) {
        return publicEndpoint + "/" + bucketName + "/" + objectName;
    }
}
