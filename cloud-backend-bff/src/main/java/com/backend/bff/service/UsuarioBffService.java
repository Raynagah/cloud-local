// src/main/java/com/backend/bff/service/UsuarioBffService.java
package com.backend.bff.service;

import com.backend.bff.dto.*;
import java.util.List;

public interface UsuarioBffService {
    UsuarioDTO loginSSO(SsoLoginRequestDTO dto);
    UsuarioDTO registrarUsuario(UsuarioRequestDTO dto);
    UsuarioDTO obtenerPerfil(Long id);
    List<UsuarioDTO> listarUsuarios();
    UsuarioDTO actualizarPerfil(Long id, UsuarioUpdateDTO dto);
    void eliminarUsuario(Long id);
    
    // Operaciones Admin
    UsuarioDTO crearUsuarioAdmin(UsuarioRequestDTO dto);
    UsuarioDTO actualizarUsuarioAdmin(Long id, UsuarioUpdateDTO dto);
}