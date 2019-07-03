package org.endeavourhealth.dataassurance.logic;

import org.endeavourhealth.core.database.dal.publisherTransform.models.ResourceFieldMapping;
import org.endeavourhealth.dataassurance.logic.mocks.Mock_FileMappingDAL;
import org.endeavourhealth.dataassurance.logic.mocks.Mock_ResourceDAL;
import org.endeavourhealth.dataassurance.models.PatientResource;
import org.endeavourhealth.dataassurance.models.ResourceId;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.terminologies.ITerminologyServices;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

public class ResourceLogicTest {
    private Mock_ResourceDAL resourceDAL;
    private Mock_FileMappingDAL fileMappingDAL;
    private ResourceLogic resourceLogic;

    private String service1 = UUID.randomUUID().toString();
    private String service2 = UUID.randomUUID().toString();
    private String service3 = UUID.randomUUID().toString();
    private String service4 = UUID.randomUUID().toString();

    private Set<String> serviceIds = new HashSet<>(Arrays.asList(service1, service2, service3));
    private List<String> resourceTypes = new ArrayList<>(Arrays.asList("R1","R2"));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        resourceDAL = new Mock_ResourceDAL();
        ResourceLogic.dal = resourceDAL;
        fileMappingDAL = new Mock_FileMappingDAL();
        ResourceLogic.fileMappingDal = fileMappingDAL;
        resourceLogic = new ResourceLogic();
    }

    // getServicePatient
    @Test
    public void getServicePatientResourcesNullServiceList() throws Exception {
        List<ResourceId> patients = new ArrayList<>();

        patients.add(new ResourceId().setServiceId(service1).setSystemId("C1").setPatientId("P1"));
        patients.add(new ResourceId().setServiceId(service2).setSystemId("C1").setPatientId("P2"));

        List<PatientResource> actual = resourceLogic.getPatientResources(null, patients, resourceTypes);

        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void getServicePatientResourcesEmptyServiceList() throws Exception {
        List<ResourceId> patients = new ArrayList<>();

        patients.add(new ResourceId().setServiceId(service1).setSystemId("C1").setPatientId("P1"));
        patients.add(new ResourceId().setServiceId(service2).setSystemId("C1").setPatientId("P2"));

        List<PatientResource> actual = resourceLogic.getPatientResources(new HashSet<>(), patients, resourceTypes);

        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void getServicePatientResourcesValidService() throws Exception {
        List<ResourceId> patients = new ArrayList<>();

        patients.add(new ResourceId()
            .setServiceId(service1)
            .setSystemId(UUID.randomUUID().toString())
            .setPatientId(UUID.randomUUID().toString()));
        patients.add(new ResourceId()
            .setServiceId(service2)
            .setSystemId(UUID.randomUUID().toString())
            .setPatientId(UUID.randomUUID().toString()));

        List<PatientResource> actual = resourceLogic.getPatientResources(serviceIds, patients, resourceTypes);

        Assert.assertEquals(2, actual.size());
    }

    @Test
    public void getServicePatientResourcesInvalidService() throws Exception {
        List<ResourceId> patients = new ArrayList<>();

        patients.add(new ResourceId().setServiceId(service4).setSystemId("C1").setPatientId("P1"));
        patients.add(new ResourceId().setServiceId(service4).setSystemId("C1").setPatientId("P2"));

        List<PatientResource> actual = resourceLogic.getPatientResources(serviceIds, patients, resourceTypes);

        Assert.assertEquals(0, actual.size());
    }

    // getReferenceDescription
    @Test
    public void getReferenceDescriptionNull() {
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), null);
        Assert.assertNull(actual);
    }

    @Test
    public void getReferenceDescriptionEmpty() {
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "");
        Assert.assertNull(actual);
    }

    @Test
    public void getReferenceDescriptionInvalidReference() {
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "INVALID_REFERENCE");
        Assert.assertEquals("Invalid reference", actual);
    }

    @Test
    public void getReferenceDescriptionInvalidReferenceType() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No enum constant org.hl7.fhir.instance.model.ResourceType.INVALID_REFERENCE_TYPE");
        resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "INVALID_REFERENCE_TYPE/" + UUID.randomUUID().toString());
        Assert.fail();
    }

    @Test
    public void getReferenceDescriptionInvalidReferenceId() {
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Observation/INVALID_REFERENCE_ID");
        Assert.assertEquals("Invalid reference id", actual);
    }

    @Test
    public void getReferenceDescriptionMissingReference() {
        resourceDAL.resource = null;
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Observation/" + UUID.randomUUID().toString());
        Assert.assertEquals("Not found", actual);
    }

    @Test
    public void getReferenceDescriptionUnknownReference() {
        resourceDAL.resource = new TestScript();
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "TestScript/" + UUID.randomUUID().toString());
        Assert.assertEquals("Unknown type", actual);
    }

    @Test
    public void getReferenceDescriptionOrganisationReference() {
        resourceDAL.resource = new Organization().setName("Test Org");
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Organization/" + UUID.randomUUID().toString());
        Assert.assertEquals("Test Org", actual);
    }

    @Test
    public void getReferenceDescriptionPractitionerReferenceTextOnly() {
        resourceDAL.resource = new Practitioner().setName(
            new HumanName()
                .setText("Test Practitioner")
        );
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Practitioner/" + UUID.randomUUID().toString());
        Assert.assertEquals("Test Practitioner", actual);
    }

    @Test
    public void getReferenceDescriptionPractitionerReferenceFullName() {
        resourceDAL.resource = new Practitioner().setName(
            new HumanName()
                .addPrefix("mr")
                .addGiven("mock")
                .addFamily("mockery")
        );
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Practitioner/" + UUID.randomUUID().toString());
        Assert.assertEquals("MOCKERY, Mock (Mr)", actual);
    }

    @Test
    public void getReferenceDescriptionLocationReference() {
        resourceDAL.resource = new Location().setName("Test Location");
        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Location/" + UUID.randomUUID().toString());
        Assert.assertEquals("Test Location", actual);
    }


    @Test
    public void getReferenceDescriptionObservationReferenceCodeAndValueDisplay() {
        resourceDAL.resource = new Observation()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setDisplay("Mock Obs Code")
                    )
            )
            .setValue(
                new Quantity().setValue(new BigDecimal(100)).setUnit("MUs")
            )
            .addComponent(
                new Observation.ObservationComponentComponent()
                    .setValue(
                        new Quantity().setValue(new BigDecimal(200)).setUnit("MCU1")
                    )
            )
            .addComponent(
                new Observation.ObservationComponentComponent()
                    .setValue(
                        new Quantity().setValue(new BigDecimal(300)).setUnit("MCU2")
                    )
            )
            .setComments("Mock Comments");

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Observation/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Obs Code 100 MUs, BP 200 / 300 (Mock Comments)", actual);
    }

    @Test
    public void getReferenceDescriptionObservationReferenceException() {
        resourceDAL.resource = new Observation()
            .setValue(
                new StringType().setValue("Mock value")
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Observation/" + UUID.randomUUID().toString());
        Assert.assertEquals("Error resolving Observation reference", actual);
    }

    @Test
    public void getReferenceDescriptionConditionReference() {
        resourceDAL.resource = new Condition().setCode(
            new CodeableConcept()
                .addCoding(
                    new Coding().setDisplay("Mock Condition Code")
                )
        )
        .setNotes("Mock notes");

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Condition/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Condition Code (Mock notes)", actual);
    }

    @Test
    public void getReferenceDescriptionProcedureReference() {
        resourceDAL.resource = new Procedure().setCode(
            new CodeableConcept()
                .addCoding(
                    new Coding().setDisplay("Mock Procedure Code")
                )
            )
            .addNotes(
                new Annotation().setText("Mock Annotation")
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Procedure/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Procedure Code (Mock Annotation)", actual);
    }

    @Test
    public void getReferenceDescriptionImmunizationReference() {
        resourceDAL.resource = new Immunization().setVaccineCode(
            new CodeableConcept()
                .addCoding(
                    new Coding().setDisplay("Mock Immunization Code")
                )
            )
            .addNote(
                new Annotation().setText("Mock Annotation")
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "Immunization/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Immunization Code (Mock Annotation)", actual);
    }

    @Test
    public void getReferenceDescriptionFamilyHistoryReference() {
        resourceDAL.resource = new FamilyMemberHistory()
            .addCondition(
                new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
                .setCode(
                    new CodeableConcept()
                        .addCoding(
                            new Coding().setDisplay("Mock FamilyHistory Code")
                        )
                )
            )
            .setNote(
                new Annotation().setText("Mock Annotation")
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "FamilyMemberHistory/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock FamilyHistory Code (Mock Annotation)", actual);
    }

    @Test
    public void getReferenceDescriptionMedicationStatementReference() {
        resourceDAL.resource = new MedicationStatement()
            .setMedication(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setDisplay("Mock MedicationStatement Code")
                    )
            )
            .addDosage(
                new MedicationStatement.MedicationStatementDosageComponent()
                .setText("Mock Dosage")
            )
            .setNote("Mock Note");

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "MedicationStatement/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock MedicationStatement Code Mock Dosage (Mock Note)", actual);
    }

    @Test
    public void getReferenceDescriptionMedicationOrderReference() {
        resourceDAL.resource = new MedicationOrder()
            .setMedication(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setDisplay("Mock MedicationOrder Code")
                    )
            )
            .addDosageInstruction(
                new MedicationOrder.MedicationOrderDosageInstructionComponent()
                    .setText("Mock Dosage")
            )
            .setNote("Mock Note");

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "MedicationOrder/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock MedicationOrder Code Mock Dosage (Mock Note)", actual);
    }

    @Test
    public void getReferenceDescriptionAllergyReference() {
        resourceDAL.resource = new AllergyIntolerance()
            .setSubstance(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setDisplay("Mock Allergy Code")
                    )
            )
            .setNote(
                new Annotation().setText("Mock Annotation")
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "AllergyIntolerance/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Allergy Code (Mock Annotation)", actual);
    }

    @Test
    public void getReferenceDescriptionReferralRequestNoText() {
        resourceDAL.resource = new ReferralRequest();

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "ReferralRequest/" + UUID.randomUUID().toString());
        Assert.assertEquals("", actual);
    }

    @Test
    public void getReferenceDescriptionReferralRequestServiceText() {
        resourceDAL.resource = new ReferralRequest()
        .addServiceRequested(
            new CodeableConcept().setText("Mock Service Text")
        );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "ReferralRequest/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Service Text", actual);
    }

    @Test
    public void getReferenceDescriptionReferralRequestCodingText() {
        resourceDAL.resource = new ReferralRequest()
            .addServiceRequested(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setDisplay("Mock Service Coding")
                    )
            );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "ReferralRequest/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Service Coding", actual);
    }

    @Test
    public void getReferenceDescriptionReferralRequestCodeText() {
        resourceDAL.resource = new ReferralRequest();

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "ReferralRequest/" + UUID.randomUUID().toString());
        Assert.assertEquals("", actual);
    }

    // getResourceMappingForField
    @Test
    public void getResourceMappingForFieldNullField() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), null);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(0, mapping.size());
    }

    @Test
    public void getResourceMappingForFieldEmptyField() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), "");
        Assert.assertNotNull(mapping);
        Assert.assertEquals(0, mapping.size());
    }

    @Test
    public void getResourceMappingForFieldLeafNoAudit() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), Mock_FileMappingDAL.LEAF_NO_AUDIT);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(0, mapping.size());
    }

    @Test
    public void getResourceMappingForFieldLeafWithAudit() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), Mock_FileMappingDAL.LEAF_WITH_AUDIT);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(mapping.get(0).getSourceLocation(), Mock_FileMappingDAL.LEAF_WITH_AUDIT);
    }

    @Test
    public void getResourceMappingForFieldLeafParentAudit() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), Mock_FileMappingDAL.LEAF_WITH_PARENT_AUDIT);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(mapping.get(0).getSourceLocation(), Mock_FileMappingDAL.LEAF_WITH_AUDIT);
    }

    @Test
    public void getResourceMappingForFieldLeafParentAndChildAudit() {
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), Mock_FileMappingDAL.LEAF_WITH_PARENT_AND_CHILD_AUDIT);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(mapping.get(0).getSourceLocation(), Mock_FileMappingDAL.LEAF_WITH_PARENT_AND_CHILD_AUDIT);
    }

    @Test
    public void getResourceMappingForFieldDALException() {
        fileMappingDAL.exception = new SQLException("Test exception");
        List<ResourceFieldMapping> mapping = resourceLogic.getResourceMappingsForField(UUID.randomUUID().toString(), "Observation", UUID.randomUUID().toString(), Mock_FileMappingDAL.LEAF_WITH_PARENT_AND_CHILD_AUDIT);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(0, mapping.size());
    }
    @Test
    public void getReferenceDescriptionQuestionnaireResponse() {
        resourceDAL.resource = new QuestionnaireResponse();
        QuestionnaireResponse qr = new QuestionnaireResponse();


        QuestionnaireResponse.GroupComponent comp = new QuestionnaireResponse.GroupComponent();
        comp.setTitle("Title1");

        QuestionnaireResponse.QuestionComponent qc = new QuestionnaireResponse.QuestionComponent();
        QuestionnaireResponse.QuestionAnswerComponent qac = new QuestionnaireResponse.QuestionAnswerComponent();
 //       qac.setValue(BooleanType.class);
//        qc.addAnswer()
//
//        comp.addQuestion();
//
//
//
//
//
//                .addServiceRequested(
//                        new CodeableConcept()
//                                .addCoding(
//                                        new Coding().setDisplay("Mock Service Coding")
//                                )
//                );

        String actual = resourceLogic.getReferenceDescription(UUID.randomUUID().toString(), "ReferralRequest/" + UUID.randomUUID().toString());
        Assert.assertEquals("Mock Service Coding", actual);
    }

}
