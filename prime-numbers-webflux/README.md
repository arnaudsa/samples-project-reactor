# Discover prime numbers - Spring WebFlux example
## Start the process
```
curl -v -X POST http://localhost:8080/start/{qty}
```
qty: Quantity of prime numbers to discover

## Stop the process
```
curl -v -X POST http://localhost:8080/stop
```

## Verify if process is running
```
curl -v http://localhost:8080/isRunning
```

## Listen the process
```
curl -v http://localhost:8080/listen --header content-type=application/stream+json
```