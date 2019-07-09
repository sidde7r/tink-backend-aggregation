import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {AgentFieldStats} from './model/agent-field-stats';
import {AgentFields} from './model/agent-fields';
import {FieldValue} from './model/field-value';
import {map, filter, take, tap, find, first} from 'rxjs/operators';
import {Agent} from './model/agent';

@Injectable({
  providedIn: 'root'
})
export class AgentCtService {

  private agentsUrl = 'http://192.168.99.100:30788/api/agents';
  private agentFieldsUrl = 'http://192.168.99.100:30788/api/agentFields';
  private agentFieldStatsUrl = 'http://192.168.99.100:30788/api/agentFieldStats';
  private agentFieldValuesUrl = 'http://192.168.99.100:30788/api/agentFieldValues';

  private agentsUrlDebug = 'api/agents';
  private agentFieldsUrlDebug = 'api/agentFields';
  private agentFieldStatsUrlDebug = 'api/agentFieldStats';
  private agentFieldValuesUrlDebug = 'api/agentFieldValues';

  /*
    If you want to use mock database instead of fetching data from tink-backend (i.e use it in
    debug mode, make this variable true and enable HttpClientInMemoryWebApiModule in app.module.ts
   */
  private debug = false;

  constructor(private http: HttpClient) {
  }

  getAgents(term: string): Observable<Agent[]> {
    console.log('getAgents (term = ' + term + ')');
    if (this.debug) {
      return this.http.get<Agent[]>(this.agentsUrlDebug).pipe(
        map(agents => agents.filter(agent => agent.agent.includes(term))),
        map(agents => agents.slice(0, 5))
      );
    } else {
      console.log('URL = ' + this.agentsUrl);
      return this.http.get<Agent[]>(this.agentsUrl).pipe(
        map(agents => agents.filter(agent => agent.agent.includes(term))),
        map(agents => agents.slice(0, 5))
      );
    }
  }

  getAgentFields(agent: string, term: string): Observable<string[]> {
    console.log('getAgentFields (term = ' + term + ', agent = ' + agent + ')');
    if (this.debug) {
      return this.http.get<AgentFields>(`${this.agentFieldsUrlDebug}/${agent}`).pipe(
        map(agentFields => agentFields.fields.filter(field => field.includes(term))),
        map(agentFields => agentFields.slice(0, 10))
      );
    } else {
      console.log('URL = ' + `${this.agentFieldsUrl}?agent=${agent}`);
      return this.http.get<AgentFields>(`${this.agentFieldsUrl}?agent=${agent}`).pipe(
        map(agentFields => agentFields.fields.filter(field => field.includes(term))),
        map(agentFields => agentFields.slice(0, 10))
      );
    }
  }

  getAgentFieldStats(agent: string, field: string): Observable<AgentFieldStats> {
    console.log('getAgentFieldStats (field = ' + field + ', agent = ' + agent + ')');
    if (this.debug) {
      return this.http.get<AgentFieldStats[]>(`${this.agentFieldStatsUrlDebug}/${agent}/${field}`).pipe(
        map(agentFieldsStats => {
          return agentFieldsStats.find(agentFieldStats => agentFieldStats.agent === agent && agentFieldStats.field === field);
        }),
      );
    } else {
      console.log('URL = ' + `${this.agentFieldStatsUrl}?agent=${agent}&field=${field}`);
      return this.http.get<AgentFieldStats>(`${this.agentFieldStatsUrl}?agent=${agent}&field=${field}`).pipe(
        map(agentFieldsStats => {
          return agentFieldsStats;
        }),
      );
    }
  }

  getAgentFieldsStats(agent: string): Observable<AgentFieldStats[]> {
    console.log('getAgentFieldStats (agent = ' + agent + ')');
    if (this.debug) {
      return this.http.get<AgentFieldStats[]>(`${this.agentFieldStatsUrlDebug}/${agent}`).pipe(
        map(agentFieldStats => {
          return agentFieldStats.filter(agentFieldStat => agentFieldStat.agent === agent);
        })
      );
    } else {
      console.log('URL = ' + `${this.agentFieldStatsUrl}?agent=${agent}`);
      return this.http.get<AgentFieldStats[]>(`${this.agentFieldStatsUrl}?agent=${agent}`).pipe(
        map(agentFieldStats => {
          return agentFieldStats;
        })
      );
    }
  }

  getAgentFieldValues(agent: string, field: string): Observable<FieldValue[]> {
    console.log('getAgentFieldValues (field = ' + field + ', agent = ' + agent + ')');
    if (this.debug) {
      return this.http.get<FieldValue[]>(`${this.agentFieldValuesUrlDebug}/${agent}/${field}`).pipe(
        map(fieldValues => {
          return fieldValues.filter(
            fieldValue => fieldValue.agent === agent && fieldValue.field === field);
        })
      );
    } else {
      console.log('URL = ' + `${this.agentFieldValuesUrl}?agent=${agent}&field=${field}`);
      return this.http.get<FieldValue[]>(`${this.agentFieldValuesUrl}?agent=${agent}&field=${field}`).pipe(
        map(fieldValues => {
          return fieldValues;
        })
      );
    }
  }

  isValidAgent(agentName: string): Observable<boolean> {
    console.log('isValidAgent (agentName = ' + agentName + ')');
    if (this.debug) {
      return this.http.get<Agent[]>(this.agentsUrlDebug).pipe(
        map(agents => agents.some(agent => agent.agent === agentName)),
      );
    } else {
      console.log('URL = ' + this.agentsUrl);
      return this.http.get<Agent[]>(this.agentsUrl).pipe(
        map(agents => agents.some(agent => agent.agent === agentName)),
      );
    }
  }

  isValidField(agentName: string, fieldName: string): Observable<boolean> {
    console.log('isValidField (field = ' + fieldName + ', agent = ' + agentName + ')');
    if (this.debug) {
      return this.http.get<AgentFields>(`${this.agentFieldsUrlDebug}/${agentName}`).pipe(
        map(agentFields => agentFields.fields.some(field => field === fieldName)),
      );
    } else {
      console.log('URL = ' + `${this.agentFieldsUrl}?agent=${agentName}`);
      return this.http.get<AgentFields>(`${this.agentFieldsUrl}?agent=${agentName}`).pipe(
        map(agentFields => agentFields.fields.some(field => field === fieldName)),
      );
    }
  }
}
