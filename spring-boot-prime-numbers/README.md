# Discover prime numbers - Spring WebFlux example
## Start the process
```console
curl -X POST http://localhost:8080/start/{lastNumber}
lastNumber: The last number who will be tested
```

## Stop the process
```console
curl -X POST http://localhost:8080/stop
```

## Verify if process is running
```console
curl http://localhost:8080/isRunning
```

## Listen the process
```console
curl http://localhost:8080/listen --header content-type=application/stream+json;charset=UTF-8
```