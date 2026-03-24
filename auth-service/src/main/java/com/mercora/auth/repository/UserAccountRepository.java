package com.mercora.auth.repository;

import com.mercora.auth.model.UserAccount;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
