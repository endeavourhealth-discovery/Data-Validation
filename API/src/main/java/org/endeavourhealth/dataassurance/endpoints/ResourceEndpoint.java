package org.endeavourhealth.dataassurance.endpoints;
import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMapping;
import org.endeavourhealth.dataassurance.logic.ResourceLogic;
import org.endeavourhealth.dataassurance.helpers.Security;
import org.endeavourhealth.dataassurance.models.ResourceRequest;
import org.endeavourhealth.dataassurance.models.ResourceType;
import org.endeavourhealth.dataassurance.models.PatientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/resource")
@Metrics(registry = "dataAssuranceMetricRegistry")
@Api(description = "API for all calls relating to admin")
public class ResourceEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.ResourceEndpoint.Types")
    @Path("/type")
    @ApiOperation(value = "Returns a list of all resource types")
    public Response get(@Context SecurityContext sc) throws Exception {
        LOG.debug("Get Types Called");

        List<ResourceType> resourceTypes = new ResourceLogic().getTypes();

        return Response
            .ok()
            .entity(resourceTypes)
            .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.ResourceEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Returns a list of all resources of the given types for the given service patients")
    public Response getForPatient(@Context SecurityContext sc,
                                  @ApiParam(value = "Mandatory Resource Request") String resourceRequestJson
    ) throws Exception {
        LOG.debug("getForPatient called");

        ResourceRequest resourceRequest = ObjectMapperPool.getInstance().readValue(resourceRequestJson, ResourceRequest.class);

        List<PatientResource> resources = new ResourceLogic().getPatientResources(
            new Security().getUserAllowedOrganisationIdsFromSecurityContext(sc),
            resourceRequest.getPatients(),
            resourceRequest.getResourceTypes());

        return Response
            .ok()
            .entity(resources)
            .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.ResourceEndpoint.Reference")
    @Path("/reference")
    @ApiOperation(value = "Returns the description for a given service, system and reference")
    public Response reference(@Context SecurityContext sc,
                              @ApiParam(value = "Mandatory ServiceId") @QueryParam("serviceId") String serviceId,
                              @ApiParam(value = "Mandatory reference") @QueryParam("reference") String reference) throws Exception {
        LOG.debug("Get Reference Called");

        String referenceDescription = new ResourceLogic().getReferenceDescription(serviceId, reference);
         LOG.debug("Reference:" + referenceDescription + "for ref:" + reference + " service:" + serviceId);
        return Response
            .ok(referenceDescription)
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataAssurance.ResourceEndpoint.FieldMappings")
    @Path("/fieldMappings")
    @ApiOperation(value = "Returns the field mappings for a given resource")
    public Response fieldMappings(@Context SecurityContext sc,
                                  @ApiParam(value = "Mandatory ServiceId") @QueryParam("serviceId") String serviceId,
                                  @ApiParam(value = "Mandatory ResourceType") @QueryParam("resourceType") String resourceType,
                                  @ApiParam(value = "Mandatory ResourceId") @QueryParam("resourceId") String resourceId
    ) {
        LOG.debug("Get Field Mappings Called");

        List<ResourceFieldMapping> fieldMappings = new ResourceLogic().getResourceMappings(serviceId, resourceType, resourceId);
        LOG.debug("Service:" + serviceId + ". ResType:" + resourceType + ".ResId:" + resourceId );
        for (ResourceFieldMapping rfm : fieldMappings) {
            LOG.debug(rfm.getResourceField() + ":" +rfm.getValue());
        }
        return Response
            .ok(fieldMappings)
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "DataValidation.ResourceEndpoint.FieldMappingForField")
    @Path("/fieldMappingForField")
    @ApiOperation(value = "Returns the field mappings for a given resource and field")
    public Response fieldMappingForField(@Context SecurityContext sc,
                                         @ApiParam(value = "Mandatory ServiceId") @QueryParam("serviceId") String serviceId,
                                         @ApiParam(value = "Mandatory ResourceType") @QueryParam("resourceType") String resourceType,
                                         @ApiParam(value = "Mandatory ResourceId") @QueryParam("resourceId") String resourceId,
                                         @ApiParam(value = "Mandatory Field") @QueryParam("field") String field
    ) {
        LOG.debug("Get Field Mapping For Field Called");
        LOG.debug("Field:" + field + ".ResId:" + resourceId + ".ResType:" + resourceType) ;

        List<ResourceFieldMapping> fieldMapping = new ResourceLogic().getResourceMappingsForField(serviceId, resourceType, resourceId, field);

        return Response
            .ok(fieldMapping)
            .build();
    }
}
