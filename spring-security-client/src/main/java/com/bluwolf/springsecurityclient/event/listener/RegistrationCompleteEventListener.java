package com.bluwolf.springsecurityclient.event.listener;

import com.bluwolf.springsecurityclient.entity.User;
import com.bluwolf.springsecurityclient.event.RegistrationCompleteEvent;
import com.bluwolf.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;


    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //Create the verification token for the user with the link

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        userService.saveVerificationTokenForUser(token,user);

        //Send Mail to user
        String url =
                event.getApplicationUrl()
                        + "/verifyRegistration?token="
                        + token;

        //Here we are mimicking an email sent by printing email in console
        //Ideally u can use an email client to send an email instead
        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }
}
