# Spring-Security-

https://www.youtube.com/watch?v=zvR-Oif_nxg&t=1s

1) 8:54 : In Oauth-authorization-server in AuthorizationServerConfig : I have not added auth-server to my local setup via terminal so I only have localhost at 127.0.0.1 so at line 100, i have kept it as localhost:9000 instead.
2) 9:05 : In spring-security-client in application.yml : Same issue as above, have kept localhost at line 36
3) 9:15 : In Oauth-resource-server in  application.yml : Same issue as above, have kept localhost at line 9
4) 9:17 : In Oauth-resource-server in ResourceServerConfig : mvcMatchers not working (Seems deprecated) So i have commented out entire bean.

Let me come back to this after learning springSecurity better - Especially Oauth2.