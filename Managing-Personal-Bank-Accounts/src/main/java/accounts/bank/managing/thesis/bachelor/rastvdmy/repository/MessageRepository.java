package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}
