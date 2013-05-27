package transport;

import transport.Signer;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import org.codehaus.jackson.JsonNode;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static play.libs.Json.toJson;

/**
 * Created with IntelliJ IDEA.
 * User: pAK76
 * Date: 22.05.13
 * Time: 22:17
 * To change this template use File | Settings | File Templates.
 */
public class Async {
    String _to;
    Map<String, Object> _params;

    public Async(String to, Map<String, Object> params) {
        _to = to;
        _params = new HashMap<>(params);
    }

    public String stringToSign() {
        String result = "";
        Boolean first = true;
        for(String i : _params.keySet()) {
            if (first) {
                first = false;
            } else {
                result += "&";
            }
            result += i + "=" + _params.get(i);
        }
        System.out.println(result);
        return result;
    }

     public Boolean send() {
         String from = play.Play.application().configuration().getString("smtp.user");
         MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
         mail.setSubject("Review");
         //mail.addRecipient(play.Play.application().configuration().getString("supervising.email"));
         mail.addRecipient(_to);
         mail.addFrom(from);

         _params.put("author", from);
         _params.put("time", String.format("%d", System.currentTimeMillis() / 1000L));
         _params.put("signature", Signer.signature(this.stringToSign()));

         JsonNode jsonNode = toJson(_params);
         String jsonString = jsonNode.toString();
         mail.send(jsonString);
         return true;
     }

}
