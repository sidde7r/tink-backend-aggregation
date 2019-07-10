import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BaseService {

  constructor() { }

  public search(term: string): Observable<any> {
    console.log('must be implemented in child class');
    return of('');
  }
}
