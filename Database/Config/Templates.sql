use config;

delete from config where config_id = 'Template-Patient' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Patient',
  '<div class="container-fluid">
 <form>
   <div class="row">
     <div class="col-md-6">
       <div *ngIf="resource.resourceJson.identifier">
			    <div *ngFor="let identifier of getActiveOnly(resource.resourceJson.identifier)">
				    <div class="form-group" *ngIf="identifier.system==''http://endeavourhealth.org/fhir/id/v2-local-patient-id/barts-mrn''" >
					    <label for="MRN">MRN number</label>
					    <input id="MRN" class="form-control" type="text" disabled value="{{identifier.value}} {{identifier.period ? (identifier.period | periodPipe) : ''''}} ">
				    </div>
				    <div class="form-group" *ngIf="identifier.system==''http://fhir.nhs.net/Id/nhs-number''" >
					    <label for="NHS">NHS number</label>
					    <input id="NHS" class="form-control" type="text" disabled value="{{identifier.value}} {{identifier.period ? (identifier.period | periodPipe) : ''''}} ">
				    </div>
				    <div class="form-group" *ngIf="identifier.system.endsWith(''patient-guid'')" >
					    <label for="ID">Local system number</label>
					    <input id="ID" class="form-control" type="text" disabled value="{{identifier.value}}">
				    </div>
				    <div class="form-group" *ngIf="identifier.system.endsWith(''patient-number'')" >
					    <label for="NHS">Local patient number</label>
					    <input id="NHS" class="form-control" type="text" disabled value="{{identifier.value}}">
				    </div>
			    </div>
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.name">
         <div *ngFor="let name of getActiveOnly(resource.resourceJson.name)">
           <div class="form-group">
             <label for="CuiName">Name</label>
             <input id="CuiName" class="form-control" type="text" disabled value="{{name | cuiNamePipe }} ({{ name.use }})">
           </div>
         </div>
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.maritalStatus">
         <label for="Marital">Marital status</label>
         <input id="Marital" class="form-control" type="text" disabled value="{{resource.resourceJson.maritalStatus.coding[0].display}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.address">
         <div *ngFor="let address of getActiveOnly(resource.resourceJson.address)">
          <label for="Address">Address ({{address.use}})</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let item of address.line">{{item}}</div>
              <div>{{address.city}}</div>
              <div>{{address.district}}</div>
              <div>{{address.postalCode}}</div>
            </div>
          </div>
         </div>
        </div>
      </div>
      <div class="col-md-6">
       <div class="form-group">
         <label for="Gender">Gender</label>
         <input id="Gender" class="form-control" type="text" disabled value="{{resource.resourceJson.gender}}">
       </div>
       <div class="form-group">
         <label for="DOB">Date of birth</label>
         <input id="DOB" class="form-control" type="text" disabled value="{{resource.resourceJson.birthDate | date:''dd/MM/y''}}">
       </div>
       <div *ngFor="let extension of resource.resourceJson.extension">
         <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-ethnic-category-extension''" >
           <label for="Ethnicity">Ethnicity</label>
           <input id="Ethnicity" class="form-control" type="text" disabled value="{{extension.valueCodeableConcept.coding[0].display}}">
         </div>
         <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-religion-category-extension''" >
           <label for="Religion">Religion</label>
           <input id="Religion" class="form-control" type="text" disabled value="{{extension.valueCodeableConcept.coding[0].display}}">
         </div>
       </div>
       <div *ngFor="let careProvider of resource.resourceJson.careProvider">
         <div class="form-group" *ngIf="careProvider.reference.startsWith(''Organization'')" >
           <label for="CarerOrg">Caring organisation</label>
           <input id="CarerOrg" class="form-control" type="text" disabled value="{{careProvider.display}}">
         </div>
         <div class="form-group" *ngIf="careProvider.reference.startsWith(''Practitioner'')" >
           <label for="CarerPrac">Usual GP</label>
           <input id="CarerPrac" class="form-control" type="text" disabled value="{{careProvider.display}}">
         </div>
       </div>
       <div class="form-group">
         <label for="ManagingOrg">Managing organisation</label>
         <input id="ManagingOrg" class="form-control" type="text" disabled value="{{resource.resourceJson.managingOrganization.display}}">
       </div>
       <div *ngFor="let telecom of resource.resourceJson.telecom">
         <div class="form-group">
           <label for="Telecom">{{telecom.system | titlecase}} ({{telecom.use}})</label>
           <input id="Telecom" class="form-control" type="text" disabled value="{{telecom.value}}">
         </div>
       </div>
       <div *ngIf="resource.resourceJson.deceasedDateTime" class="form-group">
         <label for="DOD">Date of death</label>
         <input id="DOD" class="form-control" type="text" disabled value="{{resource.resourceJson.deceasedDateTime | date:''dd/MM/y''}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.contained!=null">
          <label for="Additional">Additional extensions</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let containedItem of resource.resourceJson.contained">
	            <div *ngFor="let linkedParameterResource of containedItem.parameter">
					<div><i>Property:</i> {{linkedParameterResource.name}}  <i>Value:</i> {{linkedParameterResource.valueCodeableConcept.coding[0].code}} </div>
				</div>
              </div>
            </div>
          </div>
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.contact">
        <div *ngFor="let contact of getActiveOnly(resource.resourceJson.contact)">
          <h5>Contact Information</h5>
         <div *ngFor="let relationship of contact.relationship" class="form-group">
           <label for="relationshipType">Relationship Type</label>
           <input id="relationshipType" class="form-control" type="text" disabled value="{{relationship.text}}">
         </div>
         <div *ngIf="contact.period">
           <div class="form-group">
              <label for="ContactStartDate">Contact Start Date</label>
              <input id="ContactStartDate" class="form-control" type="text" disabled value="{{contact.period.start | date:''dd/MM/y''}}">
           </div>
           <div class="form-group">
              <label for="ContactEndDate">Contact End Date</label>
              <input id="ContactEndDate" class="form-control" type="text" disabled value="{{contact.period.end | date:''dd/MM/y''}}">
           </div>
         </div>
         <div *ngIf="contact.name">
           <div class="form-group">
             <label for="ContactTitle">Contact Title</label>
             <input id="ContactTitle" class="form-control" type="text" disabled value="{{contact.name.prefix}}">
           </div>
           <div class="form-group">
             <label for="ContactGiven">Contact Given Name</label>
             <input id="ContactGiven" class="form-control" type="text" disabled value="{{contact.name.given}}">
           </div>
           <div class="form-group">
             <label for="ContactFamily">Contact Family Name</label>
             <input id="ContactFamily" class="form-control" type="text" disabled value="{{contact.name.family}}">
           </div>
         </div>
         <div class="form-group" *ngIf="contact.address">
           <label for="ContactAddress">Contact Address ({{contact.address.use}})</label>
           <div class="form-control looks-disabled">
             <div class="scrollbox-100">
               <div *ngFor="let item of contact.address.line">{{item}}</div>
               <div>{{contact.address.city}}</div>
               <div>{{contact.address.district}}</div>
               <div>{{contact.address.postalCode}}</div>
             </div>
           </div>
         </div>
         <div *ngFor="let telecom of contact.telecom">
          <div class="form-group">
            <label for="ContactTelecom">{{telecom.system | titlecase}} ({{telecom.use}})</label>
            <input id="ContactTelecom" class="form-control" type="text" disabled value="{{telecom.value}}">
          </div>
         </div>
         </div>
        </div>
     </div>
   </div>
 </form>
 </div>');

delete from config where config_id = 'Template-EpisodeOfCare' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-EpisodeOfCare',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label for="Registered">Registered date</label>
        <input id="Registered" class="form-control" type="text" disabled value="{{resource.resourceJson.period?.start | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="CareManager">Care manager</label>
        <input id="CareManager" class="form-control" type="text" disabled value="{{resource.resourceJson.careManager?.display}}">
      </div>
       <div *ngFor="let extension of resource.resourceJson.extension">
         <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-patient-registration-type-extension''" >
            <label for="RegType">Registration type</label>
            <input id="RegType" class="form-control" type="text" disabled value="{{extension.valueCoding.display}}">
         </div>
         <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-episode-priority''" >
            <label for="Priority">Priority</label>
            <input id="Priority" class="form-control" type="text" disabled value="{{extension.valueString}}">
         </div>
         <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-episode-outcome''" >
            <label for="Outcome">Outcome</label>
            <input id="Outcome" class="form-control" type="text" disabled value="{{extension.valueString}}">
         </div>
       </div>
      <div *ngFor="let identifier of resource.resourceJson.identifier">
        <div class="form-group" *ngIf="identifier.system==''http://endeavourhealth.org/fhir/id/v2-local-episode-id/barts-fin''" >
          <label for="FIN">Financial number</label>
          <input id="FIN" class="form-control" type="text" disabled value="{{identifier.value}}">
        </div>
        <div class="form-group" *ngIf="identifier.system==''http://oneadvanced.com/identifier/adastra-case-no''" >
          <label for="CaseNo">Case No</label>
          <input id="CaseNo" class="form-control" type="text" disabled value="{{identifier.value}}">
        </div>
        <div class="form-group" *ngIf="identifier.system==''http://oneadvanced.com/identifier/adastra-case-tag''" >
          <label for="CaseTag">Case Tag</label>
          <input id="CaseTag" class="form-control" type="text" disabled value="{{identifier.value}}">
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group">
        <label for="Discharged">Discharged date</label>
        <input id="Discharged" class="form-control" type="text" disabled value="{{resource.resourceJson.period?.end  | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="CareOrg">Organisation</label>
        <input id="CareOrg" class="form-control" type="text" disabled value="{{resource.resourceJson.managingOrganization?.display}}">
      </div>
      <div class="form-group">
        <label for="Status">Status</label>
        <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-Condition' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Condition',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.code?.coding[0].display}}">
      </div>
      <div class="form-group">
        <label for="Date">Effective date</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.onsetDateTime | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="Recorder">Clinician</label>
        <input id="Recorder" class="form-control" type="text" disabled value="{{resource.resourceJson.asserter?.display}}">
      </div>
      <div *ngFor="let profile of resource.resourceJson.meta.profile">
        <div class="form-group" *ngIf="profile==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem''">
          <label for="Problem">Is a problem</label>
          <input id="Problem" class="form-control" type="text" disabled value="Yes">
        </div>
      </div>
      <div *ngFor="let extension of resource.resourceJson.extension">
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-review'' && extension.valueBoolean">
          <label for="Episodicity">Is a review</label>
          <input id="Episodicity" class="form-control" type="text" disabled value="Yes">
        </div>
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/is-primary-extension'' && extension.valueBoolean">
          <label for="IsPrimary">Is primary</label>
          <input id="IsPrimary" class="form-control" type="text" disabled value="Yes">
        </div>
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-condition-sequence-number-extension''">
          <label for="Sequence">Sequence number</label>
          <input id="Sequence" class="form-control" type="text" disabled value="{{extension.valueInteger}}">
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group">
        <label for="Code">Code</label>
        <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.code?.coding[0].code}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.category">
        <label for="Category">Category</label>
        <input id="Category" class="form-control" type="text" disabled value="{{resource.resourceJson.category.coding[0].code}}">
      </div>
      <div class="form-group">
        <label for="Text">Notes</label>
        <input id="Text" class="form-control" type="text" disabled value="{{resource.resourceJson.notes}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-Procedure' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Procedure',
  '<div class="container-fluid">
 <form>
   <div class="row">
     <div class="col-md-6">
       <div class="form-group">
         <label for="Display">Display term</label>
         <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].display}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.performer!=null">
         <label for="Performer">Performed by</label>
         <input id="Performer" class="form-control" type="text" disabled value="{{resource.resourceJson.performer[0].actor.display}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.performer==null">
         <label for="Performer">Performed by</label>
         <input id="Performer" class="form-control" type="text" disabled value="">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.performedPeriod!=null">
         <label for="PerformedAtStart">Procedure start</label>
         <input id="PerformedAt" class="form-control" type="text" disabled value="{{resource.resourceJson.performedPeriod.start| date:''dd/MM/y HH:mm:ss''}}">
         <label for="PerformedAtEnd">Procedure end</label>
         <input id="PerformedAt" class="form-control" type="text" disabled value="{{resource.resourceJson.performedPeriod.end| date:''dd/MM/y HH:mm:ss''}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.performedPeriod==null">
         <label for="Performer">Performed at</label>
         <input id="Performer" class="form-control" type="text" disabled value="">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.notes!=null">
         <label for="Notes">Notes</label>
         <input id="Notes" class="form-control" type="text" disabled value="{{resource.resourceJson.notes[0].text}}">
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.notes==null">
         <label for="Notes">Notes</label>
         <input id="Notes" class="form-control" type="text" disabled value="">
       </div>
       <div *ngFor="let extension of resource.resourceJson.extension">
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/is-primary-extension'' && extension.valueBoolean">
            <label for="IsPrimary">Is primary</label>
            <input id="IsPrimary" class="form-control" type="text" disabled value="Yes">
          </div>
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure-sequence-number-extension''">
            <label for="Sequence">Sequence number</label>
            <input id="Sequence" class="form-control" type="text" disabled value="{{extension.valueInteger}}">
          </div>
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-by-extension''">
            <label for="RecordedBy">Recorded by</label>
            <input id="RecordedBy" class="form-control" type="text" disabled value="{{extension.valueReference.display}}">
          </div>
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-date-extension''">
            <label for="RecordedDate">Recorded date</label>
            <input id="RecordedDate" class="form-control" type="text" disabled value="{{extension.valueDateTime | date:''dd/MM/y''}}">
          </div>
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-procedure-speciality-group-extension''">
            <label for="SpecialtyGroup">Specialty Group</label>
            <input id="SpecialtyGroup" class="form-control" type="text" disabled value="{{extension.valueString}}">
          </div>
       </div>
       <div class="form-group" *ngIf="resource.resourceJson.location!=null">
          <label for="Location">Location</label>
          <input id="Location" class="form-control" type="text" disabled value="{{resource.resourceJson.location.display}}">
       </div>
     </div>
     <div class="col-md-6">
       <div class="form-group">
         <label for="Code">Code</label>
         <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].code}}">
       </div>
       <div class="form-group">
         <label for="Date">Date</label>
         <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.performedDateTime | date:''dd/MM/y''}}">
       </div>
       <div class="form-group">
         <label for="Status">Status</label>
         <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
       </div>
     </div>
   </div>
 </form>
 </div>');


delete from config where config_id = 'Template-Observation' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Observation',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
        <div class="form-group" *ngIf="resource.resourceJson.code!=null">
            <label for="Display">Display term</label>
            <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].display}}">
        </div>
        <div class="form-group">
            <label for="Date">Effective date</label>
            <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.effectiveDateTime | date:''dd/MM/y''}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.valueQuantity!=null">
            <label for="Value">Value</label>
            <input id="Value" class="form-control" type="text" disabled value="{{resource.resourceJson.valueQuantity.comparator? resource.resourceJson.valueQuantity.comparator : ''''}}{{resource.resourceJson.valueQuantity.value}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.valueString!=null">
            <label for="ValueString">Value</label>
            <input id="ValueString" class="form-control" type="text" disabled value="{{resource.resourceJson.valueString}}">
        </div>
        <div *ngFor="let component of resource.resourceJson.component">
            <div class="form-group">
                <label for="code">Code</label>
                <input id="code" class="form-control" type="text" disabled value="{{component.code.coding[0].code}}">
            </div>
            <div class="form-group">
                <label for="display">Display term</label>
                <input id="display" class="form-control" type="text" disabled value="{{component.code.coding[0].display}}">
            </div>
            <div class="form-group">
                <label for="codeValue">Value</label>
                <input id="codeValue" class="form-control" type="text" disabled value="{{component.valueQuantity.value}}">
            </div>
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.referenceRange!=null">
          <label for="refRange">Range</label>
          <input id="refRange" class="form-control" type="text" disabled value="{{resource.resourceJson.referenceRange[0].low.value}} - {{resource.resourceJson.referenceRange[0].high.value}}">
        </div>
        <div class="form-group">
            <label for="Comments">Comments</label>
            <textarea id="Comments" class="form-control" rows=5 disabled value="{{resource.resourceJson.comments}}"></textarea>
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group" *ngIf="resource.resourceJson.code!=null">
            <label for="Code">Code</label>
            <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].code}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.performer!=null">
            <label for="Recorder">Recorded by</label>
            <input id="Recorder" class="form-control" type="text" disabled value="{{resource.resourceJson.performer[0].display}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.performer==null">
            <label for="Recorder">Recorded by</label>
            <input id="Recorder" class="form-control" type="text" disabled value="">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.valueQuantity!=null">
            <label for="Value1Units">Units</label>
            <input id="Value1Units" class="form-control" type="text" disabled value="{{resource.resourceJson.valueQuantity.unit}}">
        </div>
        <div *ngFor="let extension of resource.resourceJson.extension">
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-problem-review'' && extension.valueBoolean">
            <label for="Episodicity">Is a review</label>
            <input id="Episodicity" class="form-control" type="text" disabled value="Yes">
          </div>
		      <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-external-document-extension''">
            <label for="DOCID">Document Id</label>
            <input id="DOCID" class="form-control" type="text" disabled value="{{extension.valueIdentifier.value}}">
          </div>
        </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-AllergyIntolerance' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-AllergyIntolerance',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.substance.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.substance.coding[0].display}}">
      </div>
      <div class="form-group" *ngIf="!resource.resourceJson.substance.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.substance.text}}">
      </div>
      <div class="form-group">
        <label for="Date">Effective date</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.onset | date:''dd/MM/y''}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.reaction">
        <label for="Severity">Severity</label>
        <input id="Severity" class="form-control" type="text" disabled value="{{resource.resourceJson.reaction[0].severity}}">
      </div>
      <div class="form-group" >
        <label for="Text">Comments</label>
        <input id="Text" class="form-control" type="text" disabled value="{{resource.resourceJson.note?.text}}">
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.substance.coding">
        <label for="Code">Code</label>
        <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.substance.coding[0].code}}">
      </div>
      <div class="form-group">
        <label for="Recorder">Recorded by</label>
        <input id="Recorder" class="form-control" type="text" disabled value="{{resource.resourceJson.recorder?.display}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.reaction">
        <label for="Certainty">Certainty</label>
        <input id="Certainty" class="form-control" type="text" disabled value="{{resource.resourceJson.reaction[0].certainty}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-MedicationOrder' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-MedicationOrder',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.medicationCodeableConcept.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.coding[0].display}}">
      </div>
      <div class="form-group" *ngIf="!resource.resourceJson.medicationCodeableConcept.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.text}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.dosageInstruction!=null">
        <label for="Dosage">Dosage</label>
        <input id="Dosage" class="form-control" type="text" disabled value="{{resource.resourceJson.dosageInstruction[0].text}}">
      </div>
      <div class="form-group">
        <label for="Date">Issued</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.dateWritten | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="Text">Comments</label>
        <input id="Text" class="form-control" type="text" disabled value="{{resource.resourceJson.notes}}">
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.medicationCodeableConcept.coding">
        <label for="Code">Code</label>
        <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.coding[0].code}}">
      </div>
      <div class="form-group">
        <label for="Qty">Quantity</label>
        <input id="Qty" class="form-control" type="text" disabled value="{{resource.resourceJson.dispenseRequest?.quantity.value}} {{resource.resourceJson.dispenseRequest?.quantity.unit}}">
      </div>
       <div class="form-group">
        <label for="Prescriber">Prescriber</label>
        <input id="Prescriber" class="form-control" type="text" disabled value="{{resource.resourceJson.prescriber?.display}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-MedicationStatement' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-MedicationStatement',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.medicationCodeableConcept.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.coding[0].display}}">
      </div>
      <div class="form-group" *ngIf="!resource.resourceJson.medicationCodeableConcept.coding">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.text}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.dosage!=null">
        <label for="Dosage">Dosage</label>
        <input id="Dosage" class="form-control" type="text" disabled value="{{resource.resourceJson.dosage[0].text}}">
      </div>
      <div class="form-group">
        <label for="Date">Date</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.dateAsserted | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="Status">Status</label>
        <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
      </div>
        <div class="form-group">
        <label for="Text">Comments</label>
        <input id="Text" class="form-control" type="text" disabled value="{{resource.resourceJson.notes}}">
      </div>
    </div>
    <div class="col-md-6">
        <div class="form-group" *ngIf="resource.resourceJson.medicationCodeableConcept.coding!=null">
            <label for="Code">Code</label>
            <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.medicationCodeableConcept.coding[0].code}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.medicationCodeableConcept.coding==null">
            <label for="Code">Code</label>
            <input id="Code" class="form-control" type="text" disabled value="">
        </div>
        <div *ngFor="let extension of resource.resourceJson.extension">
            <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-quantity-extension''" >
                <label for="Qty">Quantity</label>
                <input id="Qty" class="form-control" type="text" disabled value="{{extension.valueQuantity.value}} {{extension.valueQuantity.unit}}">
            </div>
            <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-medication-authorisation-type-extension''">
                <label for="Type">Type</label>
                <input id="Type" class="form-control" type="text" disabled value="{{extension.valueCoding.display}}">
            </div>
        </div>
        <div class="form-group">
            <label for="Source">Source</label>
            <input id="Source" class="form-control" type="text" disabled value="{{resource.resourceJson.informationSource?.display}}">
        </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-Encounter' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Encounter',
  '<div class="container-fluid">
  <form>
    <div class="row">
      <div class="col-md-6">
        <div class="form-group">
          <label for="Date">Date</label>
          <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.period?.start | date:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.participant!=null">
          <label for="Practitioner">Practitioner</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let item of resource.resourceJson.participant">
                {{item.individual.display}} <span *ngIf="item.type && item.type[0].coding"> <i>({{item.type[0].coding[0].display}})</i></span>
              </div>
            </div>
          </div>
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.period !=null">
          <label for="Period">Period</label>
          <input id="Period" class="form-control" type="text" disabled value="{{resource.resourceJson.period | periodPipe:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.contained!=null">
          <label for="Linked">Linked resources</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let containedItem of resource.resourceJson.contained">
				<div *ngFor="let linkedListResource of containedItem.entry">
					<div>{{linkedListResource.item.display}}</div>
				</div>
              </div>
            </div>
          </div>
          <label for="Additional">Additional extensions</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let containedItem of resource.resourceJson.contained">
	            <div *ngFor="let linkedParameterResource of containedItem.parameter">
					<div><i>Property:</i> {{linkedParameterResource.name}}  <i>Value:</i> {{linkedParameterResource.valueCodeableConcept.coding[0].code}} </div>
				</div>
              </div>
            </div>
          </div>
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.type!=null">
          <label for="Place">Type history</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let item of resource.resourceJson.type">{{item.text}}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="form-group">
          <label for="Class">Class</label>
          <input id="Class" class="form-control" type="text" disabled value="{{resource.resourceJson.class}}">
        </div>
        <div class="form-group">
          <label for="Status">Status</label>
          <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
        </div>
        <div *ngFor="let extension of resource.resourceJson.extension">
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-encounter-source''" >
            <label for="Source">Source</label>
            <input id="Source" class="form-control" type="text" disabled value="{{extension.valueCodeableConcept.text}}">
          </div>
          <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-encounter-treatment-function-extension''" >
            <label for="Treatment">Treatment</label>
            <input id="Source" class="form-control" type="text" disabled value="{{extension.valueCodeableConcept.coding[0].display}}">
          </div>
        </div>
        <div class="form-group">
          <label for="Place">Place</label>
          <input id="Place" class="form-control" type="text" disabled value="{{resource.resourceJson.serviceProvider?.display}}">
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.reason!=null">
          <label for="Linked">Reason(s)</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let entry of resource.resourceJson.reason">
                <div>{{entry.text}}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="form-group" *ngIf="resource.resourceJson.location!=null">
          <label for="Linked">Location(s)</label>
          <div class="form-control looks-disabled">
            <div class="scrollbox-100">
              <div *ngFor="let entry of resource.resourceJson.location">
                <div>{{entry.location.display}}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </form>
</div>');

delete from config where config_id = 'Template-Immunization' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Immunization',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label for="Display">Display term</label>
        <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.vaccineCode?.coding[0].display}}">
      </div>
      <div class="form-group">
        <label for="Date">Effective date</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.date | date:''dd/MM/y''}}">
      </div>
      <div class="form-group">
        <label for="Route">Route</label>
        <input id="Route" class="form-control" type="text" disabled value="{{resource.resourceJson.route?.text}}">
      </div>
      <div class="form-group">
        <label for="Lot">Lot number</label>
        <input id="Lot" class="form-control" type="text" disabled value="{{resource.resourceJson.lotNumber}}">
      </div>
      <div class="form-group">
         <label for="Dosage">Dosage</label>
         <input id="Dosage" class="form-control" type="text" disabled value="{{resource.resourceJson.doseQuantity?.value}} {{resource.resourceJson.doseQuantity?.unit}}">
       </div>
	    <div class="form-group" *ngIf="resource.resourceJson.note!=null">
         <label for="Note">Note</label>
         <input id="Note" class="form-control" type="text" disabled value="{{resource.resourceJson.note[0].text}}">
      </div>
	    <div class="form-group" *ngIf="resource.resourceJson.vaccinationProtocol!=null">
         <label for="Protocol">Protocol</label>
         <input id="Protocol" class="form-control" type="text" disabled value="{{resource.resourceJson.vaccinationProtocol[0].series}} {{resource.resourceJson.vaccinationProtocol[0].doseSequence}} {{resource.resourceJson.vaccinationProtocol[0].description}}">
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group">
        <label for="Code">Code</label>
        <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.vaccineCode?.coding[0].code}}">
      </div>
      <div class="form-group">
        <label for="Performer">Performer</label>
        <input id="Performer" class="form-control" type="text" disabled value="{{resource.resourceJson.performer?.display}}">
      </div>
      <div class="form-group">
        <label for="Site">Site</label>
        <input id="Site" class="form-control" type="text" disabled value="{{resource.resourceJson.site?.text}}">
      </div>
      <div class="form-group">
        <label for="Status">Status</label>
        <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.explanation?.reason">
        <label for="Reason">Reason</label>
        <input id="Reason" class="form-control" type="text" disabled value="{{resource.resourceJson.explanation.reason[0].text}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-FamilyMemberHistory' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-FamilyMemberHistory',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
        <div class="form-group">
            <label for="Display">Display term</label>
            <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.condition[0].code.coding[0].display}}">
        </div>
        <div class="form-group">
            <label for="Relation">Relation</label>
            <input id="Relation" class="form-control" type="text" disabled value="{{resource.resourceJson.relationship.coding[0].display}}">
        </div>
        <div class="form-group" >
            <label for="Text">Comments</label>
            <input id="Text" class="form-control" type="text" disabled value="{{resource.resourceJson.note?.text}}">
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
            <label for="Code">Code</label>
            <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.condition[0].code.coding[0].code}}">
        </div>
        <div *ngFor="let extension of resource.resourceJson.extension">
            <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-family-member-history-reporter-extension''" >
                <label for="Reporter">Reported by</label>
                <input id="Reporter" class="form-control" type="text" disabled value="{{extension.valueReference.display}}">
            </div>
        </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-ReferralRequest' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-ReferralRequest',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label for="Date">Referral date</label>
        <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.date | date:''dd/MM/y''}}">
      </div>
      <div *ngFor="let extension of resource.resourceJson.extension">
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-referral-request-send-mode-extension''">
          <label for="RequestMode">Request mode</label>
          <input id="RequestMode" class="form-control" type="text" disabled value="{{extension.valueCodeableConcept.coding[0].display}}">
        </div>
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-by-extension''">
          <label for="RecordedBy">Recorded by</label>
          <input id="RecordedBy" class="form-control" type="text" disabled value="{{extension.valueReference.display}}">
        </div>
        <div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-date-extension''">
          <label for="RecordedDate">Recorded date</label>
          <input id="RecordedDate" class="form-control" type="text" disabled value="{{extension.valueDateTime | date:''dd/MM/y''}}">
        </div>
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.serviceRequested">
        <label for="Service">Service requested</label>
        <input id="Service" class="form-control" type="text" disabled value="{{resource.resourceJson.serviceRequested[0].text}}">
      </div>
    </div>
    <div class="col-md-6">
      <div class="form-group" *ngIf="resource.resourceJson.type">
        <label for="RequestType">Request type</label>
        <input id="RequestType" class="form-control" type="text" disabled value="{{resource.resourceJson.type.coding[0].display}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.priority">
        <label for="RequestPriority">Request priority</label>
        <input id="RequestPriority" class="form-control" type="text" disabled value="{{resource.resourceJson.priority.coding[0].display}}">
      </div>
      <div class="form-group">
        <label for="Requester">Requester</label>
        <input id="Requester" class="form-control" type="text" disabled value="{{resource.resourceJson.requester?.display}}">
      </div>
      <div class="form-group" *ngIf="resource.resourceJson.recipient">
        <label for="Recipient">Recipient</label>
        <input id="Recipient" class="form-control" type="text" disabled value="{{resource.resourceJson.recipient[0].display}}">
      </div>
      <div class="form-group">
        <label for="Status">Status</label>
        <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-ProcedureRequest' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-ProcedureRequest',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
        <div class="form-group">
            <label for="Display">Display term</label>
            <input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].display}}">
        </div>
        <div class="form-group">
            <label for="Requester">Requested by</label>
            <input id="Requester" class="form-control" type="text" disabled value="{{resource.resourceJson.orderer.display}}">
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
            <label for="Code">Code</label>
            <input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].code}}">
        </div>
        <div class="form-group">
            <label for="RequestDate">Requested date</label>
            <input id="RequestDate" class="form-control" type="text" disabled value="{{resource.resourceJson.orderedOn | date:''dd/MM/y''}}">
        </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-Flag' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Flag',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
        <div class="form-group">
            <label for="Start">Start date</label>
            <input id="Start" class="form-control" type="text" disabled value="{{resource.resourceJson.period?.start | date:''dd/MM/y''}}">
        </div>
        <div *ngIf="resource.resourceJson.extension">
			<div *ngFor="let extension of resource.resourceJson.extension">
				<div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-recorded-by-extension''">
					<label for="RecordedBy">Recorded by</label>
					<input id="RecordedBy" class="form-control" type="text" disabled value="{{extension.valueReference.display}}">
				</div>
			</div>
        </div>
        <div class="form-group">
            <label for="Info">Information</label>
            <textarea id="Info" class="form-control" rows="5" disabled value="{{resource.resourceJson.code.text}}"></textarea>
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
            <label for="Status">Status</label>
            <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
        </div>
        <div *ngFor="let identifier of getActiveOnly(resource.resourceJson.identifier)">
         <div class="form-group" *ngIf="identifier.system==''http://oneadvanced.com/identifier/adastra-case-no''" >
           <label for="CaseNo">Case No</label>
           <input id="CaseNo" class="form-control" type="text" disabled value="{{identifier.value}}">
         </div>
       </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-Appointment' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-Appointment',
  '<div class="container-fluid">
  <form>
    <div class="row">
      <div class="col-md-6">
        <div class="form-group">
          <label for="Status">Status</label>
          <input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
        </div>
        <div class="form-group">
          <label for="StartDate">Start Date</label>
          <input id="StartDate" class="form-control" type="text" disabled value="{{resource.resourceJson.start | date:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group">
          <label for="EndDate">End Date</label>
          <input id="EndDate" class="form-control" type="text" disabled value="{{resource.resourceJson.end | date:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group">
          <label for="Duration">Duration</label>
          <input id="Duration" class="form-control" type="text" disabled value="{{resource.resourceJson.minutesDuration}} mins">
        </div>
      </div>
      <div class="col-md-6">
        <div class="form-group" *ngIf="resource.resourceJson.slot">
          <label for="Slot">Slot</label>
          <div class="form-control looks-disabled">
            <div *ngFor="let item of resource.resourceJson.slot">{{item.display}} - {{item.reference}} <button type="button" class="btn btn-xs btn-info" (click)="viewResource(item.reference)" name="close">View</button></div>
          </div>
        </div>
        <div *ngFor="let participant of resource.resourceJson.participant">
         <div class="form-group" *ngIf="participant.actor.reference.startsWith(''Practitioner'')" >
           <label for="Practitioner">Practitioner</label>
           <input id="Practitioner" class="form-control" type="text" disabled value="{{participant.actor.display}}">
         </div>
        </div>
      </div>
    </div>
  </form>
</div>');

delete from config where config_id = 'Template-Slot' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
           'data-assurance',
           'Template-Slot',
           '<div class="container-fluid">
  <form>
    <div class="row">
      <div class="col-md-6">
        <div class="form-group" *ngIf="resource.resourceJson.schedule">
          <label for="Schedule">Schedule</label>
          <div class="form-control looks-disabled">
                    <div>{{resource.resourceJson.schedule.display}} - {{resource.resourceJson.schedule.reference}} <button type="button" class="btn btn-xs btn-info" (click)="viewResource(resource.resourceJson.schedule.reference)" name="close">View</button></div>
          </div>
        </div>
        <div class="form-group">
          <label for="FreeBusy">Free/Busy type</label>
          <input id="FreeBusy" class="form-control" type="text" disabled value="{{resource.resourceJson.freeBusyType}}">
        </div>
      </div>
      <div class="col-md-6">
        <div class="form-group">
          <label for="StartDate">Start Date</label>
          <input id="StartDate" class="form-control" type="text" disabled value="{{resource.resourceJson.start | date:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group">
          <label for="EndDate">End Date</label>
          <input id="EndDate" class="form-control" type="text" disabled value="{{resource.resourceJson.end | date:''dd/MM/y HH:mm:ss''}}">
        </div>
      </div>
    </div>
  </form>
</div>');

delete from config where config_id = 'Template-Schedule' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
           'data-assurance',
           'Template-Schedule',
           '<div class="container-fluid">
  <form>
    <div class="row">
      <div class="col-md-6">
        <div class="form-group">
          <label for="Comment">Comment</label>
          <input id="Comment" class="form-control" type="text" disabled value="{{resource.resourceJson.comment}}">
        </div>
        <div class="form-group">
          <label for="Type">Type</label>
          <div class="form-control looks-disabled">
                    <div *ngFor="let item of resource.resourceJson.type">{{item.text}}</div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="form-group">
          <label for="StartDate">Start Date</label>
          <input id="StartDate" class="form-control" type="text" disabled value="{{resource.resourceJson.planningHorizon.start | date:''dd/MM/y HH:mm:ss''}}">
        </div>
        <div class="form-group">
          <label for="EndDate">End Date</label>
          <input id="EndDate" class="form-control" type="text" disabled value="{{resource.resourceJson.planningHorizon.end | date:''dd/MM/y HH:mm:ss''}}">
        </div>
      </div>
    </div>
  </form>
</div>');

delete from config where config_id = 'Template-DiagnosticReport' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-DiagnosticReport',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
		<div class="form-group">
			<label for="Display">Display term</label>
			<input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].display}}">
		</div>
		<div class="form-group">
			<label for="Date">Effective date</label>
			<input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.effectiveDateTime | date:''dd/MM/y''}}">
		</div>
        <div *ngIf="resource.resourceJson.extension">
			<div *ngFor="let extension of resource.resourceJson.extension">
				<div class="form-group" *ngIf="extension.url==''http://endeavourhealth.org/fhir/StructureDefinition/primarycare-diagnostic-report-filed-by-extension''">
					<label for="FiledBy">Filed by</label>
					<input id="FiledBy" class="form-control" type="text" disabled value="{{extension.valueReference.display}}">
				</div>
			</div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
			<label for="Code">Code</label>
			<input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.code.coding[0].code}}">
		</div>
        <div class="form-group">
			<label for="Status">Status</label>
			<input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.status}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-DiagnosticOrder' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
  'data-assurance',
  'Template-DiagnosticOrder',
  '<div class="container-fluid">
<form>
  <div class="row">
    <div class="col-md-6">
		<div class="form-group">
			<label for="Display">Display term</label>
			<input id="Display" class="form-control" type="text" disabled value="{{resource.resourceJson.item[0].code.coding[0].display}}">
		</div>
		<div class="form-group">
			<label for="Date">Effective date</label>
			<input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.event[0].dateTime | date:''dd/MM/y''}}">
		</div>
		<div class="form-group">
			<label for="Ordered">Ordered by</label>
			<input id="OrderedBy" class="form-control" type="text" disabled value="{{resource.resourceJson.orderer.display}}">
		</div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
			<label for="Code">Code</label>
			<input id="Code" class="form-control" type="text" disabled value="{{resource.resourceJson.item[0].code.coding[0].code}}">
		</div>
        <div class="form-group">
			<label for="Status">Status</label>
			<input id="Status" class="form-control" type="text" disabled value="{{resource.resourceJson.event[0].status}}">
      </div>
    </div>
  </div>
</form>
</div>');

delete from config where config_id = 'Template-QuestionnaireResponse' and app_id = 'data-assurance';
insert into config (app_id, config_id, config_data)
values (
    'data-assurance',
    'Template-QuestionnaireResponse',
    '<div class="container-fluid">
       <form>
           <div class="row">
             <div class="col-md-6">
                <div class="form-group">
                  <label for="Author">Author</label>
                  <input id="Author" class="form-control" type="text" disabled value="{{resource.resourceJson.author?.display}}">
                </div>
                <div class="form-group">
                  <label for="CaseNo">Case No</label>
                  <input id="CaseNo" class="form-control" type="text" disabled value="{{resource.resourceJson.identifier.value}}">
                </div>
                <div *ngFor="let group of resource.resourceJson.group.group">
                  <div class="form-group"  >
                    <label for="QuestionGroup"><u>{{group.title}}</u></label>
                    <div class="form-group" *ngFor="let question of group.question">
                      <label for="Question">{{question.text}}</label>
                        <input id="Answer" class="form-control" type="text" disabled value="{{question.answer[0].valueString}}">
                    </div>
                  </div>
                </div>
             </div>
             <div class="col-md-6">
                <div class="form-group">
                  <label for="Date">Date</label>
                  <input id="Date" class="form-control" type="text" disabled value="{{resource.resourceJson.authored | date:''dd/MM/y''}}">
                </div>
             </div>
           </div>
       </form>
    </div>');
