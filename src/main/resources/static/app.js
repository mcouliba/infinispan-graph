(function () {
    'use strict';

    angular
        .module('app'
            , [
                'ngRoute'
            ])
        .config(config);

    config.$inject = ['$routeProvider', '$locationProvider', '$httpProvider'];
    function config($routeProvider, $locationProvider, $httpProvider) {
        $locationProvider.hashPrefix('');

        $routeProvider
            .when('/graph', {
                templateUrl: 'partials/graph.html'
                , controller: 'GraphController'
                , controllerAs: 'vm'
            })
            .otherwise({ redirectTo: '/graph' });
    }
})();
