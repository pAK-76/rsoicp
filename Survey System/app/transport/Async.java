package transport;

import transport.Signer;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import org.codehaus.jackson.JsonNode;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static play.libs.Json.toJson;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 22:17
 * To change this template use File | Settings | File Templates.
 */
public class Async {
    String _to, _theme;
    Map<String, Object> _params;
    Boolean _shouldSign;

    public Async(String theme, String to, Map<String, Object> params, Boolean shouldSign) {
        _to = to;
        _theme = theme;
        _params = new TreeMap<>(params);
        _shouldSign = shouldSign;
    }

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

     public Boolean send() {
         String from = play.Play.application().configuration().getString("smtp.user");
         MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
         mail.setSubject(_theme);
         //mail.addRecipient(play.Play.application().configuration().getString("supervising.email"));
         mail.addRecipient(_to);
         mail.addFrom(from);

         _params.put("author", from);
         if (_shouldSign) {
            _params.put("time", String.format("%d", System.currentTimeMillis() / 1000L));
            _params.put("signature", Signer.signature(Async.stringToSign(_params)));
         }

         JsonNode jsonNode = toJson(_params);
         String jsonString = jsonNode.toString();
         mail.send(jsonString);
         return true;
     }

}
