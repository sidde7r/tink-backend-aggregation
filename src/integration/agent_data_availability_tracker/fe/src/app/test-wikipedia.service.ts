import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { HttpParams, HttpClient } from '@angular/common/http';
import { of, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

const WIKI_URL = 'https://en.wikipedia.org/w/api.php';
const PARAMS = new HttpParams({
  fromObject: {
    action: 'opensearch',
    format: 'json',
    origin: '*'
  }
});

@Injectable({
  providedIn: 'root'
})
export class TestWikipediaService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  public search(term: string): Observable<any> {
    if (term === '') {
      return of([]);
    }

    return this.http
      .get(WIKI_URL, {params: PARAMS.set('search', term)}).pipe(
        map(response => response[1])
      );
  }
}
