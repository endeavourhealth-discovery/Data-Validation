package org.endeavourhealth.dataassurance.logic;

import org.endeavourhealth.dataassurance.logic.mocks.Mock_PersonPatientDAL;
import org.endeavourhealth.dataassurance.models.Patient;
import org.endeavourhealth.dataassurance.models.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PersonPatientLogicTest {
    private PersonPatientLogic personPatient;
    private Mock_PersonPatientDAL mockDal;
    @Before
    public void setup() {
        mockDal = new Mock_PersonPatientDAL();
        PersonPatientLogic.dal = mockDal;
        personPatient = new PersonPatientLogic();
    }

    @Test
    public void findPersonsInOrganisationsNULLOrg() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(null, null);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNULL() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, null);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNoTerm() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, "");

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNhsNumberPresent() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.nhsNumberPresent);

        Assert.assertNotNull(persons);
        Assert.assertEquals(1, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertTrue(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNhsNumberMissing() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.nhsNumberMissing);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertTrue(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsEmisNumberPresent() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.emisNumberPresent);

        Assert.assertNotNull(persons);
        Assert.assertEquals(1, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertTrue(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsEmisNumberMissing() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.emisNumberMissing);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertTrue(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsDOBPresent() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.dobPresent);

        Assert.assertNotNull(persons);
        Assert.assertEquals(1, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertTrue(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsDOBMissing() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.dobMissing);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertTrue(mockDal.searchByDateOfBirthCalled);
    }


    @Test
    public void findPersonsInOrganisationsNamePresent() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.namePresent);

        Assert.assertNotNull(persons);
        Assert.assertEquals(1, persons.size());
        Assert.assertTrue(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNameMultiPresent() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.nameMultiPresent);

        Assert.assertNotNull(persons);
        Assert.assertEquals(2, persons.size());
        Assert.assertTrue(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNameMissing() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.nameMissing);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertTrue(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertFalse(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void findPersonsInOrganisationsNonNumericLocalId() throws Exception {
        List<Person> persons = personPatient.findPersonsInOrganisations(mockDal.organisationsPresent, mockDal.nonNumericLocalId);

        Assert.assertNotNull(persons);
        Assert.assertEquals(0, persons.size());
        Assert.assertFalse(mockDal.searchByNamesCalled);
        Assert.assertFalse(mockDal.searchByNhsNumberCalled);
        Assert.assertTrue(mockDal.searchByLocalIdCalled);
        Assert.assertFalse(mockDal.searchByDateOfBirthCalled);
    }

    @Test
    public void getPatient() throws Exception {
        Patient patient = personPatient.getPatient(mockDal.organisationsPresent, mockDal.organisationMissing, null, null);
        Assert.assertNull(patient);
        Assert.assertFalse(mockDal.getPatientCalled);
    }
}

