import {
  AfterViewInit, Compiler, Component, Injector, Input, NgModule, NgModuleRef,
  ViewChild, ViewContainerRef
} from '@angular/core';
import {ServicePatientResource} from '../../models/Resource';
import {ResourcesService} from '../resources.service';
import {CommonModule} from '@angular/common';
import {PipesModule} from "eds-angular4/dist/pipes/pipes.module";
import {ViewerComponent} from '../viewer/viewer.component';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-template-view',
  templateUrl: './template-view.component.html',
  styleUrls: ['./template-view.component.css']
})
export class TemplateViewComponent implements AfterViewInit {
  @Input() resource: ServicePatientResource;
  @Input() title: String;
  @ViewChild('dataContainer', {read: ViewContainerRef}) dataContainer: ViewContainerRef;

  constructor(private resourceService: ResourcesService,
              private modal: NgbModal,
              private active: NgbActiveModal,
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
      resource: ServicePatientResource;
      modal: NgbModal;
      active: NgbActiveModal;
      service: ResourcesService;
      title: string;

      private getActiveOnly(resources: any[]) : any[] {
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

      public viewResource(reference: string) {
        console.log('Showing resource ' + reference);
        const vm = this;

        this.service.getResource(this.resource.serviceId, reference)
          .subscribe(
            (result) => {
              const res: ServicePatientResource = {
                serviceId: vm.resource.serviceId,
                resourceJson: result
              } as ServicePatientResource;

              const parent = window.document.body.getElementsByClassName('modal fade show').item(0);
              const parentbg = window.document.body.getElementsByClassName('modal-backdrop fade show').item(0);
              parent.setAttribute('class', 'modal fade');
              parentbg.setAttribute('class', 'modal-backdrop fade');

              ViewerComponent.open(vm.modal, vm.title + ' -> ' + res.resourceJson.resourceType, res)
                .result.then(
                () => {
                  parent.setAttribute('class', 'modal fade show');
                  parentbg.setAttribute('class', 'modal-backdrop fade show');
                }
              );


            },
            (error) => console.error(error)
          );
      }
    });
    const tmpModule = NgModule({
      imports: [CommonModule, PipesModule],
      declarations: [tmpCmp]
    })(class {
    });

    this._compiler.compileModuleAndAllComponentsAsync(tmpModule)
      .then((factories) => {
        const f = factories.componentFactories[0];
        const cmpRef = f.create(this._injector, [], null, this._m);
        cmpRef.instance.resource = this.resource;
        cmpRef.instance.service = this.resourceService;
        cmpRef.instance.modal = this.modal;
        cmpRef.instance.active = this.active;
        cmpRef.instance.title = this.title;
        this.dataContainer.insert(cmpRef.hostView);
      });
  }

}
