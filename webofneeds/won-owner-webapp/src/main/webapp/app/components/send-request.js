;

import angular from 'angular';
import 'ng-redux';
import extendedGalleryModule from '../components/extended-gallery';
import { labels } from '../won-label-utils';
import { attach } from '../utils';
import { actionCreators }  from '../actions/actions';

const serviceDependencies = ['$q', '$ngRedux', '$scope'];

function genComponentConf() {
    let template = `
        <div class="sr__caption">
            <div class="sr__caption__title">Send Conversation Request</div>
            <img class="sr__caption__icon clickable" src="generated/icon-sprite.svg#ico36_close" ng-click="self.closeRequest()"/>
        </div>
        <div class="sr__header">
            <div class="sr__header__title">
                <div class="sr__header__title__topline">
                    <div class="sr__header__title__topline__title">{{self.item.remoteNeed.title}}</div>
                    <div class="sr__header__title__topline__date">{{self.item.remoteNeed.creationDate}}</div>
                </div>
                <div class="sr__header__title__subtitle">
                    <span class="sr__header__title__subtitle__group" ng-show="self.item.group">
                        <img src="generated/icon-sprite.svg#ico36_group" class="sr__header__title__subtitle__group__icon">{{self.item.group}}<span class="sr__header__title__subtitle__group__dash"> &ndash; </span>
                    </span>
                    <span class="sr__header__title__subtitle__type">{{self.labels.type[self.item.type]}}</span>
                </div>
            </div>
        </div>
        <div class="sr__content">
            <div class="sr__content__images" ng-show="self.item.images">
                <won-extended-gallery max-thumbnails="self.maxThumbnails" items="self.item.images" class="vertical"></won-extended-gallery>
            </div>
            <div class="sr__content__description">
                <div class="sr__content__description__location">
                    <img class="sr__content__description__indicator" src="generated/icon-sprite.svg#ico16_indicator_location"/>
                    <span>Vienna area</span>
                </div>
                <div class="sr__content__description__datetime">
                    <img class="sr__content__description__indicator" src="generated/icon-sprite.svg#ico16_indicator_time"/>
                    <span>Available until 5th May</span>
                </div>
                <div class="sr__content__description__text">
                    <img class="sr__content__description__indicator" src="generated/icon-sprite.svg#ico16_indicator_description"/>
                    <span>These lovley Chairs need a new home since I am moving These are the first X chars of the message et eaquuntiore dolluptaspid quam que quatur quisinia aspe sus voloreiusa plis Sae quatectibus eumendi bla volupita dolupta el et andunt …</span>
                </div>
            </div>
        </div>
        <div class="sr__footer">
            <input type="text" ng-model="self.message" placeholder="Reply Message (optional)"/>
            <div class="flexbuttons">
                <button class="won-button--filled black" ng-click="self.closeOverlay()">Cancel</button>
                <button class="won-button--filled red" ng-click="self.sendRequest(self.message)">Request Contact</button>
            </div>
        </div>
    `;

    class Controller {
        constructor() {
            attach(this, serviceDependencies, arguments);
            this.maxThumbnails = 9;
            this.labels = labels;
            this.message = '';

            const disconnect = this.$ngRedux.connect(null, actionCreators)(this);
            this.$scope.$on('$destroy', disconnect);
        }

        sendRequest(message) {
            this.connections__connect(this.item,message);
            this.item = undefined;
        }

        closeOverlay(){
            this.item = undefined;
        }
    }
    Controller.$inject = serviceDependencies;

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {item: "="},
        template: template
    }
}

export default angular.module('won.owner.components.sendRequest', [
    extendedGalleryModule
])
    .directive('wonSendRequest', genComponentConf)
    .name;
