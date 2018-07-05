package org.endeavourhealth.dataassurance.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.reference.models.Concept;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.dataassurance.dal.ReportDAL;
import org.endeavourhealth.dataassurance.dal.ReportDAL_Jdbc;
import org.endeavourhealth.dataassurance.dal.SqlUtils;
import org.endeavourhealth.dataassurance.models.FhirConcept;
import org.endeavourhealth.dataassurance.models.Practitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportLogic {
    private static final Logger LOG = LoggerFactory.getLogger(ReportLogic.class);
    private ReportDAL reportProvider;

    public ReportLogic(String enterpriseDb) {
        this.reportProvider = new ReportDAL_Jdbc(enterpriseDb);
    }

    public LibraryItem runReport(UUID reportUuid, String reportParamsJson, UUID userUuid) throws Exception {
        LOG.debug("runReport");

        Map<String, String> reportParams = ObjectMapperPool.getInstance().readValue(reportParamsJson, new TypeReference<Map<String, String>>() {
        });
        return reportProvider.runReport(userUuid, reportUuid, reportParams);
    }

    public String getNhsExport(UUID uuid, UUID userUuid) throws Exception {
        LOG.debug("exportNHS");

        List<List<String>> data = reportProvider.getNHSExport(userUuid, uuid);

        return SqlUtils.getCSVAsString(data);
    }

    public String getDataExport(UUID uuid, UUID userUuid) throws Exception {
        LOG.debug("exportData");

        List<List<String>> data = reportProvider.getDataExport(userUuid, uuid);
        return SqlUtils.getCSVAsString(data);
    }

    public List<FhirConcept> getEncounterTypes() throws Exception {
        LOG.debug("getEncounterTypes");

        List<Concept> data = reportProvider.getEncounterTypes();
        return data.stream().map(FhirConcept::new).collect(Collectors.toList());
    }

    public List<FhirConcept> getReferralTypes() throws Exception {
        LOG.debug("getReferralTypes");

        List<Concept> data = reportProvider.getReferralTypes();
        return data.stream().map(FhirConcept::new).collect(Collectors.toList());
    }

    public List<FhirConcept> getReferralPriorities() throws Exception {
        LOG.debug("getReferralPriorities");

        List<Concept> data = reportProvider.getReferralPriorities();
        return data.stream().map(FhirConcept::new).collect(Collectors.toList());
    }


    public List<Practitioner> searchPractitioners(String searchData, UUID organisationUuid) throws Exception {
        LOG.debug("searchPractitioner");

        return reportProvider.searchPractitioner(searchData, organisationUuid);
    }
}
