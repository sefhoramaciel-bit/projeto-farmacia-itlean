package com.farmacia.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final String DATETIME_FORMAT_SHORT = "dd/MM/yyyy HH:mm";

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        SimpleModule customModule = new SimpleModule();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        DateTimeFormatter dateTimeFormatterShort = DateTimeFormatter.ofPattern(DATETIME_FORMAT_SHORT);
        
        // Serializers - sempre usa formato brasileiro
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        
        // Deserializers customizados - aceita formato brasileiro e ISO
        customModule.addDeserializer(LocalDate.class, new StdDeserializer<LocalDate>(LocalDate.class) {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateStr = p.getText();
                if (dateStr == null || dateStr.trim().isEmpty()) {
                    return null;
                }
                
                // Tenta formato brasileiro (dd/MM/yyyy) - verifica se contém /
                if (dateStr.contains("/")) {
                    try {
                        return LocalDate.parse(dateStr, dateFormatter);
                    } catch (DateTimeParseException e) {
                        // Continua para tentar formato ISO se falhar
                    }
                }
                
                // Tenta formato ISO (yyyy-MM-dd)
                try {
                    return LocalDate.parse(dateStr);
                } catch (DateTimeParseException e) {
                    throw new IOException("Não foi possível deserializar a data '" + dateStr + 
                        "'. Formatos aceitos: dd/MM/yyyy ou yyyy-MM-dd", e);
                }
            }
        });
        
        customModule.addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateTimeStr = p.getText();
                if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                    return null;
                }
                
                // Tenta formato brasileiro completo (dd/MM/yyyy HH:mm:ss) - verifica se contém /
                if (dateTimeStr.contains("/")) {
                    try {
                        return LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
                    } catch (DateTimeParseException e1) {
                        try {
                            // Tenta formato brasileiro sem segundos (dd/MM/yyyy HH:mm)
                            return LocalDateTime.parse(dateTimeStr, dateTimeFormatterShort);
                        } catch (DateTimeParseException e2) {
                            // Continua para tentar formato ISO
                        }
                    }
                }
                
                // Tenta formato ISO (yyyy-MM-ddTHH:mm:ss ou yyyy-MM-ddTHH:mm)
                try {
                    return LocalDateTime.parse(dateTimeStr);
                } catch (DateTimeParseException e) {
                    throw new IOException("Não foi possível deserializar a data/hora '" + dateTimeStr + 
                        "'. Formatos aceitos: dd/MM/yyyy HH:mm:ss, dd/MM/yyyy HH:mm ou yyyy-MM-ddTHH:mm:ss", e);
                }
            }
        });
        
        return builder
                .modules(javaTimeModule, customModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}

