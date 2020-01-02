package org.endeavourhealth.dataassurance.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dataassurance.logic.LibraryLogic;
import org.endeavourhealth.dataassurance.models.FolderContentsList;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/library")
public final class LibraryEndpoint extends AbstractEndpoint {
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "LibraryEndpoint.GetLibraryItem")
    @Path("/getLibraryItem")
    public Response getLibraryItem(@Context SecurityContext sc, @QueryParam("uuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "LibraryItem",
            "Item Id", uuidStr);

        LibraryItem ret = new LibraryLogic().getLibraryItem(UUID.fromString(uuidStr));

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "LibraryEndpoint.GetFolderContents")
    @Path("/getFolderContents")
    public Response getFolderContents(@Context SecurityContext sc, @QueryParam("folderUuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "FolderContents",
            "Folder Id", uuidStr);

        UUID folderUuid = UUID.fromString(uuidStr);

        FolderContentsList ret = new LibraryLogic().getFolderContents(folderUuid);

        clearLogbackMarkers();

        return Response
            .ok()
            .entity(ret)
            .build();
    }


}









