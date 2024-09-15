package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
