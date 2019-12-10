package org.endeavourhealth.dataassurance.dal;

import org.endeavourhealth.dataassurance.models.Patient;
import org.endeavourhealth.dataassurance.models.Person;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface PersonPatientDAL {
    List<Person> searchByNhsNumber(Set<String> serviceIds, String nhsNumber) throws Exception;
    List<Person> searchByLocalId(Set<String> serviceIds, String emisNumber) throws Exception;
    List<Person> searchByDateOfBirth(Set<String> serviceIds, Date dateOfBirth) throws Exception;
    List<Person> searchByNames(Set<String> serviceIds, List<String> names) throws Exception;

    List<Patient> getPatientsByNhsNumber(Set<String> serviceIds, String nhsNumber) throws Exception;
    Patient getPatient(String serviceId, String systemId, String patientId) throws Exception;
}
