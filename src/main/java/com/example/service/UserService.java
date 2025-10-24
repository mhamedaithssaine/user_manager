package com.example.service;

import com.example.dto.*;
import com.example.entity.User;
import com.example.exception.EmailAlreadyExistsException;
import com.example.exception.NotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponseDTO create(UserCreateDTO dto) {
        if (repository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyExistsException("Email already exists");

        User user = UserMapper.toEntity(dto);
        repository.save(user);
        return UserMapper.toDto(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public UserResponseDTO findById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toDto(user);
    }

    public UserResponseDTO update(Long id, UserUpdateDTO dto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setActive(dto.getActive());

        repository.save(user);
        return UserMapper.toDto(user);
    }

    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new NotFoundException("User not found");
        repository.deleteById(id);
    }
}
