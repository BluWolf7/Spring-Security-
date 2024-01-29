package com.bluwolf.springsecurityclient.controller;

import com.bluwolf.springsecurityclient.entity.User;
import com.bluwolf.springsecurityclient.entity.VerificationToken;
import com.bluwolf.springsecurityclient.event.RegistrationCompleteEvent;
import com.bluwolf.springsecurityclient.model.PasswordModel;
import com.bluwolf.springsecurityclient.model.UserModel;
import com.bluwolf.springsecurityclient.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;


    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {

        User user = userService.registerUser(userModel);
        //Now we will create an event that sends an email with a token to the user and user upon clicking it will be sent to verification
        publisher.publishEvent(new RegistrationCompleteEvent(
                user, applicationUrl(request)
        ));
        return "Success";

    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {

        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully";
        }
        return "Bad User";

    }

    //What if user registers, but didn't receive email due to some issue ?
    // Note we are assuming token is saved to db but did not get sent to user ; so we provide a resend option
    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {

        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();

        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";

    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){
        //User will pass the email id ; we will validate if that email is present in our system
        //if yes then we will generate a passwordChange token and send it as a link to user
        //with this link user will be able to change the password

        User user = userService.findUserByEmail(passwordModel.getEmail());
        String url  = "";
        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request),token);
        }
        return url;

    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel){

        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(),passwordModel.getNewPassword());
            return "Password Reset Successfully";
        }else{
            return "Invalid Token";
        }

    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        //We need to get email and old pw from user, verify if user exists by email and then check if old pw is correct
        //if yes then we allow user to change the password to new password

        User user = userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())){
                return "Invalid Old Password";
        }

        //Save New Password
        userService.changePassword(user,passwordModel.getNewPassword());

        return "Password Changed Successfully";


    }


    private String passwordResetTokenMail(User user, String applicationUrl, String token) {

        //Send Mail to user
        String url =
                applicationUrl +
                        "/savePassword?token=" +
                        token;




        //Here we are mimicking an email sent by printing email in console
        //Ideally u can use an email client to send an email instead
        //sendVerificationEmail()
        log.info("Click the link to reset your password: {}",
                url);
        return url;

    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        //Send Mail to user
        String url =
                applicationUrl +
                        "/verifyRegistration?token=" +
                        verificationToken.getToken();


        //Here we are mimicking an email sent by printing email in console
        //Ideally u can use an email client to send an email instead
        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);

    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }


}
