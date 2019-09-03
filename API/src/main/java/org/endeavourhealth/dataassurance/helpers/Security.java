package org.endeavourhealth.dataassurance.helpers;

import com.google.common.base.Strings;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.coreui.endpoints.UserManagerEndpoint;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Security {
    private static final Logger LOG = LoggerFactory.getLogger(Security.class);

    private static Map<String, UUID> hmOdsCodeToUuidMap = new ConcurrentHashMap<>();

    public Set<String> getUserAllowedOrganisationIdsFromSecurityContext(SecurityContext securityContext, String projectId) {
        if (projectId != null) {
            try {
                return getPublishingOrganisationIdsFromProject(projectId);
            } catch (Exception e) {
                // fall back to original method
            }
        }

        Set<String> orgs = new HashSet<>();

        AccessToken accessToken = SecurityUtils.getToken(securityContext);
        List<Map<String, Object>> orgGroups = (List)accessToken.getOtherClaims().getOrDefault("orgGroups", null);

        if (orgGroups != null) {
            for (Object orgGroup1 : orgGroups) {
                Map<String, Object> orgGroup = (Map) orgGroup1;

                String orgGroupOrganisationId = (String)orgGroup.getOrDefault("organisationId", null);
                UUID uuid = findServiceIdForKeyCloakValue(orgGroupOrganisationId);
                if (uuid != null) {
                    orgs.add(uuid.toString());
                }
            }
        }

        return orgs;
    }

    private UUID findServiceIdForKeyCloakValue(String keyCloakOrgId) {

        if (Strings.isNullOrEmpty(keyCloakOrgId)) {
            return null;
        }

        UUID serviceUuid = null;
        //LOG.trace("Getting service UUID for ID [" + keyCloakOrgId + "]");

        try {
            //if it's a UUID, then just use it as is
            serviceUuid = UUID.fromString(keyCloakOrgId);
            //LOG.trace("Is UUID, so OK");

        } catch (Exception ex) {

            //if not a UUID then treat as an ODS code and look up via the DB
            serviceUuid = hmOdsCodeToUuidMap.get(keyCloakOrgId);
            if (serviceUuid == null) {
                ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                try {
                    Service service = serviceDal.getByLocalIdentifier(keyCloakOrgId);
                    if (service != null) {
                        serviceUuid = service.getId();
                        hmOdsCodeToUuidMap.put(keyCloakOrgId, serviceUuid);
                    }
                } catch (Exception ex2) {
                    throw new RuntimeException("Failed to find service for ODS code " + keyCloakOrgId, ex2);
                }
            }
            //LOG.trace("Is ODS code, and found UUID " + serviceUuid);
        }

        return serviceUuid;
    }

    public Set<String> getPublishingOrganisationIdsFromProject(String projectId) throws Exception {
        UserManagerEndpoint um = new UserManagerEndpoint();
        List<String> orgUuids = um.getPublishersForProject(projectId);

        return new HashSet<>(orgUuids);

    }
}
