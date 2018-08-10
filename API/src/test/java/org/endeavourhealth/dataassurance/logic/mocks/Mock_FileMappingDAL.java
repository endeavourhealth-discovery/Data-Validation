package org.endeavourhealth.dataassurance.logic.mocks;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.publisherTransform.SourceFileMappingDalI;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMapping;
import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMappingAudit;
import org.endeavourhealth.core.database.dal.publisherTransform.models.SourceFileRecord;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Mock_FileMappingDAL implements SourceFileMappingDalI {
    public static final String LEAF_NO_AUDIT = "leaf.no.audit";

    public static final String LEAF_WITH_AUDIT = "leaf.with.audit";
    public static final String LEAF_WITH_PARENT_AUDIT = "leaf.with.audit.randomChild";

    public static final String LEAF_WITH_PARENT_AND_CHILD_AUDIT = "leaf.with.audit.childWithAudit";

    public Exception exception;

    @Override
    public int findOrCreateFileTypeId(UUID uuid, String s, List<String> list) throws Exception {
        return 0;
    }

    @Override
    public Integer findFileAudit(UUID uuid, UUID uuid1, UUID uuid2, int i, String s) throws Exception {
        return null;
    }

    @Override
    public int auditFile(UUID uuid, UUID uuid1, UUID uuid2, int i, String s) throws Exception {
        return 0;
    }

    @Override
    public Long findRecordAuditIdForRow(UUID uuid, int i, int i1) throws Exception {
        return null;
    }

    @Override
    public SourceFileRecord findSourceFileRecordRow(UUID uuid, long l) throws Exception {
        return null;
    }

    @Override
    public void auditFileRow(UUID uuid, SourceFileRecord sourceFileRecord) throws Exception {

    }

    @Override
    public void auditFileRows(UUID uuid, List<SourceFileRecord> list) throws Exception {

    }

    @Override
    public void saveResourceMappings(ResourceWrapper resourceWrapper, ResourceFieldMappingAudit resourceFieldMappingAudit) throws Exception {

    }

    @Override
    public void saveResourceMappings(Map<ResourceWrapper, ResourceFieldMappingAudit> map) throws Exception {

    }



    @Override
    public List<ResourceFieldMapping> findFieldMappings(UUID uuid, ResourceType resourceType, UUID uuid1) throws Exception {
        return null;
    }

    @Override
    public List<ResourceFieldMapping> findFieldMappingsForField(UUID serviceId, ResourceType resourceType, UUID resourceId, String field) throws Exception {
        if (exception != null)
            throw exception;

        List<ResourceFieldMapping> fieldMappings = new ArrayList<>();

        if (LEAF_WITH_AUDIT.equals(field)) {
            fieldMappings.add(new ResourceFieldMapping().setSourceLocation(LEAF_WITH_AUDIT));
        }

        if (LEAF_WITH_PARENT_AND_CHILD_AUDIT.equals(field))
            fieldMappings.add(new ResourceFieldMapping().setSourceLocation(LEAF_WITH_PARENT_AND_CHILD_AUDIT));

        return fieldMappings;
    }
}
