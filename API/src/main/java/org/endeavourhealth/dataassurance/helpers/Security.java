package org.endeavourhealth.dataassurance.helpers;

import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.endpoints.UserManagerEndpoint;
import org.keycloak.representations.AccessToken;

import javax.ws.rs.core.SecurityContext;
import java.util.*;

public class Security {
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
                String orgGroupOrganisationId = (String) orgGroup.getOrDefault("organisationId", null);
                if (orgGroupOrganisationId != null)
                    orgs.add(orgGroupOrganisationId);
            }
        }

        return orgs;
    }

    public Set<String> getPublishingOrganisationIdsFromProject(String projectId) throws Exception {
        UserManagerEndpoint um = new UserManagerEndpoint();
        List<String> orgUuids = um.getPublishersForProject(projectId);

        return new HashSet<String>(orgUuids);

    }
}
