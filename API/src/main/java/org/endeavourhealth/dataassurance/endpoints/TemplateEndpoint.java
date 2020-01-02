package org.endeavourhealth.dataassurance.endpoints;
import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.dataassurance.logic.TemplateLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/template")
@Metrics(registry = "dataAssuranceMetricRegistry")
@Api(description = "API for all calls relating to admin")
public class TemplateEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(TemplateEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name = "TemplateEndpoint.Get")
    @ApiOperation(value = "Returns a the template for a given resource type")
    public Response getServiceName(@Context SecurityContext sc,
                                @ApiParam(value = "Mandatory Resource Type") @QueryParam("resourceType") String resourceType
    ) throws Exception {
        LOG.debug("Get Template called");

        String resourceData = new TemplateLogic().getTemplate(resourceType);

        return Response
            .ok()
            .entity(resourceData)
            .build();
    }
}
