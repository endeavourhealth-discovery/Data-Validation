import {
  AfterViewInit, Compiler, Component, Injector, Input, NgModule, NgModuleRef,
  ViewChild, ViewContainerRef
} from '@angular/core';
import {ServicePatientResource} from '../../models/Resource';
import {ResourcesService} from '../resources.service';
import {CommonModule} from '@angular/common';
import {PipesModule} from "eds-angular4/dist/pipes/pipes.module";

@Component({
  selector: 'app-template-view',
  templateUrl: './template-view.component.html',
  styleUrls: ['./template-view.component.css']
})
export class TemplateViewComponent implements AfterViewInit {
  @Input() resource: ServicePatientResource;
  @ViewChild('dataContainer', {read: ViewContainerRef}) dataContainer: ViewContainerRef;

  constructor(private resourceService: ResourcesService,
              private _compiler: Compiler,
              private _injector: Injector,
              private _m: NgModuleRef<any>) {
  }

  ngAfterViewInit() {
    this.loadTemplate();
  }

  loadTemplate() {
    const vm = this;
    vm.resourceService.getTemplate(this.resource.resourceJson.resourceType)
      .subscribe(
        (result) => vm.buildView(result),
        (error) => console.log(error)
      );
  }

  private buildView(template: string) {

    if (template == null || template === '')
      template = '<div class="container"><h3>No clinical template configured for this resource type</h3></div>';

    const tmpCmp = Component({template: template})(class {

      private getActiveOnly(resources: any[]) : any[] {
        console.log(resources);
        let active: any[] = [];
        for (const resource of resources) {
          if (!resource.period || !resource.period.end) {
            active.push(resource);
          } else {
            var endDate = new Date(resource.period.end);
            if (endDate > new Date()) {
              active.push(resource);
            }
          }
        }
        console.log(active);
        return active.length > 0 ? active : this.getLatestEnded(resources);
      }

      private getLatestEnded(resources: any[]) : any[] {
        // all resources have an end date in the period at this point
        let latest: any[] = [];
        let latestDate : Date = new Date('1750-01-01');
        for (const resource of resources) {
          var endDate = new Date(resource.period.end);
          if (endDate == latestDate) {
            latest.push(resource);
          } else if (endDate > latestDate) {
            latest = [];
            latest.push(resource);
          }
        }

        return latest;
      }
    });
    const tmpModule = NgModule({imports: [CommonModule, PipesModule], declarations: [tmpCmp]})(class {
    });

    this._compiler.compileModuleAndAllComponentsAsync(tmpModule)
      .then((factories) => {
        const f = factories.componentFactories[0];
        const cmpRef = f.create(this._injector, [], null, this._m);
        cmpRef.instance.resource = this.resource;
        this.dataContainer.insert(cmpRef.hostView);
      });
  }
}
