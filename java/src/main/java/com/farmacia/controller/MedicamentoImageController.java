package com.farmacia.controller;

import com.farmacia.domain.dto.MedicamentoResponse;
import com.farmacia.service.ImageService;
import com.farmacia.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Imagens de Medicamentos", description = "Endpoints para upload de imagens de medicamentos")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class MedicamentoImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private MedicamentoService medicamentoService;

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload de imagens do medicamento", 
        description = "Faz upload de imagens para um medicamento (1 a 3 imagens, JPG/PNG/WebP, máx 5MB cada). Use o botão 'Choose Files' para selecionar as imagens."
    )
    public ResponseEntity<MedicamentoResponse> uploadImagens(
            @Parameter(description = "ID do medicamento") @PathVariable UUID id,
            @Parameter(description = "Arquivos de imagem (1 a 3 arquivos, máx 5MB cada). Use o botão 'Choose Files' para selecionar.", required = true)
            @RequestParam("files") List<MultipartFile> files) {
        
        MedicamentoResponse medicamento = medicamentoService.uploadImagens(id, files);
        return ResponseEntity.status(HttpStatus.OK).body(medicamento);
    }

    @DeleteMapping("/{id}/imagens")
    @Operation(summary = "Remover imagens do medicamento", 
               description = "Remove todas as imagens de um medicamento")
    public ResponseEntity<MedicamentoResponse> removerImagens(@PathVariable UUID id) {
        MedicamentoResponse medicamento = medicamentoService.removerImagens(id);
        return ResponseEntity.ok(medicamento);
    }
}

