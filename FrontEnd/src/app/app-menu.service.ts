import {Injectable} from '@angular/core';
import {MenuOption} from 'eds-angular4/dist/layout/models/MenuOption';
import {AbstractMenuProvider} from 'eds-angular4';
import {Routes} from '@angular/router';
import {ResourcesComponent} from './resources/resources.component';
import {StandardReportsComponent} from './standard-reports/standard-reports.component';

@Injectable()
export class AppMenuService implements  AbstractMenuProvider {
  static getRoutes(): Routes {
    return [
      { path: '', redirectTo : 'resources', pathMatch: 'full' },
      { path: 'resources', component: ResourcesComponent },
      { path: 'reports', component: StandardReportsComponent }
    ]
  }

  getClientId(): string {
    return 'data-assurance';
  }
  getApplicationTitle(): string {
    return 'Data Assurance';
  }
  getMenuOptions(): MenuOption[] {
    return [
      {caption: 'Resources', state: 'resources', icon: 'fa fa-archive', role: 'data-assurance:resources'},
      {caption: 'Standard reports', state: 'reports', icon: 'fa fa-file-text-o', role: 'data-assurance:reports'},
    ];
  }

  useUserManagerForRoles(): boolean {
    return false;
  }
}
