# Mocks para los servicios de notificaciones que son usados internamente por el microservicio transfer-service

Se us&oacute; [Mockoon](https://mockoon.com/) para la creaci&oacute;n de los dos servicios de notificaciones que se usan en el demo de circuit breaker por uno de sus driven adapters.     


|Mock    | Descripci&oacute;n                  | endpoint |
|--------|-------------------------------------|----------|
| alfa   | servicio de notiticacion principal  | http://localhost:3001/alfa/api/notification    |
| beta   | servicio de notiticacion de respaldo| http://localhost:3002/beta/api/message    |


Para ejecutar estos mocks pueden usar una de las siguientes 3 alternativas:

## 1. Usando Mockoon

Si tiene el cliente mockoon instalado puede abrir la herramienta y cargar ambos archivos json, y posteriormente ponerlos en ejecuci&oacute;n (dando play) a cada uno de ellos.

## 2. Usando Mockoon-cli

Alternativamente puede instalar y usar el [mockoon-cli](https://mockoon.com/cli/).

### Prequisitos

- Npm

```shell
# instalar el cli
npm install -g @mockoon/cli
``` 

```shell
# ejecutar el mock de alfa (puerto 3001)
mockoon-cli start --data ./alfa_notification_api.json 

# ejecutar el mock de beta (puerto 3002)
mockoon-cli start --data ./beta_notification_api.json
``` 

## 3. Usando Docker

Alternativamente puede usar Docker para ejecutar los mocks.

### Prequisitos

- Docker

```shell
# poner a correr ambos mocks (puerto 3001 y 3002) con el script provisto.
sh run_with_docker.sh
``` 

