package com.farmacia.config;

import com.farmacia.domain.entity.Usuario;
import com.farmacia.domain.enums.Role;
import com.farmacia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            criarAdmin();
        }
    }

    private void criarAdmin() {
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail("admin@farmacia.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setAvatarUrl(null);
        
        usuarioRepository.save(admin);
        System.out.println("Usuário admin criado: admin@farmacia.com / admin123");
    }
}








