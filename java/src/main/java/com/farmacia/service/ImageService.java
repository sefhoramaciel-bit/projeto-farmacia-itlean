package com.farmacia.service;

import com.farmacia.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:5242880}")
    private long maxFileSize; // 5MB em bytes

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private static final int MIN_IMAGES = 1;
    private static final int MAX_IMAGES = 3;

    public List<String> uploadMedicamentoImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("É necessário pelo menos " + MIN_IMAGES + " imagem");
        }

        if (files.size() > MAX_IMAGES) {
            throw new BusinessException("Máximo de " + MAX_IMAGES + " imagens permitidas");
        }

        List<String> imageUrls = new ArrayList<>();

        try {
            // Cria diretório se não existir
            Path medicamentosDir = Paths.get(uploadDir, "medicamentos");
            Files.createDirectories(medicamentosDir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Pula arquivos vazios
                }

                validateImage(file);
                String filename = generateFilename(file.getOriginalFilename());
                Path targetLocation = medicamentosDir.resolve(filename);
                
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                
                // Retorna caminho relativo para acesso via URL
                String imageUrl = "/uploads/medicamentos/" + filename;
                imageUrls.add(imageUrl);
            }

            if (imageUrls.size() < MIN_IMAGES) {
                throw new BusinessException("É necessário pelo menos " + MIN_IMAGES + " imagem válida");
            }

        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar imagens: " + e.getMessage());
        }

        return imageUrls;
    }

    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                // Tenta deletar de medicamentos
                Path imagePath = Paths.get(uploadDir, "medicamentos", filename);
                if (!Files.exists(imagePath)) {
                    // Se não existir em medicamentos, tenta em avatares
                    imagePath = Paths.get(uploadDir, "avatars", filename);
                }
                Files.deleteIfExists(imagePath);
            }
        } catch (IOException e) {
            // Log do erro, mas não lança exceção para não quebrar o fluxo
            System.err.println("Erro ao deletar imagem: " + e.getMessage());
        }
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                deleteImage(imageUrl);
            }
        }
    }

    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de avatar não pode ser vazio");
        }

        try {
            // Cria diretório se não existir
            Path avataresDir = Paths.get(uploadDir, "avatars");
            Files.createDirectories(avataresDir);

            validateImage(file);
            String filename = generateFilename(file.getOriginalFilename());
            Path targetLocation = avataresDir.resolve(filename);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Retorna caminho relativo para acesso via URL
            return "/uploads/avatars/" + filename;

        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar avatar: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        // Valida tamanho
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Valida extensão
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("Nome do arquivo inválido");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Formato de arquivo não permitido. Formatos aceitos: JPG, PNG, WebP");
        }

        // Valida tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("image/jpeg") && 
             !contentType.equals("image/jpg") && 
             !contentType.equals("image/png") && 
             !contentType.equals("image/webp"))) {
            throw new BusinessException("Tipo de arquivo não permitido. Apenas imagens JPG, PNG ou WebP são aceitas");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String generateFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }
}

