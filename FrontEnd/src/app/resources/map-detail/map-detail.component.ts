import {Component, Input, OnChanges, SimpleChange} from '@angular/core';
import {ResourceFieldMapping} from '../../models/ResourceFieldMapping';

@Component({
  selector: 'app-map-detail',
  templateUrl: './map-detail.component.html',
  styleUrls: ['./map-detail.component.css']
})
export class MapDetailComponent {
  @Input() mapDetail: any;

  constructor() { }

  getLocation(row: ResourceFieldMapping) {
    if (row.sourceLocation)
      return row.sourceLocation;

    return this.getFilename(row.sourceFileName) + ' [Row:' + row.sourceFileRow + ', Col:' + row.sourceFileColumn + ']';
  }

  getFilename(fullPath: string) {
    let lastSlash = fullPath.lastIndexOf('\\');
    if (lastSlash >= 0)
      return fullPath.substr(lastSlash + 1);

    lastSlash = fullPath.lastIndexOf('/');
    if (lastSlash >= 0)
      return fullPath.substr(lastSlash + 1);

    return fullPath;
  }

  hide() {
    this.mapDetail = null;
  }
}
