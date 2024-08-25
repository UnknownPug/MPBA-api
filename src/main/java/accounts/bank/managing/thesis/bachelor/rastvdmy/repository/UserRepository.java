package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    User findByName(String receiverName);
}
