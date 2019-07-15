import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { AgentFields } from './model/agent-fields';
import { Agent } from './model/agent';
import { AgentFieldStats } from './model/agent-field-stats';

@Injectable({
  providedIn: 'root'
})
export class InMemoryDataService implements InMemoryDbService {
  createDb() {
    const agents: Agent[] = [
      { agent: 'es-lacaixa-password' },
      { agent: 'es-bbva-password' },
    ];

    const agentFields: AgentFields[] = [
      { id: 'es-lacaixa-password', fields: [
        'Account<SAVINGS>.accountNumber',
        'Account<SAVINGS>.bankId',
        'Account<SAVINGS>.name',
        'Account<SAVINGS>.holderName',
        'Account<SAVINGS>.type',
        'Account<SAVINGS>.identifiers',
        'Account<CHECKING>.accountNumber',
        'Account<CHECKING>.bankId',
        'Account<CHECKING>.name',
        'Account<CHECKING>.holderName',
        'Account<CHECKING>.type',
        'Account<CHECKING>.identifiers'
      ]},
      { id: 'es-bbva-password', fields: [
        'Account<SAVINGS>.accountNumber',
        'Account<SAVINGS>.bankId',
        'Account<SAVINGS>.name',
        'Account<SAVINGS>.holderName',
        'Account<SAVINGS>.type',
        'Account<SAVINGS>.identifiers',
        'Account<CHECKING>.accountNumber',
        'Account<CHECKING>.bankId',
        'Account<CHECKING>.name',
        'Account<CHECKING>.type',
        'Account<CHECKING>.identifiers'
      ]},
    ];

    const agentFieldsStats: AgentFieldStats[] = [
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.accountNumber', nullCount: 4, totalCount: 10, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.bankId', nullCount: 2, totalCount: 4, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.name', nullCount: 1, totalCount: 7, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.holderName', nullCount: 5, totalCount: 5, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.type', nullCount: 2, totalCount: 40, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.identifiers', nullCount: 23, totalCount: 41, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.accountNumber', nullCount: 4, totalCount: 65, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.bankId', nullCount: 35, totalCount: 450, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.name', nullCount: 0, totalCount: 1, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.holderName', nullCount: 10, totalCount: 11, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.type', nullCount: 4, totalCount: 21, lastSeen: Date.now() },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.identifiers', nullCount: 20, totalCount: 400, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.accountNumber', nullCount: 200, totalCount: 400, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.bankId', nullCount: 27, totalCount: 45, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.name', nullCount: 19, totalCount: 57, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.holderName', nullCount: 57, totalCount: 92, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.type', nullCount: 15, totalCount: 91, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.identifiers', nullCount: 48, totalCount: 82, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.accountNumber', nullCount: 14, totalCount: 68, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.bankId', nullCount: 11, totalCount: 15, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.name', nullCount: 0, totalCount: 4, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.type', nullCount: 173, totalCount: 482, lastSeen: Date.now() },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.identifiers', nullCount: 83, totalCount: 173, lastSeen: Date.now() },
    ];

    const fieldValues = [
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.accountNumber', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.bankId', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.name', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.holderName', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.type', value: 'SAVINGS' },
      { agent: 'es-lacaixa-password', field: 'Account<SAVINGS>.identifiers', value: 'iban' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.accountNumber', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.bankId', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.name', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.holderName', value: 'REDACTED' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.type', value: 'CHECKING' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.identifiers', value: 'iban' },
      { agent: 'es-lacaixa-password', field: 'Account<CHECKING>.identifiers', value: 'accountNumber' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.accountNumber', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.bankId', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.name', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.holderName', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.type', value: 'SAVINGS' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.type', value: 'CHECKING' },
      { agent: 'es-bbva-password', field: 'Account<SAVINGS>.identifiers', value: 'iban' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.accountNumber', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.bankId', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.name', value: 'REDACTED' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.type', value: 'CHECKING' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.identifiers', value: 'iban' },
      { agent: 'es-bbva-password', field: 'Account<CHECKING>.identifiers', value: 'accountNumber' },
    ];

    return { agents, agentFields, agentFieldsStats, fieldValues };
  }
}
