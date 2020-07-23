package org.endeavourhealth.dataassurance.logic.mocks;

import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.dataassurance.dal.ResourceDAL;
import org.endeavourhealth.dataassurance.models.ResourceType;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mock_ResourceDAL implements ResourceDAL {
    public Resource resource;

    @Override
    public List<ResourceType> getResourceTypes() {
        return null;
    }

    @Override
    public List<ResourceWrapper> getPatientResources(String serviceId, String systemId, String patientId, List<String> resourceTypes) {
        List<ResourceWrapper> resources = new ArrayList<>();

        ResourceWrapper resourceWrapper = new ResourceWrapper();
        resourceWrapper.setServiceId(UUID.fromString(serviceId));
        resourceWrapper.setPatientId(UUID.fromString(patientId));
        resourceWrapper.setResourceData("{ \"resourceType\": \"Condition\" }");

        resources.add(resourceWrapper);

        return resources;
    }

    @Override
    public ResourceWrapper getWrappedResource(org.hl7.fhir.instance.model.ResourceType resourceType, String resourceId, String serviceId) {
        return null;
    }

    @Override
    public Resource getResource(org.hl7.fhir.instance.model.ResourceType resourceType, String resourceId, String serviceId) {
        return resource;
    }
}
