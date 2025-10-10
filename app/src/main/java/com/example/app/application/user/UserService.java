package com.example.app.application.user;

import com.example.app.domain.user.User;
import com.example.app.domain.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ユーザー操作のユースケースをまとめたサービスクラス。 */
@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  public Optional<User> findById(String auth0Id) {
    return userRepository.findById(auth0Id);
  }

  @Transactional
  public User create(String auth0Id) {
    return createInternal(auth0Id);
  }

  @Transactional
  public Optional<User> update(String auth0Id) {
    return userRepository
        .findById(auth0Id)
        .map(existing -> new User(auth0Id, existing.createdAt(), Instant.now()))
        .map(userRepository::save);
  }

  @Transactional
  public User findOrCreate(String auth0Id) {
    return userRepository.findById(auth0Id).orElseGet(() -> createInternal(auth0Id));
  }

  private User createInternal(String auth0Id) {
    Instant now = Instant.now();
    User user = new User(auth0Id, now, now);
    return userRepository.save(user);
  }

  @Transactional
  public void delete(String auth0Id) {
    userRepository.deleteById(auth0Id);
  }
}
