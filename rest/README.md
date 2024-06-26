# Herramientas para invocar servicio de tansferencia

## Pre-requisitos

- El servicio transfer service esta ejecutandose y escuchando por el puerto 8080.

## Endpoint de transferencias

```bash
curl --request POST \
  --url http://localhost:8080/api/money/send \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnium/0.2.3-a' \
  --data '{
	"originAccount": "23123123199",
	"destinationAccount": "98979789799",
	"amount": 1000.0
}'
```

```bash
# response
{"message":"Transferencia exitosa"}
```

## Coleccion insomnium

Puede usar el archivo `insomnium.json` e importarlo en el cliente [insomnium](https://github.com/ArchGPT/insomnium).

## Shell Script

O puede usar el script `call_loop.sh` para invocar el endpoint de transferencias cada 2 segundos.

