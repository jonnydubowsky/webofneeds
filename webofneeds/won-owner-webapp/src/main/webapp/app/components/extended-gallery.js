/**
 * Created by ksinger on 28.08.2015.
 */

;
import angular from 'angular';

function genComponentConf() {
    let template = `
        <div class="eg__selected" ng-show="self.items">
            <img ng-src="{{self.selectedImgUrl? self.selectedImgUrl : self.items[0].src}}" alt="a table"/>
        </div>
        <div class="eg__thumbs" ng-show="self.items && self.items.length > 1">
            <div class="eg__thumbs__frame clickable" ng-repeat="item in self.items track by $index" ng-click="self.showImage(item.src)" ng-show="self.showMore || $index < self.maxThumbnails">
                <img src="{{item.src}}" alt="a combination of shelfs"/>
            </div>
            <div class="eg__thumbs__more clickable" ng-click="self.showMore = true" ng-show="!(self.showMore || self.items.length <= self.maxThumbnails)">
                <span>+{{self.items.length-self.maxThumbnails}}</span>
            </div>
        </div>`;

    class Controller {
        constructor() {
            self.showMore=false;
        }

        showImage(src){
            this.selectedImgUrl = src;
        }
    }

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {maxThumbnails: "=",
                items: "="},
        template: template
    }
}

export default angular.module('won.owner.components.extendedGallery', [])
    .directive('wonExtendedGallery', genComponentConf)
    .name;
