package com.cardano_lms.server.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender(MailProperties props) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        if (props.getHost() != null) sender.setHost(props.getHost());
        if (props.getPort() != null) sender.setPort(props.getPort());
        if (props.getUsername() != null) sender.setUsername(props.getUsername());
        if (props.getPassword() != null) sender.setPassword(props.getPassword());
        if (props.getProtocol() != null) sender.setProtocol(props.getProtocol());
        if (props.getDefaultEncoding() != null) sender.setDefaultEncoding(props.getDefaultEncoding().name());

        java.util.Properties javaMailProps = new java.util.Properties();
        if (props.getProperties() != null) {
            props.getProperties().forEach(javaMailProps::put);
        }
        sender.setJavaMailProperties(javaMailProps);
        return sender;
    }
}
