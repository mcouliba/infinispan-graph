(function () {
    'use strict';

    angular
        .module('app')
        .factory('NodeInfoService', NodeInfoService);

    NodeInfoService.$inject = ['$http', '$q'];

    function NodeInfoService($http, $q) {
        var factory = {
            get: get
        };

        return factory;

        function get(cachename, containerName) {
            console.log('Fetching NodeInfo...');
            var deferred = $q.defer();
            $http.get("clusterinfo/nodes?cacheName=" + cachename + "&containerName=" + containerName)
                .then(
                    function (response) {
                        console.log('Fetched successfully NodeInfo!');
                        deferred.resolve(response.data);
                    },
                    function (errResponse) {
                        console.error('Error while fetching NodeInfo');
                        deferred.reject(errResponse);
                    }
                );
            return deferred.promise;
        }
  };
})();
