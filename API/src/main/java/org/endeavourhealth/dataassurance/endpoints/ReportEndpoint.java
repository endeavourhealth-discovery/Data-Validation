package org.endeavourhealth.dataassurance.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dataassurance.helpers.Security;
import org.endeavourhealth.dataassurance.logic.ReportLogic;
import org.endeavourhealth.dataassurance.models.FhirConcept;
import org.endeavourhealth.dataassurance.models.Practitioner;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

/**
 * REST API for reports.  Provides all methods on the path "/report"
 */
@Path("/report")
public final class ReportEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ReportEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsPatientExplorerModule.CountReport);
    /**
     * Run a predefined report
     * @param sc                Security context (provided)
     * @param reportUuid          UUID of the report to run
     * @param reportParamsJson  Appropriate report filtering parameters
     * @return  LibraryItem definition of the report that ran
     * @throws Exception
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ReportEndpoint.RunReport")
    @Path("/runReport")
    public Response runReport(@Context SecurityContext sc, @QueryParam("reportUuid") UUID reportUuid, String reportParamsJson) throws Exception {
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Report",
            "uuid", reportUuid,
            "params", reportParamsJson);

        LibraryItem ret = new ReportLogic(Security.getEnterpriseConfigName()).runReport(reportUuid, reportParamsJson, userUuid);

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

    /**
     * Export a list of NHS Numbers resulting from the most recent run of a report
     * @param sc        Security context (provided)
     * @param uuid  UUID of the report to export
     * @return  Line-break separated list of NHS Numbers
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name = "ReportEndpoint.ExportNhsNumbers")
    @Path("/exportNHS")
    public Response exportNHSNumbers(@Context SecurityContext sc, @QueryParam("uuid") UUID uuid) throws Exception {
            UUID userUuid = SecurityUtils.getCurrentUserId(sc);
            userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Export NHS Numbers",
                "uuid", uuid);

            String ret = new ReportLogic(Security.getEnterpriseConfigName()).getNhsExport(uuid, userUuid);

            return Response
                .ok(ret, MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    /**
     * Export the data resulting from the most recent run of a report
     * @param sc        Security context (provided)
     * @param uuid  UUID of the report to export
     * @return  CSV export of the report data
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name = "ReportEndpoint.ExportData")
    @Path("/exportData")
    public Response exportData(@Context SecurityContext sc, @QueryParam("uuid") UUID uuid) throws Exception {
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Export Data",
            "uuid", uuid);
        String ret = new ReportLogic(Security.getEnterpriseConfigName()).getDataExport(uuid, userUuid);

        return Response
            .ok(ret, MediaType.TEXT_PLAIN_TYPE)
            .build();
    }

    /**
     * Retrieve a distinct list of encounter types (based on code AND term)
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ReportEndpoint.GetEncounterTypes")
    @Path("/encounterType")
    public Response getEncounterTypes(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Encounter Types");
        List<FhirConcept> ret = new ReportLogic(Security.getEnterpriseConfigName()).getEncounterTypes();

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

    /**
     * Retrieve a distinct list of referral types
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ReportEndpoint.GetReferralTypes")
    @Path("/referralTypes")
    public Response getReferralTypes(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Referral Types");
        List<FhirConcept> ret = new ReportLogic(Security.getEnterpriseConfigName()).getReferralTypes();

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

    /**
     * Retrieve a distinct list of referral priorities
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ReportEndpoint.GetReferralPriorities")
    @Path("/referralPriorities")
    public Response getReferralPriorities(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Referral Priorities");
        List<FhirConcept> ret = new ReportLogic(Security.getEnterpriseConfigName()).getReferralPriorities();

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ReportEndpoint.SearchPractitioner")
    @Path("/searchPractitioner")
    public Response searchPractitioner(
        @Context SecurityContext sc,
        @QueryParam("searchData") String searchData,
        @QueryParam("organisationUuid") UUID organisationUuid) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), organisationUuid, AuditAction.Load, "Practitioner Search");

        List<Practitioner> ret = new ReportLogic(Security.getEnterpriseConfigName()).searchPractitioners(searchData, organisationUuid);

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

}