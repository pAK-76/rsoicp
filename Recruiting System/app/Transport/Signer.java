package Transport;

import com.avaje.ebean.Expr;
import com.ning.http.util.Base64;
import models.Employer;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 23.05.13
 * Time: 23:41
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
    public static Integer checkSignature(Map<String, Object> params, String signature) {
        Integer result = -1;

        try {
            String email = (String)params.get("author");
            Employer employer = Employer.find.where(Expr.eq("email", email)).findUnique();

            String key = employer.secret;
            System.out.println("Key: " + key);
            System.out.println("Email: " + email);
            String stringToSign = Signer.stringToSign(params);
            System.out.println("Stringtosign: " + stringToSign);

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte [] hmacData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String shouldBe = Base64.encode(hmacData);
            System.out.println("Should be: " + shouldBe);
            System.out.println("Signature: " + signature);

            if (shouldBe.equals(signature)) {
                result = employer.id;
            }
        }
        catch (Exception ex) {
             System.out.println(ex);
        }
        return result;
    }
}
