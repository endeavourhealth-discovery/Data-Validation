package org.endeavourhealth.dataassurance.logic;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.dataassurance.dal.PersonPatientDAL;
import org.endeavourhealth.dataassurance.dal.PersonPatientDAL_Jdbc;
import org.endeavourhealth.dataassurance.helpers.SearchTermsParser;
import org.endeavourhealth.dataassurance.models.Patient;
import org.endeavourhealth.dataassurance.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PersonPatientLogic {
    private static final Logger LOG = LoggerFactory.getLogger(PersonPatientLogic.class);
    static PersonPatientDAL dal;

    public PersonPatientLogic() {
        if (dal == null)
         dal = new PersonPatientDAL_Jdbc();
    }

    public List<Person> findPersonsInOrganisations(Set<String> organisationIds, String searchTerms) throws Exception {
        List<Person> result = new ArrayList<>();

        if (organisationIds == null || organisationIds.size() == 0) {
            LOG.error("No access to any organisations");
            return result;
        }

        if (!StringUtils.isEmpty(searchTerms)) {

            SearchTermsParser parser = new SearchTermsParser(searchTerms);

            if (parser.hasNhsNumber()) {
                LOG.debug("Searching NHS Number");
                List<Person> matches = dal.searchByNhsNumber(organisationIds, parser.getNhsNumber());
                if (matches != null && matches.size() > 0)
                    result.addAll(matches);
            }

            if (parser.hasEmisNumber()) {
                LOG.debug("Searching Local Id");
                List<Person> matches = dal.searchByLocalId(organisationIds, parser.getEmisNumber());
                if (matches != null && matches.size() > 0)
                    result.addAll(matches);
            }

            if (parser.hasDateOfBirth()) {
                LOG.debug("Searching DOB");
                List<Person> matches = dal.searchByDateOfBirth(organisationIds, parser.getDateOfBirth());
                if (matches != null && matches.size() > 0)
                    result.addAll(matches);
            }

            if (parser.hasNames()) {
                LOG.debug("Searching names");
                List<Person> matches = dal.searchByNames(organisationIds, parser.getNames());
                if (matches != null && matches.size() > 0)
                    result.addAll(matches);
            }
        }

        for (Person person : result) {
            if (person.getName() == null || person.getName().isEmpty())
                person.setName("Unknown");
        }

        return result;
    }

    public List<Patient> getPatientsForPerson(Set<String> serviceIds, String personId) throws Exception {
        return dal.getPatientsByNhsNumber(serviceIds, personId);
    }

    public Patient getPatient(Set<String> serviceIds, String serviceId, String systemId, String patientId) {
        if (!serviceIds.contains(serviceId))
            return null;

        return dal.getPatient(serviceId, systemId, patientId);
    }

    /**
     * simple fn to test all methods in the DAL to ensure compatability
     */
    /*public static void main(String[] args) {

        try {
            ConfigManager.Initialize("queuereader");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        try {

            UUID serviceId = UUID.fromString("11572db8-ae34-4465-9f2f-35cc1e8a1e95");

            Set<String> serviceIds = new HashSet<>();
            serviceIds.add(serviceId.toString());

            Patient patient = null;
            List<Person> l = null;

            PersonPatientLogic o = new PersonPatientLogic();

            LOG.info("Search by NHS number");
            l = o.findPersonsInOrganisations(serviceIds, "9435754902");
            LOG.info("Got " + l.size() + " results");
            for (Person person: l) {
                LOG.info("   " + person.getNhsNumber() + ", " + person.getName() + ", " + person.getPatientId() + ", " + person.getPatientCount());

                List<Patient> patients = o.getPatientsForPerson(serviceIds, person.getNhsNumber());
                LOG.info("    Got " + patients.size() + " patients");
                for (Patient patient2: patients) {
                    LOG.info("   " + patient2.getPatientName() + ", " + patient2.getDob() + ", " + patient2.getLocalIds() + ", " + patient2.getId());

                    patient = o.getPatient(serviceIds, serviceId.toString(), "", patient2.getId().getPatientId());
                    if (patient == null) {
                        LOG.info("       Failed to get patient");
                    } else {
                        LOG.info("       " + patient.getPatientName() + ", " + patient.getDob() + ", " + patient.getLocalIds() + ", " + patient.getId());
                    }
                }
            }

            LOG.info("Search by Emis number");
            l = o.findPersonsInOrganisations(serviceIds, "4358");
            LOG.info("Got " + l.size() + " results");
            for (Person person: l) {
                LOG.info("   " + person.getNhsNumber() + ", " + person.getName() + ", " + person.getPatientId() + ", " + person.getPatientCount());

                List<Patient> patients = o.getPatientsForPerson(serviceIds, person.getNhsNumber());
                LOG.info("    Got " + patients.size() + " patients");
                for (Patient patient2: patients) {
                    LOG.info("   " + patient2.getPatientName() + ", " + patient2.getDob() + ", " + patient2.getLocalIds() + ", " + patient2.getId());

                    patient = o.getPatient(serviceIds, serviceId.toString(), "", patient2.getId().getPatientId());
                    if (patient == null) {
                        LOG.info("       Failed to get patient");
                    } else {
                        LOG.info("       " + patient.getPatientName() + ", " + patient.getDob() + ", " + patient.getLocalIds() + ", " + patient.getId());
                    }
                }
            }

            LOG.info("Search by DoB");
            l = o.findPersonsInOrganisations(serviceIds, "22-Nov-2005");
            LOG.info("Got " + l.size() + " results");
            for (Person person: l) {
                LOG.info("   " + person.getNhsNumber() + ", " + person.getName() + ", " + person.getPatientId() + ", " + person.getPatientCount());

                List<Patient> patients = o.getPatientsForPerson(serviceIds, person.getNhsNumber());
                LOG.info("    Got " + patients.size() + " patients");
                for (Patient patient2: patients) {
                    LOG.info("   " + patient2.getPatientName() + ", " + patient2.getDob() + ", " + patient2.getLocalIds() + ", " + patient2.getId());

                    patient = o.getPatient(serviceIds, serviceId.toString(), "", patient2.getId().getPatientId());
                    if (patient == null) {
                        LOG.info("       Failed to get patient");
                    } else {
                        LOG.info("       " + patient.getPatientName() + ", " + patient.getDob() + ", " + patient.getLocalIds() + ", " + patient.getId());
                    }
                }
            }

            LOG.info("Search by Names");
            l = o.findPersonsInOrganisations(serviceIds, "LUO");
            LOG.info("Got " + l.size() + " results");
            for (Person person: l) {
                LOG.info("   " + person.getNhsNumber() + ", " + person.getName() + ", " + person.getPatientId() + ", " + person.getPatientCount());

                List<Patient> patients = o.getPatientsForPerson(serviceIds, person.getNhsNumber());
                LOG.info("    Got " + patients.size() + " patients");
                for (Patient patient2: patients) {
                    LOG.info("   " + patient2.getPatientName() + ", " + patient2.getDob() + ", " + patient2.getLocalIds() + ", " + patient2.getId());

                    patient = o.getPatient(serviceIds, serviceId.toString(), "", patient2.getId().getPatientId());
                    if (patient == null) {
                        LOG.info("       Failed to get patient");
                    } else {
                        LOG.info("       " + patient.getPatientName() + ", " + patient.getDob() + ", " + patient.getLocalIds() + ", " + patient.getId());
                    }
                }
            }


        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }*/

}
