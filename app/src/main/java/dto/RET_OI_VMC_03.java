package dto;

/**
 * Created by njoy on 2015. 9. 23..
 */
public class RET_OI_VMC_03 {
    public static String SECURE_TOKEN;
    public static String CRYPTO_KEY;

    public static String getSecureToken() {
        return SECURE_TOKEN;
    }

    public static void setSecureToken(String secureToken) {
        SECURE_TOKEN = secureToken;
    }

    public static String getCryptoKey() {
        return CRYPTO_KEY;
    }

    public static void setCryptoKey(String cryptoKey) {
        CRYPTO_KEY = cryptoKey;
    }
}
