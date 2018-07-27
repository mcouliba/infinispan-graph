(function () {
    'use strict';

    angular
        .module('app')
        .factory('CacheInfoService', CacheInfoService);

    CacheInfoService.$inject = ['$http', '$q'];

    function CacheInfoService($http, $q) {
        var factory = {
            get: get
        };

        return factory;

        function get() {
            console.log('Fetching CacheInfo...');
            var deferred = $q.defer();
            $http.get("clusterinfo/caches")
                .then(
                    function (response) {
                        console.log('Fetched successfully CacheInfo!');
                        deferred.resolve(response.data);
                    },
                    function (errResponse) {
                        console.error('Error while fetching CacheInfo');
                        deferred.reject(errResponse);
                    }
                );
            return deferred.promise;
        }
  };
})();
