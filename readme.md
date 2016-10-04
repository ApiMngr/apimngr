# ApiMngr

ApiMngr is a simple API Manager for RESTful APIs

http://localhost:9090

```
curl -X POST -H "Content-Type: application/json" http://localhost:9090/default/create -d '{"url":"http://petstore.swagger.io/v2/swagger.json"}'
curl -X GET --header 'Accept: application/json' 'http://localhost:9090/default/_/v2/pet/findByStatus?status=available' --include`
```