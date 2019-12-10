package org.endeavourhealth.dataassurance.dal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.FhirIdentifierUri;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.framework.ContextShutdownHook;
import org.endeavourhealth.coreui.framework.StartupConfig;
import org.endeavourhealth.dataassurance.helpers.CUIFormatter;
import org.endeavourhealth.dataassurance.models.Patient;
import org.endeavourhealth.dataassurance.models.Person;
import org.endeavourhealth.dataassurance.models.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class PersonPatientDAL_Jdbc implements PersonPatientDAL { //, ContextShutdownHook {
    private static final Logger LOG = LoggerFactory.getLogger(PersonPatientDAL_Jdbc.class);

    //private Connection _connection = null;

    public PersonPatientDAL_Jdbc() {
        //StartupConfig.registerShutdownHook("PersonPatientDAL_Jdbc", this);
    }

    @Override
    public List<Person> searchByNhsNumber(Set<String> serviceIds, String nhsNumber) throws Exception {
        Connection conn = getConnection();
        try {
            String sql = "select distinct nhs_number, forenames, surname, count(*) as cnt " +
                    "from patient_search " +
                    "where nhs_number = ? " +
                    "and service_id in ("+String.join(",", Collections.nCopies(serviceIds.size(), "?")) +") " +
                    "and dt_deleted is null " +
                    "group by nhs_number, forenames, surname";

            List<Person> ret = searchPeople(serviceIds, nhsNumber, conn, sql);

            //DAB-29 - search on past NHS numbers too
            ret.addAll(searchByPastNhsNumber(conn, serviceIds, nhsNumber));

            return ret;

        } catch (Exception e) {
            LOG.error("Error searching by NHS number  [" + nhsNumber + "]", e);
            return null;
        } finally {
            conn.close();
        }
    }

    /**
     * searches for patients by past NHS number. Any matches have that past NHS number shown in brackets after their name
     */
    private List<Person> searchByPastNhsNumber(Connection conn, Set<String> serviceIds, String nhsNumber) throws Exception {

        String sql = "select distinct nhs_number, forenames, surname, count(*) as cnt, p.service_id, p.patient_id " +
                "from patient_search p " +
                "inner join patient_search_local_identifier l on p.service_id = l.service_id and p.patient_id = l.patient_id " +
                "where l.local_id = ? " +
                "and l.local_id_system = '" + FhirIdentifierUri.IDENTIFIER_SYSTEM_NHSNUMBER + "' " +
                "and l.service_id IN ("+String.join(",", Collections.nCopies(serviceIds.size(), "?")) +") " +
                "and p.dt_deleted is null " +
                "and l.dt_deleted is null " +
                "group by nhs_number, forenames, surname, p.service_id, p.patient_id";

        List<Person> ret = searchPeople(serviceIds, nhsNumber, conn, sql);

        //append the past NHS number after the name so it's shown in the results. Not ideal, as it really should
        //be shown in the NHS number column, but at least it'll be shown somewhere
        for (Person person: ret) {
            String s = person.getName();
            s += " (" + nhsNumber + ")";
            person.setName(s);
        }

        return ret;
    }

    @Override
    public List<Person> searchByLocalId(Set<String> serviceIds, String emisNumber) throws Exception {
        Connection conn = getConnection();
        try {
            String sql = "select distinct nhs_number, forenames, surname, count(*) as cnt, p.service_id, p.patient_id " +
                "from patient_search p " +
                "inner join patient_search_local_identifier l on p.service_id = l.service_id and p.patient_id = l.patient_id " +
                "where l.local_id = ? " +
                "and l.service_id IN ("+String.join(",", Collections.nCopies(serviceIds.size(), "?")) +") " +
                "and p.dt_deleted is null " +
                "and l.dt_deleted is null " +
                "group by nhs_number, forenames, surname, p.service_id, p.patient_id";

            return searchPeople(serviceIds, emisNumber, conn, sql);

        } catch (Exception e) {
            LOG.error("Error searching by Local ID  [" + emisNumber + "]", e);
            return null;
        } finally {
            conn.close();
        }
    }

    @Override
    public List<Person> searchByDateOfBirth(Set<String> serviceIds, Date dateOfBirth) throws Exception {
        Connection conn = getConnection();
        try {
            String sql = "select distinct nhs_number, forenames, surname, count(*) as cnt, service_id, patient_id " +
                "from patient_search " +
                "where date_of_birth = ? " +
                "and service_id in ("+String.join(",", Collections.nCopies(serviceIds.size(), "?")) +") "+
                "and dt_deleted is null " +
                "group by nhs_number, forenames, surname, service_id, patient_id";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                int i = 1;
                statement.setDate(i++, new java.sql.Date(dateOfBirth.getTime()));

                for (String serviceId : serviceIds)
                    statement.setString(i++, serviceId);

                List<Person> result = getStatementResults(statement);

                return result;
            }

        } catch (Exception e) {
            LOG.error("Error searching by NHS number  [" + dateOfBirth + "]", e);
            return null;
        } finally {
            conn.close();
        }
    }

    @Override
    public List<Person> searchByNames(Set<String> serviceIds, List<String> names) throws Exception {
        Connection conn = getConnection();
        try {
            String sql;
            String name1;
            String name2 = "";

            if (names.size() == 1) {
                name1 = (names.get(0)).replace(",", "") + "%";
                sql = "select distinct nhs_number, forenames, surname, count(*) as cnt, service_id, patient_id " +
                    "from patient_search " +
                    "where (lower(surname) LIKE ? or lower(forenames) LIKE ?) " +
                    "and service_id in (" + String.join(",", Collections.nCopies(serviceIds.size(), "?")) + ") " +
                    "and dt_deleted is null " +
                    "group by nhs_number, forenames, surname, service_id, patient_id";
            } else {
                name1 = (names.get(0)).replace(",", "") + "%";
                name2 = (names.get(1)).replace(",", "") + "%";
                sql = "select distinct nhs_number, forenames, surname, count(*) as cnt, service_id, patient_id " +
                    "from patient_search " +
                    "where (" +
                        "(lower(surname) LIKE ? and lower(forenames) LIKE ?)" +
                        " or (lower(surname) LIKE ? and lower(forenames) LIKE ?)" +
                    ") " +
                    "and service_id in (" + String.join(",", Collections.nCopies(serviceIds.size(), "?")) + ") "+
                    "and dt_deleted is null " +
                    "group by nhs_number, forenames, surname, service_id, patient_id";
            }

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                int i = 1;

                if (names.size() == 1) {
                    statement.setString(i++, name1.toLowerCase());
                    statement.setString(i++, name1.toLowerCase());
                } else {
                    statement.setString(i++, name1.toLowerCase());
                    statement.setString(i++, name2.toLowerCase());
                    statement.setString(i++, name2.toLowerCase());
                    statement.setString(i++, name1.toLowerCase());
                }

                for (String serviceId : serviceIds)
                    statement.setString(i++, serviceId);

                List<Person> result = getStatementResults(statement);

                return result;
            }

        } catch (Exception e) {
            LOG.error("Error searching by names  [" + String.join("],[", names) + "]", e);
            return null;
        } finally {
            conn.close();
        }
    }

    private List<Person> searchPeople(Set<String> serviceIds, String identifier, Connection conn, String sql) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            int i = 1;
            statement.setString(i++, identifier);

            for (String serviceId : serviceIds)
                statement.setString(i++, serviceId);

            List<Person> result = getStatementResults(statement);

            return result;
        }
    }

    private List<Person> getStatementResults(PreparedStatement statement) throws SQLException {
        ResultSet rs = statement.executeQuery();

        List<Person> result = new ArrayList<>();

        while (rs.next()) {
            Person person = new Person(
                    rs.getString("nhs_number"),
                    new CUIFormatter().getFormattedName(
                        null,
                        rs.getString("forenames"),
                        rs.getString("surname")
                    ),
                    rs.getInt("cnt")
                );

            // Populate patientId if present
            if (rs.getMetaData().getColumnCount() > 4) {
                person.setPatientId(
                    new ResourceId()
                    .setServiceId( rs.getString("service_id"))
                    .setPatientId( rs.getString("patient_id"))
                );
            }

            result.add(person);
        }

        LOG.debug("Found " + result.size() + "rows");

        return result;
    }

    @Override
    public List<Patient> getPatientsByNhsNumber(Set<String> serviceIds, String nhsNumber) throws Exception {
        Connection conn = getConnection();
        try {
            String sql = "select ps.service_id, ps.patient_id, ps.forenames, ps.surname, ps.date_of_birth, li.local_id, li.local_id_system " +
                "from patient_search ps " +
                "left outer join patient_search_local_identifier li on li.service_id = ps.service_id and li.patient_id = ps.patient_id and li.dt_deleted is null " +
                "where ps.nhs_number = ? " +
                "and ps.service_id in ("+String.join(",", Collections.nCopies(serviceIds.size(), "?")) +") " +
                "and ps.dt_deleted is null";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                int i = 1;
                statement.setString(i++, nhsNumber);

                for (String serviceId : serviceIds)
                    statement.setString(i++, serviceId);

                ResultSet rs = statement.executeQuery();

                List<Patient> result = new ArrayList<>();

                Patient lastPatient = null;

                while (rs.next()) {
                    if (lastPatient == null || !lastPatient.getId().getPatientId().equals(rs.getString("patient_id"))) {
                        Patient patient = getPatientFromResultSet(rs);
                        result.add(patient);
                        lastPatient = patient;
                    }
                    String system = rs.getString("local_id_system");
                    if (!Strings.isNullOrEmpty(system)) {
                        system = system.substring(system.lastIndexOf('/') + 1);
                        lastPatient.getLocalIds().put(system, rs.getString("local_id"));
                    }
                }

                return result;
            }
        } catch (Exception e) {
            LOG.error("Error fetching patients for person [" + nhsNumber + "]", e);
            return null;
        } finally {
            conn.close();
        }
    }

    @Override
    public Patient getPatient(String serviceId, String systemId, String patientId) throws Exception {
        Patient patient = null;
        Connection conn = getConnection();
        try {
            String sql = "select ps.service_id, ps.patient_id, ps.forenames, ps.surname, ps.date_of_birth, li.local_id, li.local_id_system " +
                "from patient_search ps " +
                "left outer join patient_search_local_identifier li on li.service_id = ps.service_id and li.patient_id = ps.patient_id " +
                "where ps.service_id = ? " +
                "and ps.patient_id = ?";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                int i = 1;
                statement.setString(i++, serviceId);

                statement.setString(i++, patientId);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    if (patient == null)
                        patient = getPatientFromResultSet(rs);
                    String system = rs.getString("local_id_system");
                    if (!Strings.isNullOrEmpty(system)) {
                        system = system.substring(system.lastIndexOf('/') + 1);
                        patient.getLocalIds().put(system, rs.getString("local_id"));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error fetching patient [" + serviceId+ ", " + systemId + ", " + patientId + "]", e);
        } finally {
            conn.close();
        }

        return patient;
    }

    private Patient getPatientFromResultSet(ResultSet rs) throws SQLException {
        Date dateOfBirth = rs.getDate("date_of_birth");
        String dateOfBirthStr = (dateOfBirth == null) ? null : dateOfBirth.toString();
        return new Patient(
            rs.getString("service_id"),
            null,
            rs.getString("patient_id"),
            new CUIFormatter().getFormattedName(
                null,
                rs.getString("forenames"),
                rs.getString("surname")
            ),
            dateOfBirthStr
        );
    }

    private Connection getConnection() throws Exception {
        return ConnectionManager.getEdsConnection();
    }

}
