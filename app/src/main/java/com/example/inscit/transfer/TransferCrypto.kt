package com.example.inscit.transfer

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object TransferCrypto {
    // Increased from 10000 to slow down offline brute-force of weak pair codes.
    private const val PBKDF2_ITERATIONS = 60000
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_LENGTH = 16

    // Per-session random salt replaces the fixed static salt. Prevents precomputation
    // and rainbow-table attacks against the 6-digit pair code. Salt is transmitted
    // alongside the ciphertext (inside packEncrypted) and is not secret.
    private fun deriveKey(pairCode: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pairCode.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(bytes: ByteArray, pairCode: String): Triple<ByteArray, ByteArray, ByteArray> {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(pairCode, salt)
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv, 0, GCM_IV_LENGTH))
        val ciphertext = cipher.doFinal(bytes)
        return Triple(salt, iv, ciphertext)
    }

    fun decrypt(salt: ByteArray, iv: ByteArray, ciphertext: ByteArray, pairCode: String): ByteArray {
        val key = deriveKey(pairCode, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv, 0, iv.size))
        return cipher.doFinal(ciphertext)
    }

    // Pack salt + iv + ciphertext as single payload: [saltLen(1) | salt | ivLen(1) | iv | ciphertext]
    fun packEncrypted(salt: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val out = ByteArray(1 + salt.size + 1 + iv.size + ciphertext.size)
        out[0] = salt.size.toByte()
        System.arraycopy(salt, 0, out, 1, salt.size)
        out[1 + salt.size] = iv.size.toByte()
        System.arraycopy(iv, 0, out, 2 + salt.size, iv.size)
        System.arraycopy(ciphertext, 0, out, 2 + salt.size + iv.size, ciphertext.size)
        return out
    }

    fun unpackEncrypted(packed: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val saltLen = packed[0].toInt() and 0xFF
        val salt = packed.copyOfRange(1, 1 + saltLen)
        val ivLen = packed[1 + saltLen].toInt() and 0xFF
        val iv = packed.copyOfRange(2 + saltLen, 2 + saltLen + ivLen)
        val ct = packed.copyOfRange(2 + saltLen + ivLen, packed.size)
        return Triple(salt, iv, ct)
    }

    fun encryptPacked(bytes: ByteArray, pairCode: String): ByteArray {
        val (salt, iv, ct) = encrypt(bytes, pairCode)
        return packEncrypted(salt, iv, ct)
    }

    fun decryptPacked(packed: ByteArray, pairCode: String): ByteArray {
        val (salt, iv, ct) = unpackEncrypted(packed)
        return decrypt(salt, iv, ct, pairCode)
    }
}
