    package com.ieumsae.user.repository;

    import com.ieumsae.user.domain.User;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;


    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findById(Long userIdx);
        Optional<User> findByUserId(String userId);
        Optional<User> findByUserNickName(String userNickName);
        Optional<User> findByUserEmail(String userEmail);
        Optional<User> findByUserName(String userName);

    }
