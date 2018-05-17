import {Component, OnInit} from '@angular/core';
import {ResourcesService} from './resources.service';
import {PersonFindDialogComponent} from '../person-find/person-find-dialog/person-find-dialog.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Service} from '../models/Service';
import {Person} from '../models/Person';
import {ResourceType} from '../models/ResourceType';
import {Patient} from '../models/Patient';
import {LoggerService} from 'eds-angular4';
import {ViewerComponent} from './viewer/viewer.component';
import {ServicePatientResource} from '../models/Resource';
import { DateHelper} from '../helpers/date.helper';
import {ResourceId} from '../models/ResourceId';

@Component({
  selector: 'app-resources-component',
  templateUrl: './resources.component.html',
  styleUrls: ['./resources.component.css']
})
export class ResourcesComponent implements OnInit {

  private resourceMap: any;
  private serviceMap: any;

  protected resourceTypes: ResourceType[] = [];
  protected resourceFilter: string[];

  public person: Person;
  protected patients: Patient[] = [];
  protected patientFilter: ResourceId[];

  protected patientResourceList: ServicePatientResource[] = [];
  protected clinicalResourceList: ServicePatientResource[] = [];

  protected highlight: ServicePatientResource;
  protected lastHighlight: ServicePatientResource;

  constructor(protected logger: LoggerService,
              protected modal: NgbModal,
              protected resourceService: ResourcesService) {
    this.getResourceTypes();
  }

  ngOnInit() {
  }

  private invalidSelection(): boolean {
    return this.patientFilter == null || this.patientFilter.length === 0 || this.resourceFilter == null || this.resourceFilter.length === 0;
  }

  /** RESOURCE TYPES **/
  private getResourceTypes(): void {
    const vm = this;
    vm.resourceService.getResourceTypes()
      .subscribe(
        (result) => vm.processResourceTypes(result),
        (error) => vm.logger.error(error)
      );
  }

  private processResourceTypes(resourceTypes: ResourceType[]) {
    this.resourceTypes = resourceTypes;
    this.resourceMap = {};
    for (const resourceType of resourceTypes)
      this.resourceMap[resourceType.id] = resourceType.name;
  }

  /** PERSON FIND **/
  public findPerson() {
    PersonFindDialogComponent.open(this.modal)
      .result.then(
      (result) => this.loadPerson(result),
      (error) => this.logger.error(error)
    );
  }

  /** INITIAL DATA LOAD **/
  private loadPerson(person: Person) {
    const vm = this;
    this.patientResourceList = [];
    this.clinicalResourceList = [];
    this.patients = [];
    this.patientFilter = [];

    vm.person = person;
    if (this.person.nhsNumber && this.person.nhsNumber !== '')
    vm.resourceService.getPatients(person.nhsNumber)
      .subscribe(
        (result) => vm.processPatients(result),
        (error) => vm.logger.error(error)
      );
    else
      vm.resourceService.getPatient(person.patientId.serviceId, person.patientId.systemId, this.person.patientId.patientId)
        .subscribe(
          (result) => vm.processPatients([result]),
          (error) => vm.logger.error(error)
        );

  }

  private processPatients(patients: Patient[]) {
    this.patients = [];

    if (!patients)
      return;

    this.patients = patients;

    this.serviceMap = {};

    for (const patient of patients) {
      if (!this.serviceMap[patient.id.serviceId]) {
        const service: Service = new Service();
        service.id = patient.id.serviceId;
        this.serviceMap[service.id] = service;
        this.loadServiceName(service);
      }

      this.setPatientTooltip(patient);

      // Auto-select all patients
      this.patientFilter.push(patient.id);
    }
  }

  /** SERVICES **/
  private loadServiceName(service: Service) {
    if (service.name != null && service.name !== '')
      return;

    service.name = 'Loading...';

    const vm = this;
    vm.resourceService.getServiceName(service.id)
      .subscribe(
        (result) => vm.processServiceName(service, result),
        (error) => { vm.logger.log(error); service.name = 'Error!'; }
      );
  }

  private processServiceName(service: Service, name: string) {
    service.name = (name == null) ? 'Not Known' : name;

    for (const patient of this.patients) {
      if (patient.id.serviceId === service.id) {
        patient.serviceName = service.name;
        this.updatePatientDisplayName(patient);
      }
    }
  }

  private getServiceName(resource: ServicePatientResource): string {
    const service: Service = this.serviceMap[resource.serviceId];

    if (service)
      return service.name;

    return 'Not Known';
  }

  private getLocalIds(patient: ServicePatientResource): string {
    if (!patient.resourceJson.identifier || patient.resourceJson.identifier.length === 0)
      return '';

    const localIds: string[] = [];
    for (const localId of patient.resourceJson.identifier) {
      localIds.push(localId.value);
    }

    return localIds.join();
  }

  private updatePatientDisplayName(patient: Patient) {
    patient.name = patient.patientName + ' @ ' + patient.serviceName;
  }

  /** RESOURCE DATA LOAD **/
  private loadResources(): void {
    if (this.patientFilter == null || this.patientFilter.length === 0) {
      this.logger.error('Select at least one patient');
      return;
    }

    if (this.resourceFilter == null || this.resourceFilter.length === 0) {
      this.logger.error('Select at least one Resource type');
      return;
    }

    if (!this.patientFilter || this.patientFilter.length === 0)
      return;

    this.patientResourceList = null;
    this.clinicalResourceList = null;

    const vm = this;
    vm.resourceService.getResources(this.patientFilter, ['Patient'])
      .subscribe(
        (result) => vm.patientResourceList = result,
        (error) => vm.logger.error(error)
      );

    vm.resourceService.getResources(this.patientFilter, this.resourceFilter)
      .subscribe(
        (result) => {
          vm.clinicalResourceList = vm.sortResources(result);
        },
        (error) => vm.logger.error(error)
      );
  }

  /** RECORDED DATE FUNCTIONS **/
  private getRecordedDate(resource: ServicePatientResource): Date {
    if (resource.recordedDate)
      return resource.recordedDate;

    switch (resource.resourceJson.resourceType) {
      case 'Condition': return resource.recordedDate = DateHelper.parse(resource.resourceJson.dateRecorded);
      case 'AllergyIntolerance': return resource.recordedDate = DateHelper.parse(resource.resourceJson.recordedDate);
      case 'DiagnosticOrder': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'DiagnosticReport': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'ProcedureRequest': return resource.recordedDate = DateHelper.parse(resource.resourceJson.orderedOn);
      case 'Encounter': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'EpisodeOfCare': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);       // Period.start!?
      case 'FamilyMemberHistory': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'Immunization': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'MedicationOrder': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'MedicationStatement': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'Medication': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'Observation': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'Procedure': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'ReferralRequest': return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      case 'Specimen':  return resource.recordedDate = this.getRecordedDateExtension(resource.resourceJson);
      default: return resource.recordedDate = DateHelper.NOT_KNOWN;
    }
  }

  private getRecordedDateExtension(resource: any): Date {
    const RECORDED_DATE = 'http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-date-extension';

    if (!resource || !resource.extension)
      return DateHelper.NOT_KNOWN;

    const extension = resource.extension.find((e) => e.url === RECORDED_DATE);

    if (!extension)
      return DateHelper.NOT_KNOWN;

    return DateHelper.parse(extension.valueDateTime);
  }

  /** EFFECTIVE DATE FUNCTIONS **/
  private getDate(resource: ServicePatientResource): Date {
    if (resource.effectiveDate)
      return resource.effectiveDate;

    switch (resource.resourceJson.resourceType) {
      case 'Patient': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.birthDate);
      case 'AllergyIntolerance': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.onset);
      case 'Condition': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.onsetDateTime);
      case 'DiagnosticOrder': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.event ? resource.resourceJson.event.dateTime : null);
      case 'DiagnosticReport': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.effectiveDateTime);
      case 'ProcedureRequest': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.scheduledDateTime);
      case 'Encounter': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.period ? resource.resourceJson.period.start : null);
      case 'EpisodeOfCare': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.period ? resource.resourceJson.period.start : null);
      case 'FamilyMemberHistory': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.date);
      case 'Immunization': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.date);
      case 'MedicationOrder': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.dateWritten);
      case 'MedicationStatement': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.dateAsserted);
      case 'Medication': return resource.effectiveDate = DateHelper.NOT_KNOWN;
      case 'Observation': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.effectiveDateTime);
      case 'Procedure': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.performedDateTime);
      case 'ReferralRequest': return resource.effectiveDate = DateHelper.parse(resource.resourceJson.date);
      case 'Specimen':  return resource.effectiveDate = DateHelper.parse(resource.resourceJson.collection ? resource.resourceJson.collection.collectedDateTime : null);
      default: return resource.effectiveDate = DateHelper.NOT_KNOWN;
    }
  }

  /** DESCRIPTION FUNCTIONS **/
  private getDescription(resource: ServicePatientResource): string {
    if (resource.description)
      return resource.description;

    return resource.description = this.generateDescription(resource);
  }

  private generateDescription(resource: ServicePatientResource) : string {
    switch (resource.resourceJson.resourceType) {
      case 'AllergyIntolerance':
        return this.getCodeTerm(resource.resourceJson.substance);
      case 'Condition':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'DiagnosticOrder':
        return this.getCodeTerm((resource.resourceJson.item && resource.resourceJson.item.length > 0) ? resource.resourceJson.item[0].code : null);
      case 'DiagnosticReport':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'ProcedureRequest':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'Encounter':
        return this.getCodeTerm((resource.resourceJson.reason && resource.resourceJson.reason.length > 0) ? resource.resourceJson.reason[0] : null);
      case 'EpisodeOfCare':
        return this.getCodeTerm(resource.resourceJson.type);
      case 'FamilyMemberHistory':
        return this.getCodeTerm((resource.resourceJson.condition && resource.resourceJson.condition.length > 0) ? resource.resourceJson.condition[0].code : null);
      case 'Immunization':
        return this.getCodeTerm(resource.resourceJson.vaccineCode);
      case 'MedicationOrder':
        return this.getCodeTerm((resource.resourceJson.medicationCodeableConcept) ? resource.resourceJson.medicationCodeableConcept : null);
      case 'MedicationStatement':
        return this.getMedicationStatementDescription(resource);
      case 'Medication':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'Observation':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'Procedure':
        return this.getCodeTerm(resource.resourceJson.code);
      case 'ReferralRequest':
        return this.getCodeTerm((resource.resourceJson.serviceRequested && resource.resourceJson.serviceRequested.length > 0) ? resource.resourceJson.serviceRequested[0] : null);
      case 'Specimen':
        return this.getCodeTerm(resource.resourceJson.type);
      default: return null;
    }
  }

  private getCodeTerm(code: any): string {
    if (code == null)
      return null;

    if (code.text != null)
      return code.text;

    if (code.coding)
      return code.coding[0].display;

    return null;
  }

  private getMedicationStatementDescription(resource: ServicePatientResource) {
    let description = '';

    if (resource.resourceJson.extension) {
      for(let extension of resource.resourceJson.extension) {
        if (extension.url === 'http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-type-extension')
          description = '(' + extension.valueCoding.display + ') ';
      }
    }

    description += this.getCodeTerm((resource.resourceJson.medicationCodeableConcept) ? resource.resourceJson.medicationCodeableConcept : null);
    return description;
  }

  /** BASIC LOOKUPS **/
  private getPatientName(resource: ServicePatientResource): string {
    if (resource.resourceJson.name && resource.resourceJson.name.length > 0) {
      const name = resource.resourceJson.name[0];

      if (name.text && name.text !== '')
        return name.text;

      const surnames = (name.family == null) ? '' : name.family.join(' ') + ', ';
      const forenames = (name.given == null) ? '' : name.given.join(' ');
      const title = (name.title == null) ? '' : '(' + name.title.join(' ') + ')';

      return surnames + forenames + title;
    }

    return 'Not known';
  }

  /** MAP LOOKUPS **/
  private getResourceName(resource: ServicePatientResource): string {
    const resourceName: string = this.resourceMap[resource.resourceJson.resourceType];
    if (resourceName && resourceName !== '')
      return resourceName;

    return resource.resourceJson.resourceType;
  }

  private viewResource(resource: ServicePatientResource) {
    ViewerComponent.open(this.modal, this.getResourceName(resource), resource);
  }

  private sortResources(array) {
    const len = array.length;
    if (len < 2) {
      return array;
    }
    const pivot = Math.ceil(len / 2);
    return this.mergeResources(this.sortResources(array.slice(0, pivot)), this.sortResources(array.slice(pivot)));
  }

  private mergeResources(left, right) {
    let result = [];
    while ((left.length > 0) && (right.length > 0)) {
      const leftRecorded = this.dateValue(this.getRecordedDate(left[0]));
      const rightRecorded = this.dateValue(this.getRecordedDate(right[0]));

      if (leftRecorded == rightRecorded) {
        if (this.getDate(left[0]) > this.getDate(right[0])) {
          result.push(left.shift());
        } else {
          result.push(right.shift());
        }
      } else if (leftRecorded > rightRecorded) {
        result.push(left.shift());
      } else {
        result.push(right.shift());
      }
    }

    result = result.concat(left, right);
    return result;
  }

  private dateValue(date: Date) : number {
    if (date == null)
      return -1;

    return date.valueOf();
  }

  private setPatientTooltip(patient: any) {
    patient.tooltip = 'Local ID(s)';
    patient.tooltipKvp = patient.localIds;
  }
}
