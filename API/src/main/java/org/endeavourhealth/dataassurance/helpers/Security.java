package org.endeavourhealth.dataassurance.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.coreui.endpoints.UserManagerEndpoint;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Security {
    private static final Logger LOG = LoggerFactory.getLogger(Security.class);

    private static Map<String, UUID> hmOdsCodeToUuidMap = new ConcurrentHashMap<>();


    /**
     * returns the set of service UUIDs that this application is allowed to view. It uses three
     * inputs to determine the output:
     *
     * projectId ONLY - if specified, then the User Manager is used to look up that project and its members
     *
     * OR
     *
     * config record AND securityContext - if a "restrict-to-protocol" config record exists, then the protocol for this name is
     * found and its publishers determines the set of organisations, this is then intersected with the list of service
     * IDs from the security context
     *
     * OR
     *
     * securityContext ONLY - if none of the above apply, then the list of service IDs from the security context is used
     */
    public static Set<String> getAllowedServiceUuids(String projectId, SecurityContext securityContext) throws Exception {

        //if a project ID is supplied, use that first
        if (!Strings.isNullOrEmpty(projectId)) {
            try {
                return getPublishingOrganisationIdsFromProject(projectId);
            } catch (Exception e) {
                // fall back to original method
            }
        }

        Set<String> protocolServiceIds = getServiceIdsFromProtocol();
        Set<String> scServiceIds = getServiceIdsFromSecurityContext(securityContext);

        //if we have a list of protocol service IDs, then intersect with the security context list
        if (protocolServiceIds != null) {
            Set<String> ret = new HashSet<>(protocolServiceIds);
            ret.retainAll(scServiceIds);
            return ret;
        }

        //if none of the above, just use the security context
        return scServiceIds;
    }

    private static Set<String> getServiceIdsFromProtocol() throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("restrict-to-protocol");
        if (json == null) {
            return null;
        }

        String protocolIdStr = json.get("uuid").asText();
        UUID protocolId = UUID.fromString(protocolIdStr);
        LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(protocolId);
        if (libraryItem == null) {
            throw new Exception("Failed to find protocol for config JSON " + json);
        }

        Set<String> ret = new HashSet<>();

        Protocol protocol = libraryItem.getProtocol();
        for (ServiceContract serviceContract: protocol.getServiceContract()) {
            if (serviceContract.getType() != ServiceContractType.PUBLISHER
                    || serviceContract.getActive() != ServiceContractActive.TRUE) {
                continue;
            }

            String serviceId = serviceContract.getService().getUuid();
            ret.add(serviceId);
        }

        return ret;
    }

    private static UUID findServiceIdForKeyCloakValue(String keyCloakOrgId) {

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

    public static Set<String> getServiceIdsFromSecurityContext(SecurityContext securityContext) {

        Set<String> serviceIds = new HashSet<>();

        AccessToken accessToken = SecurityUtils.getToken(securityContext);
        List<Map<String, Object>> orgGroups = (List)accessToken.getOtherClaims().getOrDefault("orgGroups", null);

        if (orgGroups != null) {
            for (Object orgGroup1 : orgGroups) {
                Map<String, Object> orgGroup = (Map) orgGroup1;

                String orgGroupOrganisationId = (String)orgGroup.getOrDefault("organisationId", null);
                UUID uuid = findServiceIdForKeyCloakValue(orgGroupOrganisationId);
                if (uuid != null) {
                    serviceIds.add(uuid.toString());
                }
            }
        }

        return serviceIds;
    }

    private static Set<String> getPublishingOrganisationIdsFromProject(String projectId) throws Exception {
        UserManagerEndpoint um = new UserManagerEndpoint();
        List<String> orgUuids = um.getPublishersForProject(projectId);

        return new HashSet<>(orgUuids);
    }


    /**
     * returns the config name to use for the "enterprise-lite" DB to use for the Reports section of the Data Assurance
     * app
     */
    public static String getEnterpriseConfigName() throws Exception {
        JsonNode json = ConfigManager.getConfigurationAsJson("restrict-to-protocol");
        if (json == null) {
            throw new Exception("No config record found to specify database");
        }

        String protocolIdStr = json.get("uuid").asText();
        UUID protocolId = UUID.fromString(protocolIdStr);
        LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(protocolId);
        if (libraryItem == null) {
            throw new Exception("Failed to find protocol for config JSON " + json);
        }

        List<String> ret = new ArrayList<>();

        Protocol protocol = libraryItem.getProtocol();
        for (ServiceContract serviceContract: protocol.getServiceContract()) {

            if (serviceContract.getType() != ServiceContractType.SUBSCRIBER
                    || serviceContract.getActive() != ServiceContractActive.TRUE) {
                continue;
            }

            String endpoint = LibraryRepositoryHelper.getSubscriberEndpoint(serviceContract);
            ret.add(endpoint);
        }

        if (ret.size() > 1) {
            throw new Exception("More than one potential config record found " + ret);

        } else if (ret.isEmpty()) {
            throw new Exception("No potential subscriber/enterprise DB found");

        } else {
            return ret.get(0);
        }
    }
}
