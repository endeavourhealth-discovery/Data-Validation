import {Input, Component} from '@angular/core';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Practitioner} from '../../models/Practitioner';
import {StandardReportsService} from '../standard-reports.service';

@Component({
  selector: 'app-practitioner-picker-dialog',
  templateUrl: './practitioner-picker-dialog.component.html',
  styleUrls: ['./practitioner-picker-dialog.component.css']
})
export class PractitionerPickerDialogComponent {
  @Input() selectedPractitioner;
  @Input() organisationUuid;

  searchData: string;
  searchResults: Practitioner[] = [];

  public static open(modalService: NgbModal, organisationUuid: string) {
    const modalRef = modalService.open(PractitionerPickerDialogComponent, { backdrop: 'static' });
    modalRef.componentInstance.organisationUuid = organisationUuid;
    return modalRef;
  }

  constructor(protected activeModal: NgbActiveModal,
              private standardReportsService: StandardReportsService) {
  }

  search() {
    const vm = this;
    vm.standardReportsService.findPractitioner(vm.searchData, this.organisationUuid)
      .subscribe(
        (result) => {
          vm.searchResults = result;
        });
  }

  selectPractitioner(practitioner: Practitioner, withClose: boolean) {
    this.selectedPractitioner = practitioner;
    if (withClose)
      this.ok();
  }

  ok() {
    this.activeModal.close(this.selectedPractitioner);
    console.log('OK Pressed');
  }

  cancel() {
    this.activeModal.dismiss('cancel');
    console.log('Cancel Pressed');
  }
}
