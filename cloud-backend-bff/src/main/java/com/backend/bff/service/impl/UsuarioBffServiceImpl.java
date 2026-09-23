// src/main/java/com/backend/bff/service/impl/UsuarioBffServiceImpl.java
package com.backend.bff.service.impl;

import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.*;
import com.backend.bff.service.UsuarioBffService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioBffServiceImpl implements UsuarioBffService {

    private final UsuarioClient usuarioClient;

    public UsuarioBffServiceImpl(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    @Override
    public UsuarioDTO loginSSO(SsoLoginRequestDTO dto) {
        return usuarioClient.login(dto);
    }

    @Override
    public UsuarioDTO registrarUsuario(UsuarioRequestDTO dto) {
        return usuarioClient.crear(dto);
    }

    @Override
    public UsuarioDTO obtenerPerfil(Long id) {
        return usuarioClient.obtenerPorId(id);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioClient.listar();
    }

    @Override
    public UsuarioDTO actualizarPerfil(Long id, UsuarioUpdateDTO dto) {
        return usuarioClient.actualizar(id, dto);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioClient.eliminar(id);
    }

    @Override
    public UsuarioDTO crearUsuarioAdmin(UsuarioRequestDTO dto) {
        return usuarioClient.crearPorAdmin(dto);
    }

    @Override
    public UsuarioDTO actualizarUsuarioAdmin(Long id, UsuarioUpdateDTO dto) {
        return usuarioClient.actualizarPorAdmin(id, dto);
    }
}