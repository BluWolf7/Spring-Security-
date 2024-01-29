package com.bluwolf.springsecurityclient.repository;

import com.bluwolf.springsecurityclient.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {


    PasswordResetToken findByToken(String token);
}
