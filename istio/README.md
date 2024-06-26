# Circuit breaker demo con Istio

## Prerrequisitos

- Para ambientes locales: minikube o colima ejecutando k8s
- istio instalado en el cl&uacute;ster
- kubectl


En este demo se usar&aacute; [Fortio](https://github.com/fortio/fortio), que es una herramienta que nos permitir&aacute; ejecutar pruebas de carga contra el otro servicio que ser&aacute; httpbin.


### Prueba con un Servicio Interno


1. Creaci&oacute;n de namespace, servicios y recursos de ejemplo
   ```bash
   $ kubectl apply -f namespace.yaml
   $ kubectl apply -f fortio.yaml
   $ kubectl apply -f httpbin-deployment.yaml
   ```
   
   Crearemos el recurso `DestinationRule` para httpbin donde configuraremos un l&iacute;mites bastante bajo del n&uacute;mero de conexiones http y ajustes asociados a CircuitBreaker con el [Outlier Detection](https://istio.io/latest/docs/reference/config/networking/destination-rule/#OutlierDetection).

   ```bash
   $ kubectl apply -f httpbin-destination-rule.yaml
   ```

   Se crea un pool de conexiones tcp de 1, con m&aacute;xima concurrencia de 1 peticiones. Adicionalmente si hay un error 5xx en un intervalo de 1 segundo(s), el pod es descartado por 3 minutos.

   ```yaml
   # httpbin-destination-rule.yaml
   [...]
   connectionPool:
      tcp:
        maxConnections: 1
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1

    outlierDetection:
      consecutive5xxErrors: 1
      interval: 1s
      baseEjectionTime: 3m
      maxEjectionPercent: 100
   [...]
   ```



2. Probar conexi&oacute;n:

   Corremos el siguiente comando en el pod de Fortio para que realice una peticion simple al servicio httpbin.
   ```bash
   $ FORTIO_POD=$(kubectl get pod -n circuit-breaker | grep fortio | awk '{ print $1 }')
   
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker -c fortio --  /usr/bin/fortio load -curl http://httpbin:8000/get
   ```

   Si vemos que la respuesta es como se muestra a continuaci&oacute;n entonces nuestro ambiente est&aacute; OK.
   ```bash
   HTTP/1.1 200 OK
   server: envoy
   date: Wed, 25 Mar 2020 17:01:12 GMT
   content-type: application/json
   content-length: 371
   access-control-allow-origin: *
   access-control-allow-credentials: true
   x-envoy-upstream-service-time: 5

   {
     "args": {},
     "headers": {
       "Content-Length": "0",
       "Host": "httpbin:8000",
       "User-Agent": "fortio.org/fortio-1.3.1",
       "X-B3-Parentspanid": "2c8551997f5ce61c",
       "X-B3-Sampled": "0",
       "X-B3-Spanid": "4c6e3e9881c90d60",
       "X-B3-Traceid": "00ff1af974b6a1d92c8551997f5ce61c"
     },
     "origin": "127.0.0.1",
     "url": "http://httpbin:8000/get"
   }
   ```

3. Ahora ejecutammos una prueba de carga con la configuraci&oacute;n actual:

   Estamos diciendo a Fortio que:

   - Abra dos conexiones http `-c 2`
   - Envie la mayor cantidad de peticiones que pueda por segundo `-qps 0`
   - Haciendo en total 20 peticiones `-n 20`

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker -c fortio -- /usr/bin/fortio load -c 2 -qps 0 -n 20 -loglevel Warning http://httpbin:8000/get
   ```

   La salida del comando se ver&aacute; asi:

   ```bash
   12:58:08.530 r1 [INF] logger.go:254> Log level is now 3 Warning (was 2 Info)
   Fortio 1.60.3 running at 0 queries per second, 4->4 procs, for 20 calls: http://httpbin:8000/get
   Starting at max qps with 2 thread(s) [gomax 4] for exactly 20 calls (10 per thread + 0)
   12:58:08.534 r24 [WRN] http_client.go:1104> Non ok http code, code=503, status="HTTP/1.1 503", thread=1, run=0
   12:58:08.546 r23 [WRN] http_client.go:1104> Non ok http code, code=503, status="HTTP/1.1 503", thread=0, run=0
   Ended after 54.647154ms : 20 calls. qps=365.98
   Aggregated Function Time : count 20 avg 0.0053576504 +/- 0.003339 min 0.000326236 max 0.015548664 sum 0.107153007
   # range, mid point, percentile, count
   >= 0.000326236 <= 0.001 , 0.000663118 , 5.00, 1
   > 0.001 <= 0.002 , 0.0015 , 10.00, 1
   > 0.004 <= 0.005 , 0.0045 , 70.00, 12
   > 0.005 <= 0.006 , 0.0055 , 80.00, 2
   > 0.006 <= 0.007 , 0.0065 , 85.00, 1
   > 0.007 <= 0.008 , 0.0075 , 90.00, 1
   > 0.012 <= 0.014 , 0.013 , 95.00, 1
   > 0.014 <= 0.0155487 , 0.0147743 , 100.00, 1
   # target 50% 0.00466667
   # target 75% 0.0055
   # target 90% 0.008
   # target 99% 0.0152389
   # target 99.9% 0.0155177
   Error cases : count 2 avg 0.000967372 +/- 0.0006411 min 0.000326236 max 0.001608508 sum 0.001934744
   # range, mid point, percentile, count
   >= 0.000326236 <= 0.001 , 0.000663118 , 50.00, 1
   > 0.001 <= 0.00160851 , 0.00130425 , 100.00, 1
   # target 50% 0.000326236
   # target 75% 0.00130425
   # target 90% 0.00148681
   # target 99% 0.00159634
   # target 99.9% 0.00160729
   # Socket and IP used for each connection:
   [0]   2 socket used, resolved to 10.43.145.245:8000, connection timing : count 2 avg 0.000148033 +/- 5.94e-05 min 8.8632e-05 max 0.000207434 sum 0.000296066
   [1]   2 socket used, resolved to 10.43.145.245:8000, connection timing : count 2 avg 8.52155e-05 +/- 5.788e-05 min 2.7336e-05 max 0.000143095 sum 0.000170431
   Connection time (s) : count 4 avg 0.00011662425 +/- 6.653e-05 min 2.7336e-05 max 0.000207434 sum 0.000466497
   Sockets used: 4 (for perfect keepalive, would be 2)
   Uniform: false, Jitter: false, Catchup allowed: true
   IP addresses distribution:
   10.43.145.245:8000: 4
   Code 200 : 18 (90.0 %)
   Code 503 : 2 (10.0 %)
   Response Header Sizes : count 20 avg 207.1 +/- 69.03 min 0 max 231 sum 4142
   Response Body/Total Sizes : count 20 avg 628.1 +/- 129 min 241 max 672 sum 12562
   All done 20 calls (plus 0 warmup) 5.358 ms avg, 366.0 qps
   ```

   Casi al final de la salida se muestra informaci&oacute;n sobre el estado HTTP devuelto por las peticiones. Donde el 90% fueron exitosas y el 10% fue fallida, aunque esto puede variar seg&uacute;n el ambiente donde se ejecute la prueba.

   ```bash
   [...]
   Code 200 : 18 (90.0 %)
   Code 503 : 2 (10.0 %)
   [...]
   ```



4. Ahora vamos a cambiar la configuraci&oacute;n, dejamos unicamente el atributo `outlierDetection` del DestinationRule, y quitamos las restricciones a las conexiones http.

   ```bash
   $ kubectl apply -f httpbin-destination-rule-2.yaml
   ```

   ```yaml
   [...]
   outlierDetection:
      consecutive5xxErrors: 5
      interval: 10s
      baseEjectionTime: 1m
      maxEjectionPercent: 100
   ```

   La idea con la siguiente prueba es que vamos a forzar el circuit breaker para que se abra cuando el numero de errores supere lo defindo `consecutive5xxErrors: 5` en un `interval: 10s` (10 segundos), descartando el pod por `baseEjectionTime: 1m` (un minuto)

5. Para la prueba de carga, vamos a usar un endpoint de `httpbin` que entrega un status aleatoriamente entre los que le indiquemos (200, 502). 

   **Primera ejecuci&oacute;n:**

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker -c fortio -- /usr/bin/fortio load -c 2 -qps 0 -n 20 -loglevel Warning http://httpbin:8000/status/200%2C502

   Code 200 : 12 (60.0 %)
   Code 502 : 8 (40.0 %)
   Response Header Sizes : count 20 avg 141.6 +/- 115.6 min 0 max 236 sum 2832
   Response Body/Total Sizes : count 20 avg 239.6 +/- 4.409 min 236 max 245 sum 4792
   All done 20 calls (plus 0 warmup) 5.015 ms avg, 384.8 qps
   ```

   40% de las peticiones fueron fallidas. Istio tiene en cuenta las peticiones con status 502 para realizar un eject de la instancia disponible del servicio externo

   **Segunda ejecuci&oacute;n:**

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker -c fortio -- /usr/bin/fortio load -c 2 -qps 0 -n 20 -loglevel Warning http://httpbin:8000/status/200%2C502

   Sockets used: 19 (for perfect keepalive, would be 2)
   Code 200 : 1 (5.0 %)
   Code 502 : 4 (20.0 %)
   Code 503 : 15 (75.0 %)
   Response Header Sizes : count 20 avg 11.8 +/- 51.44 min 0 max 236 sum 236
   Response Body/Total Sizes : count 20 avg 175.55 +/- 39.1 min 153 max 245 sum 3511
   All done 20 calls (plus 0 warmup) 2.497 ms avg, 781.3 qps
   ```

   Ahora solo el 5% de las peticiones fueron exitosas (estado 200).

   **Tercera ejecuci&oacute;n:**

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker -c fortio -- /usr/bin/fortio load -c 2 -qps 0 -n 20 -loglevel Warning http://httpbin:8000/status/200%2C502

   Sockets used: 20 (for perfect keepalive, would be 2)
   Code 503 : 20 (100.0 %)
   Response Header Sizes : count 20 avg 0 +/- 0 min 0 max 0 sum 0
   Response Body/Total Sizes : count 20 avg 153 +/- 0 min 153 max 153 sum 3060
   All done 20 calls (plus 0 warmup) 2.259 ms avg, 826.0 qps
   ```

   Todas las peticiones son estado 503. En este momento istio hizo el eject del pod y el circuito est&aacute; abierto. Se deber&aacute; esperar lo configurado `baseEjectionTime: 1m` para que el circuito determine si debe seguir cerrado o se abre.

### Prueba con un Servicio Externo

1. Prueba hacia servicio externo.

   No siempre los consumos ser&aacute;n realizados a servicios internos del clúster, es por ello que se requiere configurar el acceso a servicios externos en Istio para poder hacer uso de CircuitBreaker a éste nivel.

   Para poder realizar la configuraci&oacute;n de `DestinationRule` para un servicio externo, es necesario crear un `ServiceEntry`, que nos permite registrar el servicio externo en el service registry de istio. Posterior a ello creamos el `DestinationRule`.

   En este escenario se usa una api pública https://reqres.in/

   ```bash
   $ kubectl apply -f test-external-service-entry.yaml
   $ kubectl apply -f test-external-destination-rule.yaml
   ```

2. Ejecutar carga con la configuraci&oacute;n actual

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker  -c fortio -- /usr/bin/fortio load -c 5 -qps 0 -n 50 -loglevel Warning https://reqres.in/api/users/2

   ...
   Code 200 : 50 (100.0 %)
   Response Header Sizes : count 50 avg 0 +/- 0 min 0 max 0 sum 0
   Response Body/Total Sizes : count 50 avg 371 +/- 0 min 371 max 371 sum 18550
   All done 50 calls (plus 0 warmup) 71.960 ms avg, 54.3 qps
   ```

   En este caso todas las peticiones ser&aacute;n exitosas por la configuraci&oacute;n actual del DestinationRule, ahora apliquemos una configuraci&oacute;n mas restrictiva en cantidad de conexiones y peticiones por conexi&oacute;n

   ```bash
   $ kubectl apply -f test-external-destination-rule-2.yaml
   ```

   Ejecutemos nuevamente la prueba

   ```bash
   $ kubectl exec -it $FORTIO_POD -n circuit-breaker  -c fortio -- /usr/bin/fortio load -c 5 -qps 0 -n 50 -loglevel Warning https://reqres.in/api/users/2

   ...
   Code 200 : 10 (20.0 %)
   Code 400 : 40 (80.0 %)
   Response Header Sizes : count 50 avg 0 +/- 0 min 0 max 0 sum 0
   Response Body/Total Sizes : count 50 avg 163.8 +/- 103.6 min 112 max 371 sum 8190
   All done 50 calls (plus 0 warmup) 29.665 ms avg, 39.5 qps
   ```

   En este caso solo obtuvimos un 20% de peticiones exitosas.

   Adcionalmente con este comando...

   ```bash
   kubectl exec "$FORTIO_POD" -c istio-proxy -n circuit-breaker -- pilot-agent request GET stats | grep httpbin | grep pending
   ```

   ```bash
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.circuit_breakers.default.remaining_pending: 4294967295
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.circuit_breakers.default.rq_pending_open: 0
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.circuit_breakers.high.rq_pending_open: 0
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.upstream_rq_pending_active: 0
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.upstream_rq_pending_failure_eject: 0
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.upstream_rq_pending_overflow: 34
   cluster.outbound|8000||httpbin.circuit-breaker.svc.cluster.local.upstream_rq_pending_total: 606
   ```
   Se puede ver para la estadistica de `upstream_rq_pending_overflow` que 34 llamadas han sido marcadas para ser sujetas a circuit breaking.

