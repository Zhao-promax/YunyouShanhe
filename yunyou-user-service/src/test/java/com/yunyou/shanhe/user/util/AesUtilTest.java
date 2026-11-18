package com.yunyou.shanhe.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesUtilTest {

    private AesUtil aesUtil;

    @BeforeEach
    void setUp() throws Exception {
        aesUtil = new AesUtil("yunyou-shanhe-aes-secret-dev-only-change-in-prod-0123456789");
    }

    @Test
    void shouldEncryptAndDecryptRoundtrip() {
        String plaintext = "110101199001011234";
        String encrypted = aesUtil.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, aesUtil.decrypt(encrypted));
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintext() {
        String plaintext = "same-plaintext";
        assertNotEquals(aesUtil.encrypt(plaintext), aesUtil.encrypt(plaintext));
    }

    @Test
    void shouldThrowWhenDecryptInvalid() {
        assertThrows(IllegalStateException.class, () -> aesUtil.decrypt("!!!not-valid-base64!!!"));
    }
}
