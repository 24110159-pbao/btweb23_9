package vn.iotstar.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image lên Cloudinary.
     *
     * @param file file ảnh từ MultipartFile
     * @return kết quả upload từ Cloudinary
     */
    public Map<String, Object> upload(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File ảnh không được để trống"
            );
        }

        validateImage(file);

        try {

            Map<String, Object> options =
                    new HashMap<>();

            /*
             * Folder lưu ảnh Product.
             */
            options.put(
                    "folder",
                    "shop-management/products"
            );

            /*
             * Cloudinary tự sinh public_id.
             */
            options.put(
                    "resource_type",
                    "image"
            );

            return cloudinary
                    .uploader()
                    .upload(
                            file.getBytes(),
                            options
                    );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Không thể upload ảnh lên Cloudinary",
                    e
            );
        }
    }

    /**
     * Xóa image trên Cloudinary bằng publicId.
     */
    public void delete(
            String publicId
    ) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {

            cloudinary
                    .uploader()
                    .destroy(
                            publicId,
                            Map.of(
                                    "resource_type",
                                    "image"
                            )
                    );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Không thể xóa ảnh trên Cloudinary",
                    e
            );
        }
    }

    /**
     * Kiểm tra file upload có phải image hay không.
     */
    private void validateImage(
            MultipartFile file
    ) {
        String contentType =
                file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "File upload phải là hình ảnh"
            );
        }
    }
}
