# BPAY Sandbox Integration

A Java 17 / Spring Boot application demonstrating two ways to call the AP+ BPAY sandbox:

1. **Hand-written client** — uses Spring `RestClient` directly.
2. **OpenAPI-generated client** — generates typed Java API/model classes from the supplied OpenAPI YAML specifications and uses the generated `RestClient` clients.

> **Sandbox/learning project only.** This repository is not affiliated with or endorsed by BPAY Group or AP+. Do not use it for production payments without a separate security, compliance, resilience, and operational review.

## Contents

- OAuth 2.0 client-credentials authentication.
- Retrieve biller details.
- Validate payment requests.
- Hand-written and OpenAPI-generated client implementations for comparison.
- Java trust-store troubleshooting notes for local sandbox development.
- 
## OpenAPI Specifications

The OpenAPI specification files used to generate the BPAY API clients are not included in this public repository.

To regenerate the API clients, obtain the relevant specifications through the official BPAY/AP+ developer resources, subject to their access and usage terms, and place them in:

- `src/main/resources/openApi/retrievebpaybiller.yaml`
- `src/main/resources/openApi/validatebpaypayments.yaml`

The project uses OpenAPI Generator as part of its build configuration. Ensure the required specification files are available locally before running the generation or build process.
## Technology

- Java 17
- Spring Boot 4.x (see `pom.xml` for the exact version)
- Maven Wrapper
- Spring `RestClient`
- OpenAPI Generator Maven Plugin
- Jackson 3 for generated clients configured with Spring Boot 4 support

## How the two client implementations differ

### A. Hand-written client

The hand-written implementation is in the application packages:

- `controller/BpayAuthController.java` — local authentication test endpoint.
- `controller/BpayController.java` — local biller and payment-validation endpoints.
- `service/BpayAuthService.java` — requests an OAuth access token using the client-credentials grant.
- `service/BpayService.java` — calls the BPAY payment API using Spring `RestClient`.
- `dto/PaymentValidationRequest.java` — request DTO for payment validation.

It builds the HTTP calls and request/response handling in application code. This makes the HTTP flow easy to follow, but request and response mapping must be maintained manually.

### B. OpenAPI-generated client

The generated Java client code is produced from the API specifications using the OpenAPI Generator Maven plugin. In the current generated output, the client packages are:

| API area | Generated package | Generated API class | Specification |
|---|---|---|---|
| Biller details | `com.example.bpay_integration.generated.biller.api` | `RetrieveBpayBillerDetailsApi` | `retrievebpaybiller.yaml` |
| Biller summaries | `com.example.bpay_integration.generated.biller.api` | `RetrieveBpayBillerSummariesApi` | `retrievebpaybiller.yaml` |
| Payment validation | `com.example.bpay_integration.generated.validation.api` | `ValidateBpayPaymentApi` | `validatebpaypayments.yaml` |

Generated models and HTTP client support classes are in the matching `.model` and `.invoker` packages. The Maven configuration uses the Java generator, `restclient` library, `useSpringBoot4=true`, `useJackson3=true`, and Java 8 date types; see `pom.xml` for the authoritative settings.

**Important distinction:** generated API classes are client libraries, not Spring MVC controllers. Generating them does not automatically create local HTTP endpoints. To expose generated-client functionality through localhost, the application must have a wrapper service/configuration and a Spring controller that calls the generated API classes. Only document or test `/api/bpay-generated/...` routes if those wrapper controllers are present in the committed source.

### Generated client lifecycle

1. The OpenAPI YAML defines operations, request/response schemas, and endpoint details.
2. Maven's `generate-sources` phase runs OpenAPI Generator.
3. Generated API classes, model classes, and invoker/client configuration are written under `target/generated-sources/openapi/`.
4. Application configuration creates the generated `ApiClient` / API beans and configures the sandbox base URL and authentication.
5. A wrapper service calls the generated API methods; a controller can expose local routes if required.

Generated output is build output. Do not copy the same generated Java classes into `src/main/java` as well; duplicate copies cause `duplicate class` compilation errors.

## Configuration

The normal and generated clients use separate Spring property namespaces. They can safely share the same environment variables when both should use the same sandbox credentials.

| Property | Used by | Value / source |
|---|---|---|
| `bpay.api.base-url` | Hand-written client | `https://sandbox.api.bpaygroup.com.au/payments/v1` |
| `bpay.auth.token-url` | Hand-written authentication service | `https://sandbox.api.bpaygroup.com.au/oauth/token` |
| `bpay.auth.client-id` | Hand-written authentication service | `${BPAY_CLIENT_ID}` |
| `bpay.auth.client-secret` | Hand-written authentication service | `${BPAY_CLIENT_SECRET}` |
| `bpay.generated.auth.token-url` | Generated-client authentication service, if configured by the application | `https://sandbox.api.bpaygroup.com.au/oauth/token` |
| `bpay.generated.auth.client-id` | Generated-client authentication service | `${BPAY_CLIENT_ID}` |
| `bpay.generated.auth.client-secret` | Generated-client authentication service | `${BPAY_CLIENT_SECRET}` |

Recommended tracked `application.properties` configuration:

```properties
spring.application.name=bpay-integration

bpay.api.base-url=https://sandbox.api.bpaygroup.com.au/payments/v1

bpay.auth.token-url=https://sandbox.api.bpaygroup.com.au/oauth/token
bpay.auth.client-id=${BPAY_CLIENT_ID}
bpay.auth.client-secret=${BPAY_CLIENT_SECRET}

bpay.generated.auth.token-url=https://sandbox.api.bpaygroup.com.au/oauth/token
bpay.generated.auth.client-id=${BPAY_CLIENT_ID}
bpay.generated.auth.client-secret=${BPAY_CLIENT_SECRET}
```

The generated namespace is useful only if the generated-client configuration/service actually reads those keys. Check the `@Value` / `@ConfigurationProperties` bindings in the committed generated-client wrapper code and keep the README aligned with them.

### Provide credentials locally

Do not put real credentials in tracked properties files, source code, README examples, Postman exports, screenshots, or shared IntelliJ run configurations.

PowerShell (current terminal session only):

```powershell
$env:BPAY_CLIENT_ID = "YOUR_SANDBOX_CLIENT_ID"
$env:BPAY_CLIENT_SECRET = "YOUR_SANDBOX_CLIENT_SECRET"
```

Run the application from the same terminal so it inherits those variables:

```powershell
.\mvnw.cmd spring-boot:run
```

In IntelliJ IDEA, set `BPAY_CLIENT_ID` and `BPAY_CLIENT_SECRET` in **Run → Edit Configurations → Environment variables**. If the normal and generated clients need separate credentials, create distinct environment variables and update the property placeholders accordingly.

If you use `application-local.properties`, keep it local and untracked. Prefer environment variables for secrets. A committed `application.properties` should contain placeholders, not real values. If a credential has already been shared or committed, rotate it with the provider; removing it from the latest file does not erase it from Git history.

## Build and run

Use JDK 17 and the repository Maven Wrapper. From the project root in PowerShell:

```powershell
.\mvnw.cmd clean
.\mvnw.cmd generate-sources
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

The current source archive uses `src/main/resources/openApi/` (capital `A`) for the YAML files, while the shared POM snapshot refers to `src/main/resources/openapi/` (lowercase `a`). Make the actual directory name and both POM `inputSpec` paths match exactly before pushing. Windows may hide this mismatch; Linux CI will not.

Expected generated output locations:

```text
target/generated-sources/openapi/biller/
target/generated-sources/openapi/validation/
```

These are generated during the build and should normally remain ignored by Git (`target/`). Commit the source YAML specifications only if you have permission to redistribute them. Do not commit generated build output just to make compilation work; fix the Maven generation configuration instead.

## Local API testing — hand-written client

Start the application, then use Postman or curl. The routes below correspond to the hand-written controller mappings in the shared source archive; re-check the current source before release.

### 1. Authentication test

```http
GET http://localhost:8080/api/bpay/auth/test
```

The endpoint should report authentication success/failure only. It must not return or log the access token, client secret, or Authorization header.

### 2. Retrieve biller details

```http
GET http://localhost:8080/api/bpay/biller/565572
```

Use a biller code supported by the sandbox account and API specification.

### 3. Validate payments

```http
POST http://localhost:8080/api/bpay/validatepayments
Content-Type: application/json
```

Example sandbox test body:

```json
{
  "payments": [
    {
      "tid": "1",
      "payment": {
        "billerCode": "565572",
        "crn": "651234567890123",
        "amount": "234.83",
        "settlementDate": "2017-10-23",
        "paymentMethod": "001"
      }
    },
    {
      "tid": "2",
      "payment": {
        "billerCode": "1313",
        "crn": "1230",
        "amount": "1045.98",
        "settlementDate": "2017-11-06",
        "paymentMethod": "001"
      }
    }
  ]
}
```

Sandbox test values can be specific. An upstream `404 Unsupported Request Payload` may indicate the sandbox rejected the request payload; it does not necessarily mean the local controller route is missing.

## Local API testing — generated-client wrapper

The generated API classes themselves are not localhost endpoints. If your application includes a wrapper controller that calls `GeneratedBpayService` / generated API beans, document its actual routes here. In the wrapper implementation discussed for this project, the intended route examples were:

| Operation | Example local route | Method |
|---|---|---|
| Generated biller lookup | `/api/bpay-generated/biller/{billerCode}` | `GET` |
| Generated payment validation | `/api/bpay-generated/validatepayments` | `POST` |

These are valid only if the corresponding generated wrapper controller is present and registered in the committed source. Verify its `@RequestMapping` annotations before publishing; the earlier source ZIP did not include that wrapper controller, so generated-client classes alone do not prove these routes exist.

Use the same sandbox request body shown above for validation only if it matches the generated DTO schema. Generated models may differ from the hand-written `PaymentValidationRequest`; use the model generated from `validatebpaypayments.yaml` and inspect required fields/value types if serialization or validation differs.

## TLS / trust-store troubleshooting

During local setup, Java raised:

```text
SSLHandshakeException: PKIX path building failed
unable to find valid certification path to requested target
```

This means the active JVM could not build a trusted certificate chain. It is not, by itself, an OAuth credential error. Check the active JDK, server certificate chain, and any corporate HTTPS inspection/proxy. Obtain CA certificates from an official source and verify their fingerprints before importing them. Never disable TLS verification or use a trust-all client.

The local trust store used during development (`bpay-truststore`) and its password must not be committed. If it is a PKCS12 store, a local PowerShell launch can use:

```powershell
$env:JAVA_TOOL_OPTIONS = "-Djavax.net.ssl.trustStore=$PWD\bpay-truststore -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.ssl.trustStoreType=PKCS12"
.\mvnw.cmd spring-boot:run
```

Only specify `PKCS12` if the file is actually in that format. `changeit` is a commonly used default for Java's `cacerts`, not a universal password. Clear the temporary environment variable after testing:

```powershell
Remove-Item Env:JAVA_TOOL_OPTIONS
```

## Troubleshooting

| Symptom | Check |
|---|---|
| `PKIX path building failed` | Active JDK, server certificate chain, proxy/HTTPS inspection, trust-store path and format. Do not disable TLS checks. |
| `401 Unauthorized` | Client credentials, OAuth scope, client-auth method, and sandbox permissions. Never log secrets/tokens. |
| `400 Bad Request` | JSON schema, required fields, `Content-Type`, and field types. |
| `404 Unsupported Request Payload` | Sandbox sample values, endpoint, and payload schema; distinguish upstream error from local 404. |
| `duplicate class` | Remove old duplicate generated folders under `src/main/java/.../generated/`; keep generation output under `target/`. |
| `release version 5 not supported` | Select JDK 17 and configure Maven compiler release 17. |
| Works on Windows but fails on Linux | Fix case-sensitive OpenAPI spec paths and filenames. |

## Public GitHub checklist

- [ ] Rotate/revoke credentials previously shared in chat, source archives, or Git history.
- [ ] Use environment variables and placeholders in tracked configuration.
- [ ] Remove code that prints the complete OAuth response or writes access tokens to `bpay-token-response.json`; parse tokens in memory and never log them.
- [ ] Keep `application-local.properties`, `.env`, `bpay-truststore`, token response files, logs, Postman exports, IDE settings, and `target/` out of Git.
- [ ] Check both normal and generated-client configuration paths for hardcoded secrets.
- [ ] Inspect staged changes (`git status --short`, `git diff --cached`) and review Git history for secrets.
- [ ] Confirm the API YAML specifications may be redistributed under the provider's terms.
- [ ] Use only synthetic test data; do not include real customer/payment information.
- [ ] Add a license only if you intend to grant reuse rights and have chosen an appropriate license.

Suggested `.gitignore` entries:

```gitignore
# Build output
target/
*.class
*.jar

# Local credentials and configuration
.env
.env.*
!.env.example
application-local.properties
application-*.local.properties
bpay-token-response.json
bpay-truststore
bpay-truststore.*
*.jks
*.keystore
*.p12
*.pfx
*.pem
*.key

# Logs and local exports
*.log
logs/
postman-local*.json
*.har

# IDE files
.idea/
*.iml
.vscode/
```

If a secret was committed, rotate it first. A `.gitignore` rule only prevents future untracked files from being added; it does not remove already tracked files or erase Git history.

## Disclaimer

This repository is a sandbox integration example. Follow the AP+/BPAY documentation, API access terms, and your organization's security requirements. No production-readiness, financial, legal, or compliance assurance is implied.
