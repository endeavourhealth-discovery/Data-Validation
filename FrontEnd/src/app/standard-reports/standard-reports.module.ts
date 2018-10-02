import { NgModule } from '@angular/core';
import { StandardReportsComponent } from './standard-reports.component';
import {StandardReportsService} from './standard-reports.service';
import { ReportParamsDialogComponent } from './report-params-dialog/report-params-dialog.component';
import { PractitionerPickerDialogComponent } from './practitioner-picker-dialog/practitioner-picker-dialog.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {FolderModule} from 'eds-angular4/dist/folder';
import {LibraryModule} from 'eds-angular4/dist/library';
import {CodingModule} from 'eds-angular4/dist/coding';
import {ModuleStateService} from 'eds-angular4/dist/common';
import {CuiControlsModule} from 'eds-angular4/dist/cuicontrols/cuicontrols.module';
import {ControlsModule} from 'eds-angular4/dist/controls';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    NgbModule,
    ControlsModule,
    CuiControlsModule,

    FolderModule,
    LibraryModule,
    CodingModule,
  ],
  declarations: [StandardReportsComponent, ReportParamsDialogComponent, PractitionerPickerDialogComponent],
  providers: [StandardReportsService, ModuleStateService],
  entryComponents: [ReportParamsDialogComponent, PractitionerPickerDialogComponent]
})
export class StandardReportsModule { }
