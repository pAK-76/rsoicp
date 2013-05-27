package transport;

import com.ning.http.util.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
public class Signer {
    public static String stringToSign(Map<String, Object> params) {
        String result = "";
        Boolean first = true;
        for(String i : params.keySet()) {
            if (first) {
                first = false;
            } else {
                result += "&";
            }
            result += i + "=" + params.get(i);
        }
        System.out.println(result.length());
        return result;
    }
    public static String stringToSignEnc(Map<String, Object> params) {
        String result = "";
        try {
            Boolean first = true;
            for(String i : params.keySet()) {
                if (first) {
                    first = false;
                } else {
                    result += "&";
                }
                result += i + "=" + URLEncoder.encode(params.get(i).toString(), "UTF-8");
            }
            System.out.println(result.length());
        } catch (Exception ex) {

        }
        return result;
    }

    public static String signature(String stringToSign) {
        try {
            String key = play.Play.application().configuration().getString("hmac.key");
            System.out.println("KEY!!!: " + key);
            System.out.println("KEY LENGTH!!!: " + String.format("%d",key.length()));
            System.out.println("S2S: " + stringToSign);

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte [] hmacData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return Base64.encode(hmacData);
        }
        catch (Exception ex) {
            return null;
        }

    }


}
