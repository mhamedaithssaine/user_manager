package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService {
    @Autowired
    private final UserRepository repository;

    public UserService(UserRepository repository){
        this.repository = repository;
    }


    public User save(User user){
        return repository.save(user);
    }

    public User findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<User> findAll(){
        return repository.findAll();
    }

    public void deleteById(Long id){
        repository.deleteById(id);
    }
}
