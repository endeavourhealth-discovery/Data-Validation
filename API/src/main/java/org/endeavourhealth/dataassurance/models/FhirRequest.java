package org.endeavourhealth.dataassurance.models;

import org.hl7.fhir.instance.model.Bundle;

import java.util.List;

public class FhirRequest {
    private Bundle patients;
    private List<String> resources;

    public Bundle getPatients() {
        return patients;
    }

    public FhirRequest setPatients(Bundle patients) {
        this.patients = patients;
        return this;
    }

    public List<String> getResources() {
        return resources;
    }

    public FhirRequest setResources(List<String> resources) {
        this.resources = resources;
        return this;
    }
}
