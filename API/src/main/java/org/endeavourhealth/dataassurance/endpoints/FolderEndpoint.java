package org.endeavourhealth.dataassurance.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dataassurance.logic.FolderLogic;
import org.endeavourhealth.dataassurance.models.FolderList;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * Endpoint for functions related to creating and managing folders
 */
@Path("/folder")
public final class FolderEndpoint extends AbstractEndpoint {
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Folders);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "FolderEndpoint.GetFolders")
    @Path("/getFolders")
    public Response getFolders(@Context SecurityContext sc, @QueryParam("folderType") int folderType, @QueryParam("parentUuid") String parentUuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Folders",
            "Folder Type", folderType,
            "Parent Uuid", parentUuidStr);

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        FolderList ret = new FolderLogic().getFolderList(folderType, parentUuidStr, orgUuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
