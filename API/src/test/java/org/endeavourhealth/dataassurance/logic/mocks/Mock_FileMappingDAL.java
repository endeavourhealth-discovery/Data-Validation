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
