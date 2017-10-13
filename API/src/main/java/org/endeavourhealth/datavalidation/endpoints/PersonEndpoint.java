package org.endeavourhealth.datavalidation.endpoints;
import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.datavalidation.logic.PersonPatient;
import org.endeavourhealth.datavalidation.logic.Security;
import org.endeavourhealth.datavalidation.models.Patient;
import org.endeavourhealth.datavalidation.models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/person")
@Metrics(registry = "dataValidationMetricRegistry")
@Api(description = "Api for all calls relating to persons")
public class PersonEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(PersonEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataValidation.PersonEndpoint.Get")
    @Path("/")
    @ApiOperation(value = "Returns a list of matching persons")
    public Response get(@Context SecurityContext sc,
                        @ApiParam(value = "Mandatory Search terms") @QueryParam("searchTerms") String searchTerms
    ) throws Exception {
        LOG.debug("Get Called");

        List<Person> matches = PersonPatient.findPersonsInOrganisations(Security.getUserAllowedOrganisationIdsFromSecurityContext(sc), searchTerms);

        return Response
            .ok()
            .entity(matches)
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataValidation.PersonEndpoint.GetPatients")
    @Path("/patients")
    @ApiOperation(value = "Returns a list patients for a given person")
    public Response getPatients(@Context SecurityContext sc,
                        @ApiParam(value = "Mandatory Person Id") @QueryParam("personId") String personId
    ) throws Exception {
        LOG.debug("GetPatients Called");

        List<Patient> patients = PersonPatient.getPatientsForPerson(Security.getUserAllowedOrganisationIdsFromSecurityContext(sc), personId);

        return Response
            .ok()
            .entity(patients)
            .build();
    }
}