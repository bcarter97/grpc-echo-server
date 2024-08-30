# echo-grpc-server

A simple gRPC server that takes a [Status Code](https://grpc.github.io/grpc/core/md_doc_statuscodes.html) and returns
a response after the configured delay.

If the Status Code is an error (greater than 0), this will be raised as a gRPC error.

## Example

### OK

```shell
grpcurl --emit-defaults -plaintext -d '{"code": 0, "delay": 10}' localhost:4770 io.github.bcarter97.echo.v1.Echo/Echo

{
  "code": 0,
  "delay": "10"
}
```

### Error

```shell
grpcurl -plaintext -d '{"code": 14, "delay": 500}' localhost:4770 io.github.bcarter97.echo.v1.Echo/Echo

ERROR:
  Code: Unavailable
  Message: UNAVAILABLE after 500 milliseconds
```
