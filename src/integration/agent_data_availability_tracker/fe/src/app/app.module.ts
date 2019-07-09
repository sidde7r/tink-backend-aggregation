import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MainComponent } from './main/main.component';
import { DisplayAgentsComponent } from './display-agents/display-agents.component';
import { DropDownTypeaheadComponent } from './drop-down-typeahead/drop-down-typeahead.component';

import { HttpClientInMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDataService } from './in-memory-data.service';
import { DisplayFieldsComponent } from './display-fields/display-fields.component';
import { DisplayFieldValuesComponent } from './display-field-values/display-field-values.component';

@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    DisplayAgentsComponent,
    DropDownTypeaheadComponent,
    DisplayFieldsComponent,
    DisplayFieldValuesComponent
  ],
  imports: [
    HttpClientModule,
    // The HttpClientInMemoryWebApiModule module intercepts HTTP requests
    // and returns simulated server responses.
    // Remove it when a real server is ready to receive requests.
    //HttpClientInMemoryWebApiModule.forRoot(
    //  InMemoryDataService, { dataEncapsulation: false }
    //),
    BrowserModule,
    NgbModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
