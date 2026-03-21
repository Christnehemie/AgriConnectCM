package com.agriconnect.service_messagerie.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:agriconnect/messagerie}")
    private String folder;

    /**
     * Upload un fichier sur Cloudinary et retourne :
     *   - "url"         : URL publique sécurisée (https)
     *   - "typeMessage" : IMAGE | VIDEO | AUDIO | VOCAL | FICHIER
     */
    public Map<String, String> upload(MultipartFile file) throws IOException {

        String mimeType     = file.getContentType() != null ? file.getContentType() : "";
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String typeMessage  = detectTypeMessage(mimeType, originalName);
        String resourceType = toCloudinaryResourceType(typeMessage);

        // Options d'upload
        Map<?, ?> options = ObjectUtils.asMap(
            "folder",          folder,
            "resource_type",   resourceType,
            "use_filename",    true,
            "unique_filename", true,
            "overwrite",       false
        );

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);

        // Cloudinary retourne "secure_url" en HTTPS
        String url = (String) result.get("secure_url");
        if (url == null || url.isBlank()) {
            throw new IOException("Cloudinary n'a pas retourné d'URL pour le fichier : " + originalName);
        }

        log.info("✅ Cloudinary upload OK — type={} url={}", typeMessage, url);

        return Map.of(
            "url",         url,
            "typeMessage", typeMessage
        );
    }

    // ── Détection du type selon MIME + nom du fichier ────────────────────────
    // Les vocaux de l'app Angular arrivent en audio/webm ou audio/ogg
    // avec un nom qui commence par "vocal_"

    private String detectTypeMessage(String mimeType, String fileName) {
        if (mimeType.equals("audio/webm") ||
            mimeType.equals("audio/ogg")  ||
            fileName.startsWith("vocal_") ||
            fileName.endsWith(".webm"))       return "VOCAL";

        if (mimeType.startsWith("image/"))    return "IMAGE";
        if (mimeType.startsWith("video/"))    return "VIDEO";
        if (mimeType.startsWith("audio/"))    return "AUDIO";
        return "FICHIER";
    }

    // ── Mapping typeMessage → resource_type Cloudinary ──────────────────────
    // Cloudinary n'accepte que : "image", "video", "raw"
    // "video" gère aussi l'audio et les vocaux (mp3, ogg, wav, webm…)

    private String toCloudinaryResourceType(String typeMessage) {
        return switch (typeMessage) {
            case "IMAGE"                    -> "image";
            case "VIDEO", "AUDIO", "VOCAL" -> "video";
            default                         -> "raw";
        };
    }
}