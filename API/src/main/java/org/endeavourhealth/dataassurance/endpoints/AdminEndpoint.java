package org.endeavourhealth.dataassurance.endpoints;
import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.dataassurance.logic.AdminLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/admin")
@Metrics(registry = "dataAssuranceMetricRegistry")
@Api(description = "API for all calls relating to admin")
public class AdminEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AdminEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name = "AdminEndpoint.Service.Name")
    @Path("/service/name")
    @ApiOperation(value = "Returns a the name for the given service id")
    public Response getServiceName(@Context SecurityContext sc,
                                @ApiParam(value = "Mandatory Service Id") @QueryParam("serviceId") String serviceId
    ) throws Exception {
        LOG.debug("Get Service Name Called");

        String resourceData = new AdminLogic().getServiceName(serviceId);

        return Response
            .ok()
            .entity(resourceData)
            .build();
    }


}
