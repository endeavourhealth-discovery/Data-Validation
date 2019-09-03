package org.endeavourhealth.dataassurance.helpers;

import com.google.common.base.Strings;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.coreui.endpoints.UserManagerEndpoint;
import org.keycloak.representations.AccessToken;

import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Security {

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

        try {
            //if it's a UUID, then just use it as is
            serviceUuid = UUID.fromString(keyCloakOrgId);

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
        }

        return serviceUuid;
    }

    public Set<String> getPublishingOrganisationIdsFromProject(String projectId) throws Exception {
        UserManagerEndpoint um = new UserManagerEndpoint();
        List<String> orgUuids = um.getPublishersForProject(projectId);

        return new HashSet<>(orgUuids);

    }
}
