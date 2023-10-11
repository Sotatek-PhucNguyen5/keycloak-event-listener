package org.cardanofoundation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.cardanofoundation.constant.CommonConstant;
import org.cardanofoundation.model.EventModel;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

@JBossLog
public class KeycloakEventListener implements EventListenerProvider {

  private final KeycloakSession keycloakSession;

  public KeycloakEventListener(KeycloakSession keycloakSession) {
    this.keycloakSession = keycloakSession;
  }

  @Override
  public void onEvent(Event event) {
    log.info("Event: " + event.getType().name());
  }

  @Override
  public void onEvent(AdminEvent adminEvent, boolean b) {
    log.info("Admin Event: " + adminEvent.getResourceType().name());
    if (adminEvent.getResourceType().equals(ResourceType.REALM_ROLE_MAPPING) || (
        adminEvent.getResourceType().equals(ResourceType.REALM_ROLE)
            && adminEvent.getOperationType().equals(
            OperationType.DELETE))) {
      RealmModel realmModel = this.keycloakSession.getContext().getRealm();
      Set<String> domains = new HashSet<>();
      this.keycloakSession.clients().getClientsStream(realmModel)
          .forEach(clientModel -> clientModel.getWebOrigins().forEach(mappingUrl -> {
            if (Objects.nonNull(mappingUrl) && mappingUrl.startsWith(
                CommonConstant.PREFIX_HTTP_URL)) {
              domains.add(mappingUrl);
            }
          }));
      EventModel model = new EventModel();
      model.setResourcePath(adminEvent.getResourcePath());
      model.setResourceType(adminEvent.getResourceType().name());
      KeycloakEvent.push(model, domains);
    }
  }

  @Override
  public void close() {
    log.info("Close");
  }
}
