package com.proselyteapi.dataprovider.security;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

@Component
public class PBKDF2Encoder implements PasswordEncoder {

    @Value("${jwt.token.password.encoder.secret}")
    private String secret;

    @Value("${jwt.token.password.encoder.iteration}")
    private Integer iteration;

    @Value("${jwt.token.password.encoder.keylength}")
    private Integer keylength;

    private static final String SECRET_KEY_INSTANCE = "PBKDF2WithHmacSHA512";
    @Override
    @SneakyThrows
    public String encode(CharSequence charSequence) {
        byte[] result = SecretKeyFactory.getInstance(SECRET_KEY_INSTANCE)
                .generateSecret(new PBEKeySpec(charSequence.toString().toCharArray(),
                    secret.getBytes(), iteration, keylength))
                .getEncoded();
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    public boolean matches(CharSequence charSequence, String password) {
        return encode(charSequence).equals(password);
    }
}