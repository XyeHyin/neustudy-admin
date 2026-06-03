## Backend Package Layout

- `common`: shared response and pagination models.
- `config`: Spring configuration classes.
- `config.properties`: typed configuration properties bound from YAML/env vars.
- `constant`: shared constants.
- `controller`: HTTP API controllers.
- `dto`: request payload models, grouped by feature.
- `entity`: JPA entities.
- `handler`: global exception handling.
- `interceptor`: web interceptors.
- `mapper`: MapStruct mappers.
- `repository`: Spring Data repositories.
- `security`: authentication, token, and password helpers.
- `service`: business services.
- `utils`: generic infrastructure utilities that are not security-specific.
- `vo`: response view models, grouped by feature.
