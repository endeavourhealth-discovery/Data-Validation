package org.endeavourhealth.dataassurance.dal;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.reference.models.Concept;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.QueryDocument;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.dataassurance.models.Practitioner;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class ReportDAL_Jdbc implements ReportDAL {
	private static final Logger LOG = LoggerFactory.getLogger(ReportDAL_Jdbc.class);
    private Connection _conn = null;
    private boolean _isPseudo = true;
	private String enterpriseDb;

	public ReportDAL_Jdbc(String enterpriseDb) {
	    this.enterpriseDb = enterpriseDb;
    }

	@Override
	public LibraryItem runReport(UUID userUuid, UUID reportUuid, Map<String, String> reportParams) throws Exception {
		ServiceDalI svcRepo = DalProvider.factoryServiceDal();
		Service svc = svcRepo.getById(UUID.fromString(reportParams.get("OrganisationUuid")));
		String odsCode = svc.getLocalId();

		LOG.trace("GettingLibraryItem for UUID {}", reportUuid);
		LibraryDalI repository = DalProvider.factoryLibraryDal();
		ActiveItem activeItem = repository.getActiveItemByItemId(reportUuid);
		Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
		LibraryItem report = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());

		// Parse the run date from the report params
		java.util.Date rundate = SqlUtils.sqlDateFromString(reportParams.get("RunDate").replace("'",""));
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(rundate);
		report.getCountReport().setLastRun(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));

		report.getCountReport().setStatus("Running...");
		saveCountReport(repository, item, report);
		try {
			Connection conn = getConnection();
			PreparedStatement statement = null;

			// Clear old results
			String tablename = getTableName(userUuid, reportUuid);
			tablename = tablename.replace("'", "''");   //escape check to ensure SI not possible

			dropReportTable(conn, tablename);

			// Build query
			createReportTable(reportParams, odsCode, report, conn, tablename);

			int rowCount = getReportItemCount(report, conn, statement, tablename);

			// Update library item
			report.getCountReport().setCount(rowCount);
			report.getCountReport().setStatus("Completed");
			saveCountReport(repository, item, report);

		} catch (SQLException e) {
			LOG.error("Error running report", e);
			report.getCountReport().setStatus("Error");
			saveCountReport(repository, item, report);
			throw e;
		}

		return report;
	}

	private int getReportItemCount(LibraryItem report, Connection conn, PreparedStatement statement, String tablename) throws SQLException {
		try {
			// Get results count
			LOG.debug("Getting result count");
			statement = conn.prepareStatement("SELECT COUNT(*) FROM " + tablename);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			int rowCount = resultSet.getInt(1);
			LOG.debug("Rows Affected : " + rowCount);
			return rowCount;
		} finally {
			statement.close();
		}
	}

	private void  createReportTable(Map<String, String> reportParams, String odsCode, LibraryItem report, Connection conn, String tablename) throws Exception {
		String query = "SELECT :RunDate as run_date, p.id as internal_patient_id\n";

		if (!_isPseudo)
		    query += ", p.nhs_number\n";

		if (!report.getCountReport().getFields().equals(""))
			query += "," + report.getCountReport().getFields();

		query += " FROM patient p JOIN episode_of_care eoc ON eoc.patient_id = p.id \n";
		query += " JOIN organization org ON org.id = p.organization_id \n";
		if (report.getCountReport().getTables() != null)
			query += report.getCountReport().getTables();

		query += " WHERE eoc.date_registered < :RunDate \nAND (eoc.date_registered_end IS NULL OR eoc.date_registered_end >= :RunDate) \n";
		query += " AND (p.date_of_death IS NULL OR p.date_of_death >= COALESCE(:DateOfDeath, p.date_of_death))\n";
		query += " AND org.ods_code = '" + odsCode + "' \n";
		if (report.getCountReport().getQuery() != null)
			query += report.getCountReport().getQuery();

		query = "CREATE TABLE " + tablename + " AS \n" + query;
		PreparedStatement statement = null;
		try {
			statement = setParameters(conn, reportParams, query);

			statement.execute();
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	private PreparedStatement setParameters(Connection conn, Map<String, String> reportParams, String query) throws SQLException, ParseException {
		// Build ordered list of param positions
		TreeMap<Integer, String> params = new TreeMap<>();

		for (String paramName : reportParams.keySet()) {
			int pos = query.indexOf(":" + paramName);
			while (pos > -1) {
				params.put(pos, paramName);
				pos = query.indexOf(":" + paramName, pos+1);
			}
		}

		// Replace parameter names
		for (String paramName : reportParams.keySet()) {
			query = query.replace(":" + paramName, "?");
		}

		LOG.debug("Preparing SQL " + query);

		// Prepare statement
		PreparedStatement statement = conn.prepareStatement(query);

		// Set parameters
		int index = 1;
		for (Map.Entry<Integer, String> param : params.entrySet()) {
			String value = reportParams.get(param.getValue());
			if ("null".equals(value))
				value = null;

			switch (param.getValue()) {
				case "RunDate":
				case "EffectiveDate":
				case "DobMin":
				case "DobMax":
                case "DateOfDeath":
					if (value == null)
						statement.setNull(index++, Types.DATE);
					else
						statement.setDate(index++, SqlUtils.sqlDateFromString(value));
					break;
				case "SnomedCode":
				case "EncounterType":
				case "ReferralSnomedCode":
					if (value == null)
						statement.setNull(index++, Types.INTEGER);
					else
						statement.setLong(index++, Long.parseLong(value));
					break;
				case "ValueMin":
				case "ValueMax":
					if (value == null)
						statement.setNull(index++, Types.DOUBLE);
					else
						statement.setDouble(index++, Double.parseDouble(value));
					break;
				case "RegistrationType":
				case "Gender":
				case "AuthType":
				case "ReferralType":
				case "ReferralPriority":
					if (value == null)
						statement.setNull(index++, Types.SMALLINT);
					else
						statement.setByte(index++, Byte.parseByte(value));
					break;
				case "Practitioner":
					if (value == null)
						statement.setNull(index++, Types.INTEGER);
					else
						statement.setInt(index++, Integer.parseInt(value));
					break;
				case "OriginalCode":
					if (value == null)
						statement.setNull(index++, Types.VARCHAR);
					else
						statement.setString(index++, value);
					break;
				default:
					if (value == null)
						statement.setNull(index++, Types.VARCHAR);
					else
						statement.setString(index++, value);
			}
		}

		return statement;
	}

	private void dropReportTable(Connection conn, String tablename) throws SQLException {
		LOG.debug("Clearing table " + tablename);
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("drop table if exists " + tablename);
			statement.execute();
		} catch (PSQLException p) {
			LOG.debug(p.getMessage());
			throw p;
		} finally {
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public List<List<String>> getNHSExport(UUID userUuid, UUID reportUuid) throws Exception {
	    String fieldList = "run_date, internal_patient_id";
	    if (!_isPseudo)
	        fieldList += ", nhs_number";
		return getResults(userUuid, reportUuid, fieldList);
	}

	@Override
	public List<List<String>> getDataExport(UUID userUuid, UUID reportUuid) throws Exception {
		return getResults(userUuid, reportUuid, "*");
	}

	private List<List<String>> getResults(UUID userUuid, UUID reportUuid, String fields) throws Exception {
		String tablename = getTableName(userUuid, reportUuid);
		tablename = tablename.replace("'", "''");   //escape check to ensure SI not possible

		Connection conn = getConnection();
		fields = fields.replace("'", "''");   //escape check to ensure SI not possible
		PreparedStatement statement = conn.prepareStatement("SELECT "+fields+" FROM " + tablename);

		return SqlUtils.getStatementResultsAsCSV(statement);
	}

	@Override
	public List<Concept> getEncounterTypes() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT DISTINCT snomed_concept_id, original_term FROM encounter");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	@Override
	public List<Concept> getReferralTypes() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT id, value FROM referral_request_type");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	@Override
	public List<Concept> getReferralPriorities() throws Exception {
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT id, value FROM referral_request_priority");
		try {
			ResultSet resultSet = statement.executeQuery();
			return buildConceptEntityList(resultSet);
		} finally {
			statement.close();
		}
	}

	@Override
	public List<Practitioner> searchPractitioner(String searchData, UUID organisationUuid) throws Exception {
		ServiceDalI svcRepo = DalProvider.factoryServiceDal();
		Service svc = svcRepo.getById(organisationUuid);
		String odsCode = svc.getLocalId();

		List<Practitioner> result = new ArrayList<>();
		Connection conn = getConnection();

		PreparedStatement statement = conn.prepareStatement("SELECT p.id, p.name FROM practitioner p JOIN organization o ON o.id = p.organization_id WHERE o.ods_code = ? AND p.name LIKE ?");

		try {
			statement.setString(1, odsCode);
			statement.setString(2, "%" + searchData + "%");
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
                Practitioner practitioner = new Practitioner();
				practitioner.setId(resultSet.getInt(1));
				practitioner.setName(resultSet.getString(2));
				result.add(practitioner);
			}
		} finally {
			statement.close();
		}

		return result;
	}

	private List<Concept> buildConceptEntityList(ResultSet resultSet) throws SQLException {
		List<Concept> result = new ArrayList<>();

		while (resultSet.next()) {
			Concept concept = new Concept();
			concept.setCode(resultSet.getString(1));
			if (resultSet.getString(2) == null)
				concept.setDisplay("[NULL]");
			else
				concept.setDisplay(resultSet.getString(2));
			result.add(concept);
		}

		return result;
	}

	private void saveCountReport(LibraryDalI repository, Item item, LibraryItem libraryItem) throws Exception {
		LOG.debug("Saving count report " + libraryItem.getUuid());
		QueryDocument doc = new QueryDocument();
		doc.getLibraryItem().add(libraryItem);
		item.setXmlContent(QueryDocumentSerializer.writeToXml(doc));
		List<Object> toSave = new ArrayList<>();
		toSave.add(item);
		repository.save(toSave);
	}

	private String getTableName(UUID userUuid, UUID reportUuid) {
		return ("workspace.rep_" + userUuid.toString().hashCode() + "_" + reportUuid.toString().hashCode()).replace('-','_');
	}

	private Connection getConnection() throws SQLException, IOException {
		if (_conn == null) {
			JsonNode config = ConfigManager.getConfigurationAsJson(this.enterpriseDb);
			String url = config.get("url").asText();
			String username = config.get("ui-username").asText();
			String password = config.get("ui-password").asText();
			_isPseudo = config.get("isPseudo").asBoolean(true);

			_conn = DriverManager.getConnection(url, username, password);
		}
		return _conn;
	}
}
