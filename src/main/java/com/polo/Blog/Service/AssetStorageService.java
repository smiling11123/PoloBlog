package com.polo.Blog.Service;

import org.springframework.web.multipart.MultipartFile;

public interface AssetStorageService {
    String store(MultipartFile file) throws Exception;
}
