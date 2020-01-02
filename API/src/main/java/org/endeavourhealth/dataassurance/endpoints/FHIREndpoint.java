package org.endeavourhealth.dataassurance.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAudit;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAuditHelper;
import org.endeavourhealth.dataassurance.helpers.Security;
import org.endeavourhealth.dataassurance.logic.FHIRLogic;
import org.endeavourhealth.dataassurance.models.FhirRequest;
import org.endeavourhealth.dataassurance.models.ResourceType;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("/fhir")
@Metrics(registry = "dataAssuranceMetricRegistry")
@Api(description = "API for all calls fhir based calls")
public class FHIREndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FHIREndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "FHIREndpoint.Types")
    @Path("/resourceType")
    @ApiOperation(value = "Returns a list of all resource types")
    public Response getResourceTypes(@Context HttpServletRequest request,
                                     @Context SecurityContext sc,
                                     @Context UriInfo uriInfo) throws Exception {
        LOG.debug("Get resource types called");

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {
            List<ResourceType> result = new FHIRLogic().getResourceTypes();

            Response r = Response
                    .ok()
                    .entity(result)
                    .build();
            SubscriberApiAuditHelper.updateAudit(audit, r, false);
            return r;

        } finally {
            SubscriberApiAuditHelper.save(audit);
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "FHIREndpoint.Patients")
    @Path("/patients")
    @ApiOperation(value = "Returns a list of patients base on NHS number")
    public Response getPatients(@Context HttpServletRequest request,
                                @Context SecurityContext sc,
                                @Context UriInfo uriInfo,
                                @ApiParam(value = "Mandatory NHS number") @QueryParam("nhsNumber") String nhsNumber,
                                @ApiParam(value = "Mandatory Search terms") @HeaderParam("projectId") String projectId
    ) throws Exception {
        LOG.debug("Get patients called");

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {
            Bundle patients = new FHIRLogic().getPatientsByNHSNumber(nhsNumber, Security.getAllowedServiceUuids(projectId, sc));
            String result = ParserPool.getInstance().composeString("application/json", patients);

            Response r = Response
                .ok()
                .entity(result)
                .build();
            SubscriberApiAuditHelper.updateAudit(audit, r, false);
            return r;

        } finally {
            SubscriberApiAuditHelper.save(audit);
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "FHIREndpoint.Get")
    @Path("/resources")
    @ApiOperation(value = "Returns a list of all resources of the given types for the given service patients")
    public Response getForPatient(@Context HttpServletRequest request,
                                  @Context SecurityContext sc,
                                  @Context UriInfo uriInfo,
                                  @ApiParam(value = "Mandatory Resource Request") String resourceRequestJson,
                                  @ApiParam(value = "Mandatory Search terms") @HeaderParam("projectId") String projectId
    ) throws Exception {
        LOG.debug("getForPatient called");

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {
            JsonNode json = ObjectMapperPool.getInstance().readTree(resourceRequestJson);

            FhirRequest fhirRequest = new FhirRequest()
                .setPatients((Bundle)ParserPool.getInstance().parse(json.get("patients").toString()))
                .setResources(ObjectMapperPool.getInstance().readValue(json.get("resources").toString(), new TypeReference<List<String>>(){}));

            Set<String> allowedOrgs = Security.getAllowedServiceUuids(projectId, sc);

            Bundle resources = new FHIRLogic().getPatientResources(allowedOrgs,fhirRequest);

            String result = ParserPool.getInstance().composeString("application/json", resources);

            Response r = Response
                .ok()
                .entity(result)
                .build();
            SubscriberApiAuditHelper.updateAudit(audit, r, false);
            return r;

        } finally {
            SubscriberApiAuditHelper.save(audit);
        }
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "FHIREndpoint.Reference")
    @Path("/reference")
    @ApiOperation(value = "Returns the admin resource a given service and reference")
    public Response adminResource(@Context HttpServletRequest request,
                                  @Context SecurityContext sc,
                                  @Context UriInfo uriInfo,
                                  @ApiParam(value = "Mandatory reference") @QueryParam("reference") String reference,
                                  @ApiParam(value = "Mandatory Search terms") @HeaderParam("projectId") String projectId) throws Exception {
        LOG.debug("Get reference resource called");

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {

            Set<String> allowedOrgs = Security.getAllowedServiceUuids(projectId, sc);
            Resource resource = new FHIRLogic().getAdminResource(allowedOrgs, reference);
            String result = resource == null ? null : ParserPool.getInstance().composeString("application/json", resource);

            Response r = Response
                .ok(result)
                .build();
            SubscriberApiAuditHelper.updateAudit(audit, r, false);
            return r;

        } finally {
            SubscriberApiAuditHelper.save(audit);
        }
    }
}
