package org.endeavourhealth.dataassurance.logic;

import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.dataassurance.dal.ResourceDAL;
import org.endeavourhealth.dataassurance.dal.ResourceDAL_Cassandra;
import org.endeavourhealth.dataassurance.models.*;
import org.endeavourhealth.dataassurance.models.Patient;
import org.endeavourhealth.dataassurance.models.ResourceType;
import org.hl7.fhir.instance.model.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FHIRLogic {
    public List<ResourceType> getResourceTypes() {
        return new ResourceLogic().getTypes();
    }

    public Bundle getPatientsByNHSNumber(String nhsNumber, Set<String> allowedOrganisations) throws Exception {
        List<Patient> patients = new PersonPatientLogic().getPatientsForPerson(allowedOrganisations, nhsNumber);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        // Reconstruct into list of FHIR patient resources
        Bundle result = new Bundle();

        for(Patient patient: patients) {
            org.hl7.fhir.instance.model.Patient fhirPatient = new org.hl7.fhir.instance.model.Patient()
                .addName(new HumanName().setText(patient.getPatientName()))
                .setManagingOrganization(new Reference().setReference("Organization/" + patient.getId().getServiceId()));

            if (patient.getDob() != null && !patient.getDob().isEmpty())
                fhirPatient.setBirthDate(df.parse(patient.getDob()));

            fhirPatient.setId(patient.getId().getPatientId());

            for(Map.Entry<String, String> localId : patient.getLocalIds().entrySet()) {
                fhirPatient.addIdentifier(
                    new Identifier()
                    .setSystem(localId.getKey())
                    .setValue(localId.getValue())
                );
            }

            Bundle.BundleEntryComponent entry = result.addEntry();
            entry.setResource(fhirPatient);
        }

        return result;
    }

    public Bundle getPatientResources(Set<String> organisations, FhirRequest resourceRequest) throws Exception {
        ResourceDAL dal = new ResourceDAL_Cassandra();
        Bundle result = new Bundle();
        ParserPool pp = ParserPool.getInstance();

        for (Bundle.BundleEntryComponent requestEntry : resourceRequest.getPatients().getEntry()) {
            if (requestEntry.getResource() instanceof org.hl7.fhir.instance.model.Patient) {
                org.hl7.fhir.instance.model.Patient patient = (org.hl7.fhir.instance.model.Patient) requestEntry.getResource();
                String organisation = patient.getManagingOrganization().getReference();
                organisation = organisation.substring(organisation.indexOf('/') + 1);

                if (organisations.contains(organisation)) {
                    List<ResourceWrapper> resourceWrappers = dal.getPatientResources(
                        organisation,
                        null,
                        patient.getId(),
                        resourceRequest.getResources());

                    for(ResourceWrapper wrapper: resourceWrappers) {
                        Resource resource = pp.parse(wrapper.getResourceData());
                        Bundle.BundleEntryComponent resultEntry = result.addEntry();
                        resultEntry.setResource(resource);
                    }
                }
            }
        }
        return result;
    }
}
