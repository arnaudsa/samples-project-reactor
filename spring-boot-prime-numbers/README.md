# Discover prime numbers - Spring WebFlux example
## Start the process
```
curl -X POST http://localhost:8080/start/{lastNumber}
```
lastNumber: The last number who will be tested

## Stop the process
```
curl -X POST http://localhost:8080/stop
```

## Verify if process is running
```
curl http://localhost:8080/isRunning
```

## Listen the process
```
curl http://localhost:8080/listen --header content-type=application/stream+json;charset=UTF-8
```