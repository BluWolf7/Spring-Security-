package com.bluwolf.springsecurityclient.service;

import com.bluwolf.springsecurityclient.entity.PasswordResetToken;
import com.bluwolf.springsecurityclient.entity.User;
import com.bluwolf.springsecurityclient.entity.VerificationToken;
import com.bluwolf.springsecurityclient.model.UserModel;
import com.bluwolf.springsecurityclient.repository.PasswordResetTokenRepository;
import com.bluwolf.springsecurityclient.repository.UserRepository;
import com.bluwolf.springsecurityclient.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserModel userModel) {

        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));


        userRepository.save(user);
        return user;

    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user, token);

        verificationTokenRepository.save(verificationToken);

    }

    @Override
    public String validateVerificationToken(String token) {

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return "invalid";
        }
        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        //If time in db - current time is less than 0 then it means token expired
        // example if time in db is 6:18 and now time is  6:15 then no problem 18-15 =3 !<=0
        // other scenario: time in db is 6:18 and now time is  6:19 then 18-19 = -1 <=0 so expired
        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            //token is expired no need to keep in db
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }
        //If control reaches here then user is valid and has token that has not been expired. So we can set user to enabled

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";

    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        //We check if old token exists if yes then replace it with new token and send it back to controller
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);



    }

    @Override
    public String validatePasswordResetToken(String token) {

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null) {
            return "invalid";
        }
         Calendar cal = Calendar.getInstance();
        //If time in db - current time is less than 0 then it means token expired
        // example if time in db is 6:18 and now time is  6:15 then no problem 18-15 =3 !<=0
        // other scenario: time in db is 6:18 and now time is  6:19 then 18-19 = -1 <=0 so expired
        if (passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            //token is expired no need to keep in db
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword,user.getPassword());
    }
}
