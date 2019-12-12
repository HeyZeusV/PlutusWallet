package com.heyzeusv.plutuswallet.billingrepo

import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 *  Security-related methods. For a secure implementation, all of this code should be implemented on
 *  a server that communicates with the application on the device.
 */
object Security {

    private const val TAG                   = "IABUtil/Security"
    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM   = "SHA1withRSA"

    /**
     *  BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     *  (that you got from the Google Play developer console, usually under Services & APIs tab).
     *  This is not your developer public key, it's the *app-specific* public key.
     *
     *  Just like everything else in this class, this public key should be kept on your server.
     *  But if you don't have a server, then you should obfuscate your app so that hackers cannot
     *  get it. If you cannot afford a sophisticated obfuscator, instead of just storing the entire
     *  literal string here embedded in the program, construct the key at runtime from pieces or
     *  use bit manipulation (for example, XOR with some other string) to hide
     *  the actual key.  The key itself is not secret information, but we don't
     *  want to make it easy for an attacker to replace the public key with one
     *  of their own and then fake messages from the server.
     */

    const val BASE_64_ENCODED_PUBLIC_KEY : String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" +
            "jWtwCHDRjKG7SVBFkza3e362QKaogGDln5/gtOLN8AICnQFJn/BrbTa+l8lmOmBp3+qxJEYp5wtJ3Z9r4cTo" +
            "5Cpam/NpTSNCvLvzq2GX4GHLbDFLxn4meYpqDIP+aTB2vWB8BElJq4rTHAwBFw0Q/tdShtbLuHI8pzkx5EVO" +
            "wq9ZEGeQOoLy12e6+1QU6muKdeB2XCwu8/MiiAZ0J1mH43QOWzyRSzBcn211yzkaQbSjhjhZnGaztbx9dDax" +
            "fnDpwufxBKogDXKPVU/KcCPQWeG1q0vAo/KxzsE0wQn0/kl0UPdOmnPGqzM8oSbxOTzrxzfXCj3nHLlgaOo1" +
            "30kaxwIDAQAB"

    /**
     *  Verifies that the data was signed with the given signature
     *
     *  @param  base64PublicKey the base64-encoded public key to use for verifying.
     *  @param  signedData      the signed JSON string (signed, not encrypted)
     *  @param  signature       the signature for the data, signed with the private key
     *  @throws IOException     if encoding algorithm is not supported or key specification is invalid
     */
    @Throws(IOException::class)
    fun verifyPurchase(base64PublicKey : String, signedData : String, signature : String) : Boolean {

        if ((TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)
                    || TextUtils.isEmpty(signature))) {

            // Log.w(TAG, "Purchase verification failed: missing data.")
            return false
        }
        val key : PublicKey = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)
    }

    /**
     *  Generates a PublicKey instance from a string containing the Base64-encoded public key.
     *
     *  @param encodedPublicKey Base64-encoded public key
     *  @throws IOException if encoding algorithm is not supported or key specification is invalid
     */
    @Throws(IOException::class)
    private fun generatePublicKey(encodedPublicKey : String) : PublicKey {

        try {

            val decodedKey : ByteArray  = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory : KeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            return keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {

            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {

            val msg = "Invalid key specification: $e"
            // Log.w(TAG, msg)
            throw IOException(msg)
        }
    }

    /**
     *  Verifies that the signature from the server matches the computed signature on the data.
     *  Returns true if the data is correctly signed.
     *
     *  @param publicKey  public key associated with the developer account
     *  @param signedData signed data from server
     *  @param signature  server signature
     *  @return true if the data and signature match
     */
    private fun verify(publicKey : PublicKey, signedData : String, signature : String) : Boolean {

        val signatureBytes : ByteArray
        try {

            signatureBytes = Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {

            // Log.w(TAG, "Base64 decoding failed.")
            return false
        }
        try {

            val signatureAlgorithm : Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes)) {

                // Log.w(TAG, "Signature verification failed...")
                return false
            }
            return true
        } catch (e: NoSuchAlgorithmException) {

            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {

            // Log.w(TAG, "Invalid key specification.")
        } catch (e: SignatureException) {

            // Log.w(TAG, "Signature exception.")
        }
        return false
    }
}