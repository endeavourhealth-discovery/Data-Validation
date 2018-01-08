package org.endeavourhealth.datavalidation.dal;

import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.datavalidation.models.ResourceType;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.UUID;

public interface ResourceDAL {
    List<ResourceType> getResourceTypes();
    List<ResourceWrapper> getPatientResources(String serviceId, String systemId, String patientId, List<String> resourceTypes);
    Resource getResource(org.hl7.fhir.instance.model.ResourceType resourceType, String resourceId, String serviceId);
    List<UUID> getServiceSystems(String serviceId);
}
