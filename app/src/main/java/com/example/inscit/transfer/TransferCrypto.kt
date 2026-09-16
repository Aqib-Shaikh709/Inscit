package com.example.inscit.transfer

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object TransferCrypto {
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    // Fixed salt for deterministic key from pairCode - ephemeral only, not for long-term storage
    private val SALT = "InscitGBALinkCable2026".toByteArray(Charsets.UTF_8)

    private fun deriveKey(pairCode: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pairCode.toCharArray(), SALT, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(bytes: ByteArray, pairCode: String): Pair<ByteArray, ByteArray> {
        val key = deriveKey(pairCode)
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv, 0, GCM_IV_LENGTH))
        val ciphertext = cipher.doFinal(bytes)
        return iv to ciphertext
    }

    fun decrypt(iv: ByteArray, ciphertext: ByteArray, pairCode: String): ByteArray {
        val key = deriveKey(pairCode)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv, 0, iv.size))
        return cipher.doFinal(ciphertext)
    }

    // Helper to pack iv + ciphertext as single payload: [ivLen(1) | iv | ciphertext]
    fun packEncrypted(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val out = ByteArray(1 + iv.size + ciphertext.size)
        out[0] = iv.size.toByte()
        System.arraycopy(iv, 0, out, 1, iv.size)
        System.arraycopy(ciphertext, 0, out, 1 + iv.size, ciphertext.size)
        return out
    }

    fun unpackEncrypted(packed: ByteArray): Pair<ByteArray, ByteArray> {
        val ivLen = packed[0].toInt() and 0xFF
        val iv = packed.copyOfRange(1, 1 + ivLen)
        val ct = packed.copyOfRange(1 + ivLen, packed.size)
        return iv to ct
    }

    fun encryptPacked(bytes: ByteArray, pairCode: String): ByteArray {
        val (iv, ct) = encrypt(bytes, pairCode)
        return packEncrypted(iv, ct)
    }

    fun decryptPacked(packed: ByteArray, pairCode: String): ByteArray {
        val (iv, ct) = unpackEncrypted(packed)
        return decrypt(iv, ct, pairCode)
    }
}
