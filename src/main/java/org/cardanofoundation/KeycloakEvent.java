package org.cardanofoundation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.cardanofoundation.constant.CommonConstant;
import org.cardanofoundation.model.EventModel;

@JBossLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeycloakEvent {

  public static void push(EventModel model, Set<String> domains) {
    domains.forEach(domain -> {
      try {
        String roleMappingUrl = domain + CommonConstant.ROLE_MAPPING_API;
        log.info("Starting push status user to url: " + roleMappingUrl);
        URL url = new URL(roleMappingUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        ObjectMapper mapper = new ObjectMapper();
        String modelStr = mapper.writeValueAsString(model);
        OutputStream os = conn.getOutputStream();
        os.write(modelStr.getBytes());
        os.flush();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
          log.error("Failed : HTTP error code : " + conn.getResponseMessage());
          throw new RuntimeException("Failed : HTTP error code : "
              + conn.getResponseCode());
        }
        conn.disconnect();
      } catch (IOException | RuntimeException e) {
        log.error("Error: " + e.getMessage());
      }
    });
  }
}
