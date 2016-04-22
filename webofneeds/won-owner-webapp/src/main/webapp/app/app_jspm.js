/**
 *
 * Created by ksinger on 06.07.2015.
 */

// enable es6 in jshint:
/* jshint esnext: true */

//---- app.js-Dependencies ----
import angular from 'angular';
window.angular = angular; // for compatibility with pre-ES6/commonjs scripts

import 'fetch'; //polyfill for window.fetch (for backward-compatibility with older browsers)

import 'redux';
import ngReduxModule from 'ng-redux';
import ngReduxRouterModule from 'redux-ui-router';
import uiRouterModule from 'angular-ui-router';

//---------- Config -----------
import configRouting from './configRouting';
import configRedux from './configRedux';

//--------- Actions -----------
import { actionCreators }  from './actions/actions';

//-------- Components ---------
import topnav from './components/topnav';
import createNeedComponent from './components/create-need/create-need';
import overviewIncomingRequestsComponent from './components/overview-incoming-requests/overview-incoming-requests';
import overviewSentRequestsComponent from './components/overview-sent-requests/overview-sent-requests';
import postVisitorComponent from './components/post-visitor/post-visitor';
import postOwnerComponent from './components/post-owner/post-owner';
import postOwnerMessagesComponent from './components/post-owner-messages/post-owner-messages';
import postVisitorMsgsComponent from './components/post-visitor-msgs/post-visitor-msgs';
import { camel2Hyphen, hyphen2Camel, firstToLowerCase } from './utils';
import landingPageComponent from './components/landingpage/landingpage';
import overviewPostsComponent from './components/overview-posts/overview-posts';
import feedComponent from './components/feed/feed';
import overviewMatchesComponent from './components/overview-matches/overview-matches';

//settings
import settingsTitleBarModule from './components/settings-title-bar';
import avatarSettingsModule from './components/settings/avatar-settings';
import generalSettingsModule from './components/settings/general-settings';


/* TODO this fragment is part of an attempt to sketch a different
 * approach to asynchronity (Remove it or the thunk-based
 * solution afterwards)
 */
import { runMessagingAgent } from './messaging-agent';

let app = angular.module('won.owner', [
    ngReduxModule,
    uiRouterModule,
    ngReduxRouterModule,

    //components
    topnav, //used in rework.html/index.html

    //views
    createNeedComponent,
    overviewIncomingRequestsComponent,
    overviewSentRequestsComponent,
    postVisitorComponent,
    postOwnerComponent,
    postOwnerMessagesComponent,
    postVisitorMsgsComponent,
    landingPageComponent,
    overviewPostsComponent,
    feedComponent,
    overviewMatchesComponent,

    //views.settings
    settingsTitleBarModule,
    avatarSettingsModule,
    generalSettingsModule,

]);

app.config([ '$ngReduxProvider', configRedux ]);
app.filter('filterByNeedState', function(){
    return function(needs,state){
        var filtered =[];
        angular.forEach(needs,function(need){
            if(need.state == state){
                filtered.push(need);
            }
        })

        return filtered;
    }
})
    .filter('filterEventByType', function(){
        return function(events,uri,type){
            var filtered =[];
            angular.forEach(events,function(event){
                if(event.hasReceiverNeed == uri && event.eventType == type){
                    filtered.push(event);
                }
            })

            return filtered;
        }
    })
    /*Filters All events so that only the ones with textMessages remain*/
    .filter('filterByEventMsgs', function(){
        return function(events){
            var filtered =[];
            angular.forEach(events,function(event){
                if(event.hasTextMessage !== undefined){
                    filtered.push(event);
                }
            })

            return filtered;
        }
    })

app.config([ '$urlRouterProvider', '$stateProvider', configRouting ]);
app.run([ '$ngRedux', $ngRedux => runMessagingAgent($ngRedux) ]);

//app.run([ '$ngRedux', $ngRedux => $ngRedux.dispatch(actionCreators.runMessagingAgent()) ]);



app.run([ '$ngRedux', $ngRedux =>
    $ngRedux.dispatch(actionCreators.config__init())
]);

//check login status. TODO: this should actually be baked-in data (to avoid the extra roundtrip)
app.run([ '$ngRedux', $ngRedux => $ngRedux.dispatch(actionCreators.verifyLogin())]);

//let app = angular.module('won.owner',[...other modules...]);
angular.bootstrap(document, ['won.owner'], {
    // make sure dependency injection works after minification (or
    // at least angular explains about sloppy imports with a
    // reference to the right place)
    // see https://docs.angularjs.org/guide/production
    // and https://docs.angularjs.org/guide/di#dependency-annotation
    strictDi: true
});