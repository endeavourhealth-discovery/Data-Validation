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
import org.endeavourhealth.dataassurance.helpers.Security;
import org.endeavourhealth.dataassurance.logic.FHIRLogic;
import org.endeavourhealth.dataassurance.models.FhirRequest;
import org.endeavourhealth.dataassurance.models.ResourceType;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Set;

@Path("/fhir")
@Metrics(registry = "dataAssuranceMetricRegistry")
@Api(description = "API for all calls fhir based calls")
public class FHIREndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FHIREndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.FHIREndpoint.Types")
    @Path("/resourceType")
    @ApiOperation(value = "Returns a list of all resource types")
    public Response getResourceTypes(@Context SecurityContext sc) throws Exception {
        LOG.debug("Get resource types called");

        List<ResourceType> result = new FHIRLogic().getResourceTypes();

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.FHIREndpoint.Patients")
    @Path("/patients")
    @ApiOperation(value = "Returns a list of patients base on NHS number")
    public Response getPatients(@Context SecurityContext sc,
                                @ApiParam(value = "Mandatory NHS number") @QueryParam("nhsNumber") String nhsNumber
    ) throws Exception {
        LOG.debug("Get patients called");

        Bundle patients = new FHIRLogic().getPatientsByNHSNumber(nhsNumber, new Security().getUserAllowedOrganisationIdsFromSecurityContext(sc));
        String result = ParserPool.getInstance().composeString("application/json", patients);

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.FHIREndpoint.Get")
    @Path("/resources")
    @ApiOperation(value = "Returns a list of all resources of the given types for the given service patients")
    public Response getForPatient(@Context SecurityContext sc,
                                  @ApiParam(value = "Mandatory Resource Request") String resourceRequestJson
    ) throws Exception {
        LOG.debug("getForPatient called");

        JsonNode json = ObjectMapperPool.getInstance().readTree(resourceRequestJson);

        FhirRequest fhirRequest = new FhirRequest()
            .setPatients((Bundle)ParserPool.getInstance().parse(json.get("patients").toString()))
            .setResources(ObjectMapperPool.getInstance().readValue(json.get("resources").toString(), new TypeReference<List<String>>(){}));

        Set<String> allowedOrgs = new Security().getUserAllowedOrganisationIdsFromSecurityContext(sc);

        Bundle resources = new FHIRLogic().getPatientResources(allowedOrgs,fhirRequest);

        String result = ParserPool.getInstance().composeString("application/json", resources);

        return Response
            .ok()
            .entity(result)
            .build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.FHIREndpoint.Reference")
    @Path("/reference")
    @ApiOperation(value = "Returns the admin resource a given service and reference")
    public Response adminResource(@Context SecurityContext sc,
                                  @ApiParam(value = "Mandatory reference") @QueryParam("reference") String reference) throws Exception {
        LOG.debug("Get reference resource called");

        Set<String> allowedOrgs = new Security().getUserAllowedOrganisationIdsFromSecurityContext(sc);
        Resource resource = new FHIRLogic().getAdminResource(allowedOrgs, reference);
        String result = resource == null ? null : ParserPool.getInstance().composeString("application/json", resource);

        return Response
            .ok(result)
            .build();
    }
}
